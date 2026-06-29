package com.example.movie.service.impl;

import com.example.movie.config.TmdbProperties;
import com.example.movie.dto.admin.TmdbImportResultDTO;
import com.example.movie.service.TmdbImportService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;

@Service
@RequiredArgsConstructor
public class TmdbImportServiceImpl implements TmdbImportService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final TmdbProperties tmdbProperties;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public TmdbImportResultDTO importPopularMovies(Integer limit) {
        int target = normalizeLimit(limit);
        ensureConfigured();

        List<Long> movieIds = fetchPopularMovieIds(target);
        int imported = 0;
        int skipped = 0;
        for (Long movieId : movieIds) {
            JsonNode detail = fetchJson("/movie/" + movieId + "?language=zh-CN");
            if (detail == null || detail.path("id").isMissingNode()) {
                skipped++;
                continue;
            }
            upsertMovie(detail);
            imported++;
        }

        TmdbImportResultDTO result = new TmdbImportResultDTO();
        result.setRequested(target);
        result.setImported(imported);
        result.setSkipped(skipped);
        result.setSource("TMDB popular movies");
        return result;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return 50;
        }
        return Math.max(1, Math.min(limit, 50));
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

    private JsonNode fetchJson(String pathAndQuery) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tmdbProperties.getBaseUrl() + pathAndQuery))
                    .header("Authorization", "Bearer " + tmdbProperties.getReadAccessToken())
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(BAD_GATEWAY, "TMDB 请求失败，状态码: " + response.statusCode());
            }
            return objectMapper.readTree(response.body());
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(BAD_GATEWAY, "TMDB 请求异常: " + exception.getMessage(), exception);
        }
    }

    private void upsertMovie(JsonNode detail) {
        Long tmdbId = detail.path("id").asLong();
        String title = textOrNull(detail, "title");
        String originalTitle = textOrNull(detail, "original_title");
        String overview = textOrNull(detail, "overview");
        String posterUrl = imageUrl(textOrNull(detail, "poster_path"));
        String backdropUrl = imageUrl(textOrNull(detail, "backdrop_path"));
        LocalDate releaseDate = parseDate(textOrNull(detail, "release_date"));
        Integer runtime = detail.path("runtime").isNumber() ? detail.path("runtime").asInt() : null;
        String region = extractRegion(detail.path("production_countries"));
        String language = textOrNull(detail, "original_language");
        BigDecimal rating = decimal(detail.path("vote_average").asDouble(0));
        int voteCount = detail.path("vote_count").asInt(0);
        int viewCount = Math.max(0, (int) Math.round(detail.path("popularity").asDouble(0) * 100));

        jdbcTemplate.update("""
                INSERT INTO movie (
                    tmdb_id, title, original_title, overview, poster_url, backdrop_url,
                    release_date, runtime, region, language, average_rating, tmdb_rating,
                    favorite_count, view_count, status, deleted, create_time, update_time
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 0, NOW(), NOW())
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
}
