package com.example.movie.service.impl;

import com.example.movie.dto.admin.AdminMovieListItemDTO;
import com.example.movie.dto.admin.AdminUserListItemDTO;
import com.example.movie.dto.admin.DashboardStatisticsDTO;
import com.example.movie.service.AdminStatisticsService;
import com.example.movie.utils.DashboardRangeResolver;
import com.example.movie.utils.RedisKeys;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStatisticsServiceImpl implements AdminStatisticsService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider;

    @Value("${app.cache.redis.enabled:false}")
    private boolean redisCacheEnabled;

    @Override
    public DashboardStatisticsDTO getDashboardStatistics(Integer range) {
        int rangeDays = DashboardRangeResolver.normalize(range);
        String cacheKey = RedisKeys.STATISTICS_DASHBOARD + ":" + rangeDays;
        DashboardStatisticsDTO cached = readCache(cacheKey);
        if (cached != null) {
            return cached;
        }

        DashboardStatisticsDTO statistics = new DashboardStatisticsDTO();
        statistics.setRangeDays(rangeDays);
        statistics.setGeneratedAt(LocalDateTime.now());
        statistics.setSummary(buildSummary());
        statistics.setUserGrowth(buildUserGrowth(rangeDays));
        statistics.setDailyActivity(buildDailyActivity(rangeDays));
        statistics.setCategoryDistribution(buildCategoryDistribution());
        statistics.setRatingDistribution(buildRatingDistribution());
        statistics.setYearDistribution(buildYearDistribution());
        statistics.setRegionDistribution(buildRegionDistribution());
        statistics.setHotMovies(buildHotMovies());
        statistics.setFavoriteRanking(buildRanking("favorite_count", "收藏"));
        statistics.setViewRanking(buildRanking("view_count", "浏览"));
        writeCache(cacheKey, statistics);
        return statistics;
    }

    @Override
    public List<AdminMovieListItemDTO> listAdminMovies() {
        String sql = """
                SELECT m.id,
                       m.title,
                       m.poster_url,
                       m.region,
                       m.release_date,
                       m.average_rating,
                       m.favorite_count,
                       m.view_count,
                       m.status,
                       GROUP_CONCAT(DISTINCT c.name ORDER BY c.name SEPARATOR ',') AS categories,
                       GROUP_CONCAT(DISTINCT d.name ORDER BY d.name SEPARATOR ',') AS directors,
                       GROUP_CONCAT(DISTINCT a.name ORDER BY a.name SEPARATOR ',') AS actors
                FROM movie m
                LEFT JOIN movie_category mc ON mc.movie_id = m.id
                LEFT JOIN category c ON c.id = mc.category_id AND c.deleted = 0
                LEFT JOIN movie_director md ON md.movie_id = m.id
                LEFT JOIN director d ON d.id = md.director_id AND d.deleted = 0
                LEFT JOIN movie_actor ma ON ma.movie_id = m.id
                LEFT JOIN actor a ON a.id = ma.actor_id AND a.deleted = 0
                WHERE m.deleted = 0
                GROUP BY m.id
                ORDER BY m.update_time DESC, m.id DESC
                LIMIT 24
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            AdminMovieListItemDTO dto = new AdminMovieListItemDTO();
            dto.setId(rs.getLong("id"));
            dto.setTitle(rs.getString("title"));
            dto.setPosterUrl(rs.getString("poster_url"));
            dto.setRegion(rs.getString("region"));
            dto.setReleaseDate(toLocalDate(rs.getDate("release_date")));
            dto.setAverageRating(rs.getBigDecimal("average_rating"));
            dto.setFavoriteCount(rs.getInt("favorite_count"));
            dto.setViewCount(rs.getInt("view_count"));
            dto.setStatus(rs.getInt("status"));
            dto.setCategories(splitNames(rs.getString("categories")));
            dto.setDirectors(splitNames(rs.getString("directors")));
            dto.setActors(splitNames(rs.getString("actors")));
            return dto;
        });
    }

    @Override
    public List<AdminUserListItemDTO> listAdminUsers() {
        String sql = """
                SELECT u.id,
                       u.username,
                       u.nickname,
                       u.email,
                       u.avatar,
                       u.role,
                       u.status,
                       u.create_time,
                       COALESCE(f.favorite_count, 0) AS favorite_count,
                       COALESCE(c.comment_count, 0) AS comment_count,
                       COALESCE(r.rating_count, 0) AS rating_count
                FROM user u
                LEFT JOIN (
                    SELECT user_id, COUNT(*) AS favorite_count
                    FROM favorite
                    GROUP BY user_id
                ) f ON f.user_id = u.id
                LEFT JOIN (
                    SELECT user_id, COUNT(*) AS comment_count
                    FROM comment
                    WHERE deleted = 0
                    GROUP BY user_id
                ) c ON c.user_id = u.id
                LEFT JOIN (
                    SELECT user_id, COUNT(*) AS rating_count
                    FROM rating
                    GROUP BY user_id
                ) r ON r.user_id = u.id
                WHERE u.deleted = 0
                ORDER BY u.create_time DESC, u.id DESC
                LIMIT 24
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            AdminUserListItemDTO dto = new AdminUserListItemDTO();
            dto.setId(rs.getLong("id"));
            dto.setUsername(rs.getString("username"));
            dto.setNickname(rs.getString("nickname"));
            dto.setEmail(rs.getString("email"));
            dto.setAvatar(rs.getString("avatar"));
            dto.setRole(rs.getString("role"));
            dto.setStatus(rs.getInt("status"));
            dto.setCreateTime(toLocalDateTime(rs.getTimestamp("create_time")));
            dto.setFavoriteCount(rs.getInt("favorite_count"));
            dto.setCommentCount(rs.getInt("comment_count"));
            dto.setRatingCount(rs.getInt("rating_count"));
            return dto;
        });
    }

    private DashboardStatisticsDTO readCache(String cacheKey) {
        StringRedisTemplate stringRedisTemplate = getStringRedisTemplate();
        if (stringRedisTemplate == null) {
            return null;
        }
        try {
            String cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedValue == null || cachedValue.isBlank()) {
                return null;
            }
            return objectMapper.readValue(cachedValue, DashboardStatisticsDTO.class);
        } catch (Exception exception) {
            log.warn("Failed to read dashboard cache: {}", exception.getMessage());
            return null;
        }
    }

    private void writeCache(String cacheKey, DashboardStatisticsDTO statistics) {
        StringRedisTemplate stringRedisTemplate = getStringRedisTemplate();
        if (stringRedisTemplate == null) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(statistics), Duration.ofMinutes(10));
        } catch (DataAccessException | JsonProcessingException exception) {
            log.warn("Failed to write dashboard cache: {}", exception.getMessage());
        }
    }

    private StringRedisTemplate getStringRedisTemplate() {
        if (!redisCacheEnabled) {
            return null;
        }
        return stringRedisTemplateProvider.getIfAvailable();
    }

    private DashboardStatisticsDTO.Summary buildSummary() {
        DashboardStatisticsDTO.Summary summary = new DashboardStatisticsDTO.Summary();
        summary.setTotalUsers(queryForLong("SELECT COUNT(*) FROM user WHERE deleted = 0"));
        summary.setTotalMovies(queryForLong("SELECT COUNT(*) FROM movie WHERE deleted = 0"));
        summary.setTotalComments(queryForLong("SELECT COUNT(*) FROM comment WHERE deleted = 0"));
        summary.setTotalFavorites(queryForLong("SELECT COUNT(*) FROM favorite"));
        summary.setTotalRatings(queryForLong("SELECT COUNT(*) FROM rating"));
        summary.setTodayUsers(queryForLong("SELECT COUNT(*) FROM user WHERE deleted = 0 AND DATE(create_time) = CURDATE()"));
        summary.setTodayComments(queryForLong("SELECT COUNT(*) FROM comment WHERE deleted = 0 AND DATE(create_time) = CURDATE()"));
        return summary;
    }

    private List<DashboardStatisticsDTO.TrendPoint> buildUserGrowth(int rangeDays) {
        LocalDate startDate = LocalDate.now().minusDays(rangeDays - 1L);
        Long baseCount = queryForLong("SELECT COUNT(*) FROM user WHERE deleted = 0 AND DATE(create_time) < ?", Date.valueOf(startDate));
        String sql = """
                SELECT DATE(create_time) AS item_date, COUNT(*) AS value
                FROM user
                WHERE deleted = 0 AND DATE(create_time) >= ?
                GROUP BY DATE(create_time)
                ORDER BY item_date
                """;
        Map<LocalDate, Long> dailyCounts = jdbcTemplate.query(sql, rs -> {
            Map<LocalDate, Long> data = new LinkedHashMap<>();
            while (rs.next()) {
                data.put(toLocalDate(rs.getDate("item_date")), rs.getLong("value"));
            }
            return data;
        }, Date.valueOf(startDate));

        List<DashboardStatisticsDTO.TrendPoint> points = new ArrayList<>();
        long cumulative = baseCount == null ? 0L : baseCount;
        for (LocalDate date : buildDateRange(startDate, LocalDate.now())) {
            cumulative += dailyCounts.getOrDefault(date, 0L);
            DashboardStatisticsDTO.TrendPoint point = new DashboardStatisticsDTO.TrendPoint();
            point.setDate(date);
            point.setValue(cumulative);
            points.add(point);
        }
        return points;
    }

    private List<DashboardStatisticsDTO.ActivityPoint> buildDailyActivity(int rangeDays) {
        LocalDate startDate = LocalDate.now().minusDays(rangeDays - 1L);
        String commentSql = """
                SELECT DATE(create_time) AS item_date, COUNT(*) AS value
                FROM comment
                WHERE deleted = 0 AND DATE(create_time) >= ?
                GROUP BY DATE(create_time)
                """;
        String favoriteSql = """
                SELECT DATE(create_time) AS item_date, COUNT(*) AS value
                FROM favorite
                WHERE DATE(create_time) >= ?
                GROUP BY DATE(create_time)
                """;
        Map<LocalDate, Long> commentMap = queryDateValueMap(commentSql, startDate);
        Map<LocalDate, Long> favoriteMap = queryDateValueMap(favoriteSql, startDate);

        List<DashboardStatisticsDTO.ActivityPoint> points = new ArrayList<>();
        for (LocalDate date : buildDateRange(startDate, LocalDate.now())) {
            DashboardStatisticsDTO.ActivityPoint point = new DashboardStatisticsDTO.ActivityPoint();
            point.setDate(date);
            point.setComments(commentMap.getOrDefault(date, 0L));
            point.setFavorites(favoriteMap.getOrDefault(date, 0L));
            points.add(point);
        }
        return points;
    }

    private List<DashboardStatisticsDTO.NamedValue> buildCategoryDistribution() {
        String sql = """
                SELECT c.name AS item_name, COUNT(m.id) AS value
                FROM category c
                LEFT JOIN movie_category mc ON mc.category_id = c.id
                LEFT JOIN movie m ON m.id = mc.movie_id AND m.deleted = 0
                WHERE c.deleted = 0
                GROUP BY c.id, c.name
                HAVING COUNT(m.id) > 0
                ORDER BY value DESC, c.name ASC
                """;
        return queryNamedValues(sql);
    }

    private List<DashboardStatisticsDTO.NamedValue> buildRatingDistribution() {
        String sql = """
                SELECT CASE
                           WHEN average_rating < 2 THEN '0-2'
                           WHEN average_rating < 4 THEN '2-4'
                           WHEN average_rating < 6 THEN '4-6'
                           WHEN average_rating < 8 THEN '6-8'
                           ELSE '8-10'
                       END AS item_name,
                       COUNT(*) AS value
                FROM movie
                WHERE deleted = 0
                GROUP BY item_name
                ORDER BY item_name
                """;
        return queryNamedValues(sql);
    }

    private List<DashboardStatisticsDTO.NamedValue> buildYearDistribution() {
        String sql = """
                SELECT DATE_FORMAT(release_date, '%Y') AS item_name, COUNT(*) AS value
                FROM movie
                WHERE deleted = 0 AND release_date IS NOT NULL
                GROUP BY DATE_FORMAT(release_date, '%Y')
                ORDER BY item_name DESC
                LIMIT 10
                """;
        return queryNamedValues(sql);
    }

    private List<DashboardStatisticsDTO.NamedValue> buildRegionDistribution() {
        String sql = """
                SELECT COALESCE(NULLIF(region, ''), 'Unknown') AS item_name, COUNT(*) AS value
                FROM movie
                WHERE deleted = 0
                GROUP BY COALESCE(NULLIF(region, ''), 'Unknown')
                ORDER BY value DESC, item_name ASC
                """;
        List<DashboardStatisticsDTO.NamedValue> values = queryNamedValues(sql);
        if (values.size() <= 8) {
            return values;
        }
        List<DashboardStatisticsDTO.NamedValue> topValues = new ArrayList<>(values.subList(0, 8));
        long other = values.subList(8, values.size()).stream().mapToLong(DashboardStatisticsDTO.NamedValue::getValue).sum();
        DashboardStatisticsDTO.NamedValue otherItem = new DashboardStatisticsDTO.NamedValue();
        otherItem.setName("Other");
        otherItem.setValue(other);
        topValues.add(otherItem);
        return topValues;
    }

    private List<DashboardStatisticsDTO.MovieSpotlight> buildHotMovies() {
        String sql = """
                SELECT id, title, poster_url, backdrop_url, average_rating, favorite_count, view_count
                FROM movie
                WHERE deleted = 0
                ORDER BY view_count DESC, favorite_count DESC, average_rating DESC, id DESC
                LIMIT 10
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            DashboardStatisticsDTO.MovieSpotlight movie = new DashboardStatisticsDTO.MovieSpotlight();
            movie.setId(rs.getLong("id"));
            movie.setTitle(rs.getString("title"));
            movie.setPosterUrl(rs.getString("poster_url"));
            movie.setBackdropUrl(rs.getString("backdrop_url"));
            movie.setAverageRating(rs.getBigDecimal("average_rating"));
            movie.setFavoriteCount(rs.getInt("favorite_count"));
            movie.setViewCount(rs.getInt("view_count"));
            return movie;
        });
    }

    private List<DashboardStatisticsDTO.RankingItem> buildRanking(String field, String fallbackName) {
        String sql = """
                SELECT id, title, poster_url, average_rating, %s AS metric_value
                FROM movie
                WHERE deleted = 0
                ORDER BY %s DESC, average_rating DESC, id DESC
                LIMIT 10
                """.formatted(field, field);
        List<DashboardStatisticsDTO.RankingItem> values = jdbcTemplate.query(sql, (rs, rowNum) -> {
            DashboardStatisticsDTO.RankingItem item = new DashboardStatisticsDTO.RankingItem();
            item.setId(rs.getLong("id"));
            item.setTitle(rs.getString("title"));
            item.setPosterUrl(rs.getString("poster_url"));
            item.setAverageRating(rs.getBigDecimal("average_rating"));
            item.setMetricValue(rs.getLong("metric_value"));
            item.setMetricLabel(fallbackName);
            return item;
        });
        if (values.isEmpty()) {
            DashboardStatisticsDTO.RankingItem placeholder = new DashboardStatisticsDTO.RankingItem();
            placeholder.setTitle(fallbackName + "暂无数据");
            placeholder.setMetricLabel(fallbackName);
            placeholder.setMetricValue(0L);
            return List.of(placeholder);
        }
        return values;
    }

    private List<DashboardStatisticsDTO.NamedValue> queryNamedValues(String sql, Object... args) {
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            DashboardStatisticsDTO.NamedValue item = new DashboardStatisticsDTO.NamedValue();
            item.setName(rs.getString("item_name"));
            item.setValue(rs.getLong("value"));
            return item;
        }, args);
    }

    private Map<LocalDate, Long> queryDateValueMap(String sql, LocalDate startDate) {
        return jdbcTemplate.query(sql, rs -> {
            Map<LocalDate, Long> values = new LinkedHashMap<>();
            while (rs.next()) {
                values.put(toLocalDate(rs.getDate("item_date")), rs.getLong("value"));
            }
            return values;
        }, Date.valueOf(startDate));
    }

    private Long queryForLong(String sql, Object... args) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }

    private List<LocalDate> buildDateRange(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dates.add(current);
            current = current.plusDays(1);
        }
        return dates;
    }

    private List<String> splitNames(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return List.of(raw.split(","));
    }

    private LocalDate toLocalDate(Date value) {
        return value == null ? null : value.toLocalDate();
    }

    private LocalDateTime toLocalDateTime(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }

    @Override
    public void clearDashboardCache() {
        StringRedisTemplate stringRedisTemplate = getStringRedisTemplate();
        if (stringRedisTemplate != null) {
            try {
                java.util.Set<String> keys = stringRedisTemplate.keys(RedisKeys.STATISTICS_DASHBOARD + ":*");
                if (keys != null && !keys.isEmpty()) {
                    stringRedisTemplate.delete(keys);
                    log.info("Cleared statistics dashboard cache keys: {}", keys);
                }
            } catch (Exception exception) {
                log.warn("Failed to clear dashboard cache: {}", exception.getMessage());
            }
        }
    }
}
