package com.example.movie.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.movie.service.RecommendationService;
import com.example.movie.utils.RedisKeys;
import com.example.movie.vo.RecommendationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private static final int MIN_INTERACTIONS = 3;
    private static final int MAX_NEIGHBORS = 20;
    private static final double MIN_CANDIDATE_PREFERENCE = 3.0;
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.cache.redis.enabled:false}")
    private boolean redisEnabled;

    @Override
    @Transactional
    public List<RecommendationVO> recommendFor(String username, int limit) {
        Long userId = findActiveUserId(username);
        List<RecommendationVO> cached = readCache(userId, limit);
        if (cached != null) return cached;

        Map<Long, Map<Long, Double>> matrix = loadBehaviorMatrix();
        Map<Long, Double> targetVector = matrix.getOrDefault(userId, Map.of());
        Set<Long> touchedMovieIds = new HashSet<>(targetVector.keySet());

        List<RecommendationVO> recommendations;
        if (targetVector.size() < MIN_INTERACTIONS) {
            recommendations = coldStart(touchedMovieIds, limit);
        } else {
            recommendations = collaborativeFilter(userId, matrix, touchedMovieIds, limit);
            if (recommendations.size() < limit) {
                Set<Long> excluded = new HashSet<>(touchedMovieIds);
                recommendations.forEach(item -> excluded.add(item.getId()));
                recommendations.addAll(coldStart(excluded, limit - recommendations.size()));
            }
        }

        persistResults(userId, recommendations);
        writeCache(userId, limit, recommendations);
        return recommendations;
    }

    private List<RecommendationVO> collaborativeFilter(Long userId,
                                                         Map<Long, Map<Long, Double>> matrix,
                                                         Set<Long> touchedMovieIds,
                                                         int limit) {
        Map<Long, Double> target = matrix.get(userId);
        double targetNorm = norm(target);
        if (targetNorm == 0) return List.of();

        List<Neighbor> neighbors = new ArrayList<>();
        matrix.forEach((otherUserId, vector) -> {
            if (otherUserId.equals(userId)) return;
            int commonItems = 0;
            double dot = 0;
            for (Map.Entry<Long, Double> entry : target.entrySet()) {
                Double otherValue = vector.get(entry.getKey());
                if (otherValue != null) {
                    commonItems++;
                    dot += entry.getValue() * otherValue;
                }
            }
            double otherNorm = norm(vector);
            if (commonItems >= 2 && dot > 0 && otherNorm > 0) {
                double similarity = dot / (targetNorm * otherNorm);
                if (similarity > 0) neighbors.add(new Neighbor(otherUserId, similarity));
            }
        });
        neighbors.sort(Comparator.comparingDouble(Neighbor::similarity).reversed());
        if (neighbors.isEmpty()) return List.of();
        List<Neighbor> topNeighbors = neighbors.size() > MAX_NEIGHBORS
                ? neighbors.subList(0, MAX_NEIGHBORS)
                : neighbors;

        Map<Long, Candidate> candidates = new HashMap<>();
        for (Neighbor neighbor : topNeighbors) {
            for (Map.Entry<Long, Double> entry : matrix.get(neighbor.userId()).entrySet()) {
                if (touchedMovieIds.contains(entry.getKey()) || entry.getValue() < MIN_CANDIDATE_PREFERENCE) continue;
                Candidate candidate = candidates.computeIfAbsent(entry.getKey(), ignored -> new Candidate());
                candidate.weightedScore += neighbor.similarity() * entry.getValue();
                candidate.similaritySum += neighbor.similarity();
                candidate.support++;
            }
        }

        List<ScoredMovie> scoredMovies = candidates.entrySet().stream()
                .map(entry -> {
                    Candidate value = entry.getValue();
                    double score = value.weightedScore / value.similaritySum + Math.log1p(value.support) * 0.15;
                    String reason = "与你兴趣相近的 " + value.support + " 位用户喜欢这部影片";
                    return new ScoredMovie(entry.getKey(), score, reason, "USER_CF_COSINE");
                })
                .sorted(Comparator.comparingDouble(ScoredMovie::score).reversed())
                .limit(limit)
                .toList();
        return loadMovies(scoredMovies);
    }

    private List<RecommendationVO> coldStart(Set<Long> excludedMovieIds, int limit) {
        String sql = """
                SELECT m.id,m.title,m.overview,m.poster_url,m.backdrop_url,m.region,m.release_date,m.runtime,
                       m.average_rating,m.favorite_count,m.view_count,
                       GROUP_CONCAT(DISTINCT c.name ORDER BY c.name SEPARATOR ',') AS categories,
                       (0.40 * LOG10(m.view_count + 1)
                        + 0.25 * LOG10(m.favorite_count + 1)
                        + 0.30 * (COALESCE(NULLIF(m.average_rating,0),m.tmdb_rating,0) / 10)
                        + 0.05 * GREATEST(0,1 - COALESCE(DATEDIFF(CURDATE(),m.release_date),3650) / 3650)) AS recommendation_score
                FROM movie m
                LEFT JOIN movie_category mc ON mc.movie_id=m.id
                LEFT JOIN category c ON c.id=mc.category_id AND c.deleted=0 AND c.status=1
                WHERE m.deleted=0 AND m.status=1
                GROUP BY m.id
                ORDER BY recommendation_score DESC,m.release_date DESC,m.id DESC
                LIMIT 100
                """;
        List<RecommendationVO> pool = jdbcTemplate.query(sql, (rs, rowNum) -> {
            RecommendationVO item = mapMovie(rs);
            item.setRecommendScore(round(rs.getDouble("recommendation_score")));
            item.setReason("结合热门程度、评分与新鲜度为你推荐");
            item.setAlgorithm("COLD_START_HYBRID");
            return item;
        });
        return pool.stream().filter(item -> !excludedMovieIds.contains(item.getId())).limit(limit).toList();
    }

    private Map<Long, Map<Long, Double>> loadBehaviorMatrix() {
        String sql = """
                SELECT behavior.user_id,behavior.movie_id,SUM(behavior.preference) AS preference
                FROM (
                    SELECT user_id,movie_id,LEAST(5.0,GREATEST(0.0,score / 2.0)) AS preference FROM rating
                    UNION ALL
                    SELECT user_id,movie_id,4.0 AS preference FROM favorite
                    UNION ALL
                    SELECT user_id,movie_id,3.0 AS preference FROM watch_history GROUP BY user_id,movie_id
                    UNION ALL
                    SELECT user_id,movie_id,2.5 AS preference FROM watchlist
                    UNION ALL
                    SELECT user_id,movie_id,1.0 AS preference FROM view_history GROUP BY user_id,movie_id
                ) behavior
                INNER JOIN user u ON u.id=behavior.user_id AND u.deleted=0 AND u.status=1
                INNER JOIN movie m ON m.id=behavior.movie_id AND m.deleted=0 AND m.status=1
                GROUP BY behavior.user_id,behavior.movie_id
                """;
        Map<Long, Map<Long, Double>> matrix = new HashMap<>();
        jdbcTemplate.query(sql, (org.springframework.jdbc.core.RowCallbackHandler) rs -> matrix
                .computeIfAbsent(rs.getLong("user_id"), ignored -> new HashMap<>())
                .put(rs.getLong("movie_id"), rs.getDouble("preference")));
        return matrix;
    }

    private List<RecommendationVO> loadMovies(List<ScoredMovie> scores) {
        if (scores.isEmpty()) return List.of();
        String placeholders = String.join(",", java.util.Collections.nCopies(scores.size(), "?"));
        String sql = """
                SELECT m.id,m.title,m.overview,m.poster_url,m.backdrop_url,m.region,m.release_date,m.runtime,
                       m.average_rating,m.favorite_count,m.view_count,
                       GROUP_CONCAT(DISTINCT c.name ORDER BY c.name SEPARATOR ',') AS categories
                FROM movie m
                LEFT JOIN movie_category mc ON mc.movie_id=m.id
                LEFT JOIN category c ON c.id=mc.category_id AND c.deleted=0 AND c.status=1
                WHERE m.deleted=0 AND m.status=1 AND m.id IN (%s)
                GROUP BY m.id
                """.formatted(placeholders);
        Map<Long, RecommendationVO> movies = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            RecommendationVO item = mapMovie(rs);
            movies.put(item.getId(), item);
        }, scores.stream().map(ScoredMovie::movieId).toArray());

        List<RecommendationVO> result = new ArrayList<>();
        for (ScoredMovie score : scores) {
            RecommendationVO item = movies.get(score.movieId());
            if (item == null) continue;
            item.setRecommendScore(round(score.score()));
            item.setReason(score.reason());
            item.setAlgorithm(score.algorithm());
            result.add(item);
        }
        return result;
    }

    private RecommendationVO mapMovie(ResultSet rs) throws SQLException {
        RecommendationVO item = new RecommendationVO();
        item.setId(rs.getLong("id"));
        item.setTitle(rs.getString("title"));
        item.setOverview(rs.getString("overview"));
        item.setPosterUrl(rs.getString("poster_url"));
        item.setBackdropUrl(rs.getString("backdrop_url"));
        item.setRegion(rs.getString("region"));
        java.sql.Date releaseDate = rs.getDate("release_date");
        item.setReleaseDate(releaseDate == null ? null : releaseDate.toLocalDate());
        item.setRuntime(rs.getObject("runtime", Integer.class));
        item.setAverageRating(rs.getBigDecimal("average_rating"));
        item.setFavoriteCount(rs.getObject("favorite_count", Integer.class));
        item.setViewCount(rs.getObject("view_count", Integer.class));
        String categories = rs.getString("categories");
        item.setCategories(categories == null || categories.isBlank() ? List.of() : List.of(categories.split(",")));
        return item;
    }

    private Long findActiveUserId(String username) {
        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT id FROM user WHERE username=? AND status=1 AND deleted=0", Long.class, username);
        if (ids.isEmpty()) throw new IllegalArgumentException("登录用户不存在或已被禁用");
        return ids.getFirst();
    }

    private double norm(Map<Long, Double> vector) {
        return Math.sqrt(vector.values().stream().mapToDouble(value -> value * value).sum());
    }

    private void persistResults(Long userId, List<RecommendationVO> recommendations) {
        jdbcTemplate.update("DELETE FROM recommend_result WHERE user_id=?", userId);
        if (recommendations.isEmpty()) return;
        List<Object[]> batch = recommendations.stream()
                .map(item -> new Object[]{userId, item.getId(), BigDecimal.valueOf(item.getRecommendScore()),
                        item.getReason(), item.getAlgorithm()})
                .toList();
        jdbcTemplate.batchUpdate("""
                INSERT INTO recommend_result(user_id,movie_id,score,reason,algorithm,create_time)
                VALUES(?,?,?,?,?,NOW())
                """, batch);
    }

    private List<RecommendationVO> readCache(Long userId, int limit) {
        if (!redisEnabled) return null;
        try {
            String value = redisTemplate.opsForValue().get(cacheKey(userId, limit));
            return value == null ? null : objectMapper.readValue(value, new TypeReference<>() {});
        } catch (Exception ignored) {
            return null;
        }
    }

    private void writeCache(Long userId, int limit, List<RecommendationVO> recommendations) {
        if (!redisEnabled) return;
        try {
            redisTemplate.opsForValue().set(cacheKey(userId, limit), objectMapper.writeValueAsString(recommendations), CACHE_TTL);
        } catch (Exception ignored) {
            // Redis 是可选能力，失败时保留数据库实时计算结果。
        }
    }

    private String cacheKey(Long userId, int limit) {
        return RedisKeys.RECOMMEND_USER_PREFIX + userId + ":" + limit;
    }

    private double round(double value) {
        return Math.round(value * 10_000d) / 10_000d;
    }

    private record Neighbor(Long userId, double similarity) {}
    private record ScoredMovie(Long movieId, double score, String reason, String algorithm) {}

    private static class Candidate {
        private double weightedScore;
        private double similaritySum;
        private int support;
    }
}