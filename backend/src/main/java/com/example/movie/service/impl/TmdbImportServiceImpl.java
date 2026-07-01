package com.example.movie.service.impl;

import com.example.movie.config.TmdbProperties;
import com.example.movie.dto.admin.TmdbImportResultDTO;
import com.example.movie.service.AdminStatisticsService;
import com.example.movie.service.TmdbImportService;
import com.example.movie.service.TmdbApiClient;
import com.example.movie.service.MovieCacheService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;

@Service
@RequiredArgsConstructor
public class TmdbImportServiceImpl implements TmdbImportService {

    private final JdbcTemplate jdbcTemplate;
    private final TmdbProperties tmdbProperties;
    private final TmdbApiClient tmdbApiClient;
    private final AdminStatisticsService adminStatisticsService;
    private final MovieCacheService movieCacheService;
    private final ExecutorService tmdbFetchExecutor = Executors.newFixedThreadPool(5, runnable -> {
        Thread thread = new Thread(runnable, "tmdb-import-fetch");
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public TmdbImportResultDTO importPopularMovies(Integer limit) {
        int target = normalizeLimit(limit);
        ensureConfigured();

        List<Long> movieIds = fetchPopularMovieIds(target);
        List<FetchedMovie> fetchedMovies = fetchMovieDetails(movieIds);
        List<String> errors = new ArrayList<>();
        int imported = 0;
        int skipped = 0;
        for (FetchedMovie fetched : fetchedMovies) {
            if (fetched.detail == null || fetched.detail.path("id").isMissingNode()) {
                skipped++;
                addError(errors, "TMDB " + fetched.tmdbId + "：" + fetched.error);
                continue;
            }
            try {
                upsertMovie(fetched.detail);
                imported++;
            } catch (Exception exception) {
                skipped++;
                addError(errors, "TMDB " + fetched.tmdbId + "：写入失败 - " + rootMessage(exception));
            }
        }

        TmdbImportResultDTO result = new TmdbImportResultDTO();
        result.setMode("import");
        result.setRequested(target);
        result.setImported(imported);
        result.setUpdated(0);
        result.setSkipped(skipped);
        result.setSource("TMDB popular movies");
        result.setErrors(errors);
        
        if (imported > 0) {
            adminStatisticsService.clearDashboardCache();
            movieCacheService.evictMovieCaches();
        }
        return result;
    }

    @Override
    public TmdbImportResultDTO addNewMovies(Integer limit, String source) {
        int target = normalizeLimit(limit);
        ensureConfigured();
        String normalizedSource = source == null ? "popular" : source.trim().toLowerCase();
        if (!Set.of("popular", "top-rated").contains(normalizedSource)) {
            throw new IllegalArgumentException("导入来源仅支持 popular 或 top-rated");
        }
        String tmdbListPath = normalizedSource.equals("top-rated") ? "/movie/top_rated" : "/movie/popular";
        String sourceLabel = normalizedSource.equals("top-rated") ? "TMDB 高分榜" : "TMDB 热门榜";

        // 查询本地已有的所有 tmdb_id
        Set<Long> existingTmdbIds = new LinkedHashSet<>(
                jdbcTemplate.queryForList("SELECT tmdb_id FROM movie WHERE tmdb_id IS NOT NULL AND deleted = 0", Long.class)
        );

        // 从热门榜翻页，跳过已有的，凑够 target 个新影片
        List<Long> newMovieIds = new ArrayList<>();
        int maxPage = 50; // TMDB 热门榜最多 500 页，但我们限制到 50 页 = 1000 部
        for (int page = 1; page <= maxPage && newMovieIds.size() < target; page++) {
            JsonNode payload = fetchJson(tmdbListPath + "?language=zh-CN&page=" + page);
            JsonNode results = payload.path("results");
            if (!results.isArray() || results.isEmpty()) {
                break;
            }
            for (JsonNode item : results) {
                long tmdbId = item.path("id").asLong();
                if (tmdbId > 0 && !existingTmdbIds.contains(tmdbId)) {
                    newMovieIds.add(tmdbId);
                    existingTmdbIds.add(tmdbId); // 避免同页重复
                    if (newMovieIds.size() >= target) {
                        break;
                    }
                }
            }
        }

        List<FetchedMovie> fetchedMovies = fetchMovieDetails(newMovieIds);
        List<String> errors = new ArrayList<>();
        int imported = 0;
        int skipped = 0;
        for (FetchedMovie fetched : fetchedMovies) {
            if (fetched.detail == null || fetched.detail.path("id").isMissingNode()) {
                skipped++;
                addError(errors, "TMDB " + fetched.tmdbId + "：" + fetched.error);
                continue;
            }
            try {
                upsertMovie(fetched.detail);
                imported++;
            } catch (Exception exception) {
                skipped++;
                addError(errors, "TMDB " + fetched.tmdbId + "：写入失败 - " + rootMessage(exception));
            }
        }

        TmdbImportResultDTO result = new TmdbImportResultDTO();
        result.setMode("add");
        result.setRequested(target);
        result.setImported(imported);
        result.setUpdated(0);
        result.setSkipped(skipped);
        result.setSource(sourceLabel + "（仅新增）");
        if (newMovieIds.isEmpty()) {
            addError(errors, sourceLabel + "前 " + (maxPage * 20) + " 部影片均已在本地，无可添加的新影片");
        }
        result.setErrors(errors);

        if (imported > 0) {
            adminStatisticsService.clearDashboardCache();
            movieCacheService.evictMovieCaches();
        }
        return result;
    }

    @Override
    public TmdbImportResultDTO refreshExistingMovies(Integer limit) {
        int target = normalizeLimit(limit);
        ensureConfigured();

        // 查询本地最久未更新的 N 部有 tmdb_id 的影片
        List<Long> tmdbIds = jdbcTemplate.queryForList(
                "SELECT tmdb_id FROM movie WHERE tmdb_id IS NOT NULL AND deleted = 0 ORDER BY update_time ASC LIMIT ?",
                Long.class,
                target
        );

        if (tmdbIds.isEmpty()) {
            TmdbImportResultDTO result = new TmdbImportResultDTO();
            result.setMode("refresh");
            result.setRequested(target);
            result.setImported(0);
            result.setUpdated(0);
            result.setSkipped(0);
            result.setSource("本地影片刷新");
            result.setErrors(List.of("本地没有含 TMDB ID 的影片可刷新"));
            return result;
        }

        List<FetchedMovie> fetchedMovies = fetchMovieDetails(tmdbIds);
        List<String> errors = new ArrayList<>();
        int updated = 0;
        int skipped = 0;
        for (FetchedMovie fetched : fetchedMovies) {
            if (fetched.detail == null || fetched.detail.path("id").isMissingNode()) {
                skipped++;
                addError(errors, "TMDB " + fetched.tmdbId + "：" + fetched.error);
                continue;
            }
            try {
                upsertMovie(fetched.detail);
                updated++;
            } catch (Exception exception) {
                skipped++;
                addError(errors, "TMDB " + fetched.tmdbId + "：刷新失败 - " + rootMessage(exception));
            }
        }

        TmdbImportResultDTO result = new TmdbImportResultDTO();
        result.setMode("refresh");
        result.setRequested(target);
        result.setImported(0);
        result.setUpdated(updated);
        result.setSkipped(skipped);
        result.setSource("本地影片刷新");
        result.setErrors(errors);

        if (updated > 0) {
            adminStatisticsService.clearDashboardCache();
            movieCacheService.evictMovieCaches();
        }
        return result;
    }
    @Override
    public List<Map<String,Object>> searchMovies(String query, Integer page) {
        ensureConfigured();
        if (!StringUtils.hasText(query)) throw new IllegalArgumentException("请输入影片名称");
        int safePage = Math.max(1, page == null ? 1 : page);
        JsonNode payload = fetchJson("/search/movie?language=zh-CN&include_adult=false&page=" + safePage
                + "&query=" + URLEncoder.encode(query.trim(), StandardCharsets.UTF_8));
        Set<Long> existing = new LinkedHashSet<>(jdbcTemplate.queryForList(
                "SELECT tmdb_id FROM movie WHERE tmdb_id IS NOT NULL AND deleted=0", Long.class));
        List<Map<String,Object>> result = new ArrayList<>();
        for (JsonNode item : payload.path("results")) {
            long id=item.path("id").asLong();if(id<=0)continue;
            Map<String,Object> row=new LinkedHashMap<>();row.put("tmdbId",id);row.put("title",item.path("title").asText(""));
            row.put("originalTitle",item.path("original_title").asText(""));row.put("overview",item.path("overview").asText(""));
            row.put("releaseDate",item.path("release_date").asText(""));row.put("rating",item.path("vote_average").asDouble(0));
            row.put("posterUrl",imageUrl(item.path("poster_path").asText(null)));row.put("imported",existing.contains(id));result.add(row);
        }
        return result;
    }

    @Override
    public TmdbImportResultDTO importMovie(Long tmdbId) {
        ensureConfigured();
        if (tmdbId == null || tmdbId <= 0) throw new IllegalArgumentException("TMDB ID 不正确");
        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM movie WHERE tmdb_id=? AND deleted=0", Long.class, tmdbId) > 0)
            throw new IllegalArgumentException("该 TMDB 影片已导入");
        JsonNode detail=fetchJson("/movie/"+tmdbId+"?append_to_response=credits,release_dates,keywords,watch/providers&language=zh-CN");
        upsertMovie(detail);adminStatisticsService.clearDashboardCache();movieCacheService.evictMovieCaches();
        TmdbImportResultDTO r=new TmdbImportResultDTO();r.setMode("single");r.setRequested(1);r.setImported(1);r.setUpdated(0);r.setSkipped(0);r.setSource("TMDB movie "+tmdbId);r.setErrors(List.of());return r;
    }
    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return 50;
        }
        return Math.max(1, Math.min(limit, 100));
    }

    private void ensureConfigured() {
        if (!StringUtils.hasText(tmdbProperties.getReadAccessToken())) {
            throw new ResponseStatusException(PRECONDITION_FAILED, "TMDB 读访问令牌未配置，请在本地配置文件或环境变量中设置");
        }
    }

    private List<Long> fetchPopularMovieIds(int target) {
        int pages = (int) Math.ceil(target / 20.0);
        Set<Long> ids = new LinkedHashSet<>();
        for (int page = 1; page <= pages; page++) {
            JsonNode payload = fetchJson("/movie/popular?language=zh-CN&page=" + page);
            for (JsonNode item : payload.path("results")) {
                if (ids.size() >= target) {
                    break;
                }
                ids.add(item.path("id").asLong());
            }
        }
        return new ArrayList<>(ids);
    }

    private List<FetchedMovie> fetchMovieDetails(List<Long> movieIds) {
        List<CompletableFuture<FetchedMovie>> futures = movieIds.stream()
                .map(movieId -> CompletableFuture.supplyAsync(() -> {
                    try {
                        JsonNode detail = fetchJson("/movie/" + movieId
                                + "?append_to_response=credits,release_dates,keywords,watch/providers&language=zh-CN");
                        return new FetchedMovie(movieId, detail, null);
                    } catch (Exception exception) {
                        return new FetchedMovie(movieId, null, rootMessage(exception));
                    }
                }, tmdbFetchExecutor))
                .toList();
        return futures.stream().map(CompletableFuture::join).toList();
    }
    private JsonNode fetchJson(String pathAndQuery) {
        return tmdbApiClient.get(pathAndQuery);
    }

    private void upsertMovie(JsonNode detail) {
        Long tmdbId = detail.path("id").asLong();
        String title = textOrNull(detail, "title");
        String originalTitle = textOrNull(detail, "original_title");
        String overview = textOrNull(detail, "overview");
        if (!StringUtils.hasText(overview)) {
            try {
                JsonNode enDetail = fetchJson("/movie/" + tmdbId + "?language=en-US");
                if (enDetail != null && !enDetail.path("overview").isMissingNode()) {
                    String enOverview = enDetail.path("overview").asText();
                    if (StringUtils.hasText(enOverview)) {
                        overview = enOverview;
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to fetch English overview fallback for movie: " + tmdbId + ", error: " + e.getMessage());
            }
        }
        String posterUrl = imageUrl(textOrNull(detail, "poster_path"));
        String backdropUrl = imageUrl(textOrNull(detail, "backdrop_path"));
        LocalDate releaseDate = parseDate(textOrNull(detail, "release_date"));
        Integer runtime = detail.path("runtime").isNumber() ? detail.path("runtime").asInt() : null;
        String region = extractRegion(detail.path("production_countries"));
        String originalLanguage = textOrNull(detail, "original_language");
        String language = extractLanguages(detail.path("spoken_languages"), originalLanguage);
        String certification = extractCertification(detail.path("release_dates").path("results"));
        String productionCompanies = extractNames(detail.path("production_companies"));
        String productionCountries = extractNames(detail.path("production_countries"));
        String collectionName = textOrNull(detail.path("belongs_to_collection"), "name");
        String releaseStatus = textOrNull(detail, "status");
        String tagline = textOrNull(detail, "tagline");
        String keywords = extractNames(detail.path("keywords").path("keywords"));
        BigDecimal rating = decimal(detail.path("vote_average").asDouble(0));
        int voteCount = detail.path("vote_count").asInt(0);
        int viewCount = Math.max(0, (int) Math.round(detail.path("popularity").asDouble(0) * 100));

        jdbcTemplate.update("""
                INSERT INTO movie (
                    tmdb_id, title, original_title, overview, poster_url, backdrop_url,
                    release_date, runtime, region, language, original_language, certification,
                    production_companies, production_countries, collection_name, release_status,
                    tagline, keywords, average_rating, tmdb_rating, favorite_count, view_count,
                    status, deleted, create_time, update_time
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 0, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    title = VALUES(title),
                    original_title = VALUES(original_title),
                    overview = VALUES(overview),
                    poster_url = VALUES(poster_url),
                    backdrop_url = VALUES(backdrop_url),
                    release_date = VALUES(release_date),
                    runtime = VALUES(runtime),
                    region = VALUES(region),
                    language = VALUES(language),
                    original_language = VALUES(original_language),
                    certification = VALUES(certification),
                    production_companies = VALUES(production_companies),
                    production_countries = VALUES(production_countries),
                    collection_name = VALUES(collection_name),
                    release_status = VALUES(release_status),
                    tagline = VALUES(tagline),
                    keywords = VALUES(keywords),
                    average_rating = VALUES(average_rating),
                    tmdb_rating = VALUES(tmdb_rating),
                    favorite_count = VALUES(favorite_count),
                    view_count = VALUES(view_count),
                    status = VALUES(status),
                    deleted = VALUES(deleted),
                    update_time = NOW()
                """,
                tmdbId,
                title,
                originalTitle,
                overview,
                posterUrl,
                backdropUrl,
                releaseDate == null ? null : Date.valueOf(releaseDate),
                runtime,
                region,
                language,
                originalLanguage,
                certification,
                productionCompanies,
                productionCountries,
                collectionName,
                releaseStatus,
                tagline,
                keywords,
                rating,
                rating,
                voteCount,
                viewCount
        );
        Long localMovieId = jdbcTemplate.queryForObject("SELECT id FROM movie WHERE tmdb_id = ?", Long.class, tmdbId);
        if (localMovieId == null) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "影片写入数据库后未能读取本地 ID");
        }

        jdbcTemplate.update("DELETE FROM movie_category WHERE movie_id = ?", localMovieId);
        for (JsonNode genreNode : detail.path("genres")) {
            String genreName = genreNode.path("name").asText(null);
            if (!StringUtils.hasText(genreName)) {
                continue;
            }
            jdbcTemplate.update("""
                    INSERT INTO category (name, description, status, deleted, create_time, update_time)
                    VALUES (?, ?, 1, 0, NOW(), NOW())
                    ON DUPLICATE KEY UPDATE description = VALUES(description), status = 1, deleted = 0, update_time = NOW()
                    """, genreName, "从 TMDB 自动同步的影片类型");
            Long categoryId = jdbcTemplate.queryForObject("SELECT id FROM category WHERE name = ?", Long.class, genreName);
            if (categoryId != null) {
                jdbcTemplate.update("INSERT IGNORE INTO movie_category (movie_id, category_id) VALUES (?, ?)", localMovieId, categoryId);
            }
        }

        syncCredits(localMovieId, detail.path("credits"));
        syncWatchProviders(localMovieId, detail.path("watch/providers").path("results"));
    }

    private void syncWatchProviders(Long movieId, JsonNode providerResults) {
        jdbcTemplate.update("DELETE FROM movie_watch_provider WHERE movie_id = ?", movieId);
        if (providerResults == null || !providerResults.isObject()) {
            return;
        }

        String region = hasWatchProviders(providerResults.path("CN"))
                ? "CN"
                : hasWatchProviders(providerResults.path("US")) ? "US" : null;
        if (region == null) {
            return;
        }

        JsonNode regionProviders = providerResults.path(region);
        for (String accessType : List.of("flatrate", "free", "ads", "rent", "buy")) {
            for (JsonNode providerNode : regionProviders.path(accessType)) {
                long providerId = providerNode.path("provider_id").asLong(0);
                String providerName = textOrNull(providerNode, "provider_name");
                if (providerId == 0 || !StringUtils.hasText(providerName)) {
                    continue;
                }
                jdbcTemplate.update("""
                        INSERT IGNORE INTO movie_watch_provider (
                            movie_id, region, provider_id, provider_name, logo_url, access_type, display_priority
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                        movieId,
                        region,
                        providerId,
                        providerName,
                        imageUrl(textOrNull(providerNode, "logo_path")),
                        accessType,
                        providerNode.path("display_priority").asInt(999)
                );
            }
        }
    }

    private boolean hasWatchProviders(JsonNode regionProviders) {
        if (regionProviders == null || !regionProviders.isObject()) {
            return false;
        }
        for (String accessType : List.of("flatrate", "free", "ads", "rent", "buy")) {
            if (regionProviders.path(accessType).isArray() && !regionProviders.path(accessType).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void syncCredits(Long movieId, JsonNode credits) {
        jdbcTemplate.update("DELETE FROM movie_actor WHERE movie_id = ?", movieId);
        jdbcTemplate.update("DELETE FROM movie_director WHERE movie_id = ?", movieId);

        int castCount = 0;
        for (JsonNode actorNode : credits.path("cast")) {
            if (castCount >= 12) {
                break;
            }
            Long actorId = upsertActor(actorNode);
            if (actorId != null) {
                jdbcTemplate.update("INSERT IGNORE INTO movie_actor (movie_id, actor_id, role_name) VALUES (?, ?, ?)",
                        movieId,
                        actorId,
                        textOrNull(actorNode, "character"));
                castCount++;
            }
        }

        for (JsonNode crewNode : credits.path("crew")) {
            if (!"Director".equalsIgnoreCase(textOrNull(crewNode, "job"))) {
                continue;
            }
            Long directorId = upsertDirector(crewNode);
            if (directorId != null) {
                jdbcTemplate.update("INSERT IGNORE INTO movie_director (movie_id, director_id) VALUES (?, ?)", movieId, directorId);
            }
        }
    }

    private Long upsertActor(JsonNode actorNode) {
        Long tmdbPersonId = actorNode.path("id").asLong();
        if (tmdbPersonId == 0) {
            return null;
        }
        jdbcTemplate.update("""
                INSERT INTO actor (tmdb_id, name, original_name, gender, profile_url, deleted, create_time, update_time)
                VALUES (?, ?, ?, ?, ?, 0, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    name = VALUES(name),
                    original_name = VALUES(original_name),
                    gender = VALUES(gender),
                    profile_url = VALUES(profile_url),
                    deleted = 0,
                    update_time = NOW()
                """,
                tmdbPersonId,
                textOrNull(actorNode, "name"),
                textOrNull(actorNode, "original_name"),
                normalizeGender(actorNode.path("gender").asInt()),
                imageUrl(textOrNull(actorNode, "profile_path"))
        );
        return jdbcTemplate.queryForObject("SELECT id FROM actor WHERE tmdb_id = ?", Long.class, tmdbPersonId);
    }

    private Long upsertDirector(JsonNode directorNode) {
        Long tmdbPersonId = directorNode.path("id").asLong();
        if (tmdbPersonId == 0) {
            return null;
        }
        jdbcTemplate.update("""
                INSERT INTO director (tmdb_id, name, original_name, gender, profile_url, deleted, create_time, update_time)
                VALUES (?, ?, ?, ?, ?, 0, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    name = VALUES(name),
                    original_name = VALUES(original_name),
                    gender = VALUES(gender),
                    profile_url = VALUES(profile_url),
                    deleted = 0,
                    update_time = NOW()
                """,
                tmdbPersonId,
                textOrNull(directorNode, "name"),
                textOrNull(directorNode, "original_name"),
                normalizeGender(directorNode.path("gender").asInt()),
                imageUrl(textOrNull(directorNode, "profile_path"))
        );
        return jdbcTemplate.queryForObject("SELECT id FROM director WHERE tmdb_id = ?", Long.class, tmdbPersonId);
    }

    private String normalizeGender(int gender) {
        return switch (gender) {
            case 1 -> "女";
            case 2 -> "男";
            case 3 -> "非二元";
            default -> null;
        };
    }

    private String extractCertification(JsonNode releaseDateResults) {
        for (String preferredRegion : List.of("CN", "US")) {
            for (JsonNode regionNode : releaseDateResults) {
                if (!preferredRegion.equals(regionNode.path("iso_3166_1").asText())) {
                    continue;
                }
                String certification = certificationFromReleases(regionNode.path("release_dates"));
                if (StringUtils.hasText(certification)) {
                    return certification;
                }
            }
        }
        return null;
    }

    private String certificationFromReleases(JsonNode releases) {
        for (int preferredType : List.of(3, 2, 4, 1, 5, 6)) {
            for (JsonNode releaseNode : releases) {
                if (releaseNode.path("type").asInt() != preferredType) {
                    continue;
                }
                String certification = textOrNull(releaseNode, "certification");
                if (StringUtils.hasText(certification)) {
                    return certification;
                }
            }
        }
        return null;
    }

    private String extractNames(JsonNode nodes) {
        if (nodes == null || !nodes.isArray()) {
            return null;
        }
        Set<String> names = new LinkedHashSet<>();
        for (JsonNode node : nodes) {
            String name = textOrNull(node, "name");
            if (StringUtils.hasText(name)) {
                names.add(name);
            }
        }
        return names.isEmpty() ? null : String.join(",", names);
    }

    private String extractLanguages(JsonNode spokenLanguages, String originalLanguage) {
        Set<String> languages = new LinkedHashSet<>();
        if (StringUtils.hasText(originalLanguage)) {
            languages.add(originalLanguage);
        }
        if (spokenLanguages != null && spokenLanguages.isArray()) {
            for (JsonNode languageNode : spokenLanguages) {
                String code = textOrNull(languageNode, "iso_639_1");
                if (StringUtils.hasText(code)) {
                    languages.add(code);
                }
            }
        }
        return languages.isEmpty() ? null : String.join(",", languages);
    }

    private String extractRegion(JsonNode countries) {
        if (countries == null || !countries.isArray() || countries.isEmpty()) {
            return null;
        }
        String name = countries.get(0).path("name").asText(null);
        return StringUtils.hasText(name) ? name : null;
    }

    private String imageUrl(String path) {
        if (!StringUtils.hasText(path)) {
            return null;
        }
        return tmdbProperties.getImageBaseUrl() + path;
    }

    private LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private String textOrNull(JsonNode node, String field) {
        String value = node.path(field).asText(null);
        return StringUtils.hasText(value) ? value : null;
    }

    private BigDecimal decimal(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP);
    }

    private void addError(List<String> errors, String message) {
        if (errors.size() < 5) errors.add(message);
    }

    private String rootMessage(Exception exception) {
        if (exception instanceof ResponseStatusException response && StringUtils.hasText(response.getReason())) {
            return response.getReason();
        }
        Throwable current = exception;
        while (current.getCause() != null) current = current.getCause();
        return StringUtils.hasText(current.getMessage()) ? current.getMessage() : current.getClass().getSimpleName();
    }

    @PreDestroy
    void shutdownExecutor() {
        tmdbFetchExecutor.shutdownNow();
    }

    private record FetchedMovie(Long tmdbId, JsonNode detail, String error) {}
}