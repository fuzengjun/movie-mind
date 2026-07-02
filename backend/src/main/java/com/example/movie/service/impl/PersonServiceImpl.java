package com.example.movie.service.impl;

import com.example.movie.config.TmdbProperties;
import com.example.movie.service.PersonService;
import com.example.movie.service.TmdbApiClient;
import com.example.movie.vo.PersonDetailVO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final JdbcTemplate jdbcTemplate;
    private final TmdbProperties tmdbProperties;
    private final TmdbApiClient tmdbApiClient;
    private static final long DETAIL_CACHE_TTL_MILLIS = 10 * 60 * 1000L;
    private final Map<String, CachedPersonDetail> detailCache = new ConcurrentHashMap<>();
    private final Set<String> profileBackfillsInFlight = ConcurrentHashMap.newKeySet();
    private final ExecutorService profileExecutor = Executors.newFixedThreadPool(3, runnable -> {
        Thread thread = new Thread(runnable, "person-profile-backfill");
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public PersonDetailVO getPersonDetail(String type, Long id) {
        String normalizedType = normalizeType(type);
        String cacheKey = normalizedType + ":" + id;
        CachedPersonDetail cached = detailCache.get(cacheKey);
        if (cached != null && cached.expiresAt > System.currentTimeMillis()) {
            return cached.detail;
        }
        if (cached != null) detailCache.remove(cacheKey);

        PersonRecord primary = loadPerson(normalizedType, id);
        if (primary == null) {
            return null;
        }


        Long actorId = "actor".equals(normalizedType) ? primary.id : findMirrorPersonId("actor", primary.tmdbId, primary.name);
        Long directorId = "director".equals(normalizedType) ? primary.id : findMirrorPersonId("director", primary.tmdbId, primary.name);

        List<PersonDetailVO.WorkSectionVO> sections = new ArrayList<>();
        Set<Long> uniqueMovieIds = new LinkedHashSet<>();
        String backdropUrl = null;

        if (actorId != null) {
            List<PersonDetailVO.WorkMovieVO> movies = loadActorWorks(actorId);
            if (!movies.isEmpty()) {
                PersonDetailVO.WorkSectionVO section = new PersonDetailVO.WorkSectionVO();
                section.setKey("acting");
                section.setTitle("参演作品");
                section.setMovies(movies);
                sections.add(section);
                movies.forEach(movie -> uniqueMovieIds.add(movie.getId()));
                backdropUrl = firstBackdrop(backdropUrl, movies);
            }
        }

        if (directorId != null) {
            List<PersonDetailVO.WorkMovieVO> movies = loadDirectorWorks(directorId);
            if (!movies.isEmpty()) {
                PersonDetailVO.WorkSectionVO section = new PersonDetailVO.WorkSectionVO();
                section.setKey("directing");
                section.setTitle("导演作品");
                section.setMovies(movies);
                sections.add(section);
                movies.forEach(movie -> uniqueMovieIds.add(movie.getId()));
                backdropUrl = firstBackdrop(backdropUrl, movies);
            }
        }

        PersonDetailVO detail = new PersonDetailVO();
        detail.setId(primary.id);
        detail.setType(normalizedType);
        detail.setTypeLabel("actor".equals(normalizedType) ? "演员" : "导演");
        detail.setName(primary.name);
        detail.setOriginalName(primary.originalName);
        detail.setProfileUrl(primary.profileUrl);
        detail.setGender(primary.gender);
        detail.setBirthday(primary.birthday);
        detail.setNationality(primary.nationality);
        detail.setBiography(buildBiography(primary, uniqueMovieIds.size()));
        detail.setMovieCount(uniqueMovieIds.size());
        detail.setBackdropUrl(backdropUrl);
        detail.setSections(sections);
        detailCache.put(cacheKey, new CachedPersonDetail(detail, System.currentTimeMillis() + DETAIL_CACHE_TTL_MILLIS));
        schedulePersonProfileBackfill(normalizedType, primary, cacheKey);
        return detail;
    }

    private String normalizeType(String type) {
        if ("actor".equalsIgnoreCase(type)) {
            return "actor";
        }
        if ("director".equalsIgnoreCase(type)) {
            return "director";
        }
        throw new ResponseStatusException(BAD_REQUEST, "不支持的人物类型");
    }

    private PersonRecord loadPerson(String type, Long id) {
        String table = "actor".equals(type) ? "actor" : "director";
        String sql = "SELECT id, tmdb_id, name, original_name, gender, profile_url, birthday, nationality, biography, profile_sync_time FROM " + table + " WHERE deleted = 0 AND id = ?";
        List<PersonRecord> rows = jdbcTemplate.query(sql, (rs, rowNum) -> mapPersonRecord(rs), id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private PersonRecord mapPersonRecord(ResultSet rs) throws SQLException {
        PersonRecord record = new PersonRecord();
        record.id = rs.getLong("id");
        record.tmdbId = rs.getObject("tmdb_id", Long.class);
        record.name = rs.getString("name");
        record.originalName = rs.getString("original_name");
        record.gender = rs.getString("gender");
        record.profileUrl = rs.getString("profile_url");
        Date birthday = rs.getDate("birthday");
        record.birthday = birthday == null ? null : birthday.toLocalDate();
        record.nationality = rs.getString("nationality");
        record.biography = rs.getString("biography");
        java.sql.Timestamp profileSyncTime = rs.getTimestamp("profile_sync_time");
        record.profileSyncTime = profileSyncTime == null ? null : profileSyncTime.toLocalDateTime();
        return record;
    }

    private Long findMirrorPersonId(String targetType, Long tmdbId, String name) {
        String table = "actor".equals(targetType) ? "actor" : "director";
        if (tmdbId != null) {
            List<Long> ids = jdbcTemplate.query(
                    "SELECT id FROM " + table + " WHERE deleted = 0 AND tmdb_id = ?",
                    (rs, rowNum) -> rs.getLong("id"),
                    tmdbId
            );
            if (!ids.isEmpty()) {
                return ids.get(0);
            }
        }
        if (!StringUtils.hasText(name)) {
            return null;
        }
        List<Long> ids = jdbcTemplate.query(
                "SELECT id FROM " + table + " WHERE deleted = 0 AND name = ?",
                (rs, rowNum) -> rs.getLong("id"),
                name
        );
        return ids.isEmpty() ? null : ids.get(0);
    }

    private List<PersonDetailVO.WorkMovieVO> loadActorWorks(Long actorId) {
        String sql = """
                SELECT m.id,
                       m.title,
                       m.poster_url,
                       m.backdrop_url,
                       m.release_date,
                       m.average_rating,
                       ma.role_name,
                       GROUP_CONCAT(DISTINCT c.name ORDER BY c.name SEPARATOR ',') AS categories
                FROM movie_actor ma
                INNER JOIN movie m ON m.id = ma.movie_id AND m.deleted = 0 AND m.status = 1
                LEFT JOIN movie_category mc ON mc.movie_id = m.id
                LEFT JOIN category c ON c.id = mc.category_id AND c.deleted = 0
                WHERE ma.actor_id = ?
                GROUP BY m.id, ma.role_name
                ORDER BY m.release_date IS NULL, m.release_date DESC, m.view_count DESC, m.id DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapWorkMovie(rs), actorId);
    }

    private List<PersonDetailVO.WorkMovieVO> loadDirectorWorks(Long directorId) {
        String sql = """
                SELECT m.id,
                       m.title,
                       m.poster_url,
                       m.backdrop_url,
                       m.release_date,
                       m.average_rating,
                       '导演' AS role_name,
                       GROUP_CONCAT(DISTINCT c.name ORDER BY c.name SEPARATOR ',') AS categories
                FROM movie_director md
                INNER JOIN movie m ON m.id = md.movie_id AND m.deleted = 0 AND m.status = 1
                LEFT JOIN movie_category mc ON mc.movie_id = m.id
                LEFT JOIN category c ON c.id = mc.category_id AND c.deleted = 0
                WHERE md.director_id = ?
                GROUP BY m.id
                ORDER BY m.release_date IS NULL, m.release_date DESC, m.view_count DESC, m.id DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapWorkMovie(rs), directorId);
    }

    private PersonDetailVO.WorkMovieVO mapWorkMovie(ResultSet rs) throws SQLException {
        PersonDetailVO.WorkMovieVO movie = new PersonDetailVO.WorkMovieVO();
        movie.setId(rs.getLong("id"));
        movie.setTitle(rs.getString("title"));
        movie.setPosterUrl(rs.getString("poster_url"));
        movie.setBackdropUrl(rs.getString("backdrop_url"));
        Date releaseDate = rs.getDate("release_date");
        movie.setReleaseDate(releaseDate == null ? null : releaseDate.toLocalDate());
        movie.setAverageRating(rs.getBigDecimal("average_rating"));
        movie.setRoleName(rs.getString("role_name"));
        movie.setCategories(parseCategories(rs.getString("categories")));
        return movie;
    }

    private List<String> parseCategories(String categories) {
        if (!StringUtils.hasText(categories)) {
            return List.of();
        }
        return List.of(categories.split(","));
    }

    private void schedulePersonProfileBackfill(String type, PersonRecord record, String cacheKey) {
        boolean needsProfile = !StringUtils.hasText(record.biography)
                || record.birthday == null
                || !StringUtils.hasText(record.nationality);
        boolean recentlySynced = record.profileSyncTime != null
                && record.profileSyncTime.isAfter(LocalDateTime.now().minusDays(1));
        if (!needsProfile || recentlySynced || record.tmdbId == null
                || !StringUtils.hasText(tmdbProperties.getReadAccessToken())) {
            return;
        }
        if (!profileBackfillsInFlight.add(cacheKey)) return;
        profileExecutor.submit(() -> {
            try {
                maybeBackfillPersonProfile(type, record);
                detailCache.remove(cacheKey);
            } finally {
                profileBackfillsInFlight.remove(cacheKey);
            }
        });
    }

    private void maybeBackfillPersonProfile(String type, PersonRecord record) {
        if (record.tmdbId == null || !StringUtils.hasText(tmdbProperties.getReadAccessToken())) {
            return;
        }
        boolean needsBiography = !StringUtils.hasText(record.biography);
        boolean needsBirthday = record.birthday == null;
        boolean needsLocation = !StringUtils.hasText(record.nationality);
        if (!needsBiography && !needsBirthday && !needsLocation) {
            return;
        }
        if (record.profileSyncTime != null && record.profileSyncTime.isAfter(LocalDateTime.now().minusDays(1))) {
            return;
        }

        String table = "actor".equals(type) ? "actor" : "director";
        try {
            JsonNode detail = fetchPersonJson(record.tmdbId, "zh-CN");
            if (detail == null) {
                return;
            }

            String biography = textOrNull(detail, "biography");
            if (!StringUtils.hasText(biography)) {
                JsonNode fallback = fetchPersonJson(record.tmdbId, "en-US");
                biography = textOrNull(fallback, "biography");
            }

            LocalDate birthday = parseDate(textOrNull(detail, "birthday"));
            String placeOfBirth = textOrNull(detail, "place_of_birth");
            jdbcTemplate.update(
                    "UPDATE " + table + " SET biography = ?, birthday = ?, nationality = ?, update_time = NOW() WHERE id = ?",
                    StringUtils.hasText(record.biography) ? record.biography : biography,
                    record.birthday != null ? Date.valueOf(record.birthday) : birthday == null ? null : Date.valueOf(birthday),
                    StringUtils.hasText(record.nationality) ? record.nationality : placeOfBirth,
                    record.id
            );
        } catch (Exception exception) {
            System.err.println("Person profile backfill skipped: " + exception.getMessage());
        } finally {
            jdbcTemplate.update("UPDATE " + table + " SET profile_sync_time = NOW() WHERE id = ?", record.id);
        }
    }

    private JsonNode fetchPersonJson(Long tmdbPersonId, String language) {
        return tmdbApiClient.get("/person/" + tmdbPersonId + "?language=" + language);
    }

    private LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private String textOrNull(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        String value = node.path(field).asText(null);
        return StringUtils.hasText(value) ? value : null;
    }

    private String buildBiography(PersonRecord person, int movieCount) {
        if (StringUtils.hasText(person.biography)) {
            return person.biography;
        }
        List<String> fragments = new ArrayList<>();
        fragments.add(person.name + " 是系统片库中已收录的电影从业者。");
        if (person.birthday != null) {
            fragments.add("出生日期：" + person.birthday + "。");
        }
        if (StringUtils.hasText(person.nationality)) {
            fragments.add("相关地区：" + person.nationality + "。");
        }
        if (movieCount > 0) {
            fragments.add("当前已收录 " + movieCount + " 部相关作品。");
        }
        return String.join(" ", fragments);
    }

    private String firstBackdrop(String current, List<PersonDetailVO.WorkMovieVO> movies) {
        if (StringUtils.hasText(current)) {
            return current;
        }
        for (PersonDetailVO.WorkMovieVO movie : movies) {
            if (StringUtils.hasText(movie.getBackdropUrl())) {
                return movie.getBackdropUrl();
            }
        }
        return current;
    }

    private static class PersonRecord {
        private Long id;
        private Long tmdbId;
        private String name;
        private String originalName;
        private String gender;
        private String profileUrl;
        private LocalDate birthday;
        private String nationality;
        private String biography;
        private LocalDateTime profileSyncTime;
    }

    private record CachedPersonDetail(PersonDetailVO detail, long expiresAt) {
    }
}