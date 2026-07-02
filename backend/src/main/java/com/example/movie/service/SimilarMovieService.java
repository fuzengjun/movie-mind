package com.example.movie.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.Duration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SimilarMovieService {
    private final JdbcTemplate db;
    private final MovieCacheService cache;

    public List<Map<String, Object>> findSimilar(Long movieId, int limit) {
        Boolean exists = db.queryForObject("SELECT EXISTS(SELECT 1 FROM movie WHERE id=? AND deleted=0 AND status=1)", Boolean.class, movieId);
        if (!Boolean.TRUE.equals(exists)) throw new IllegalArgumentException("影片不存在或已下架");
        int safeLimit = Math.min(20, Math.max(1, limit));
        String cacheKey = "movie:similar:" + movieId + ":" + safeLimit;
        List<Map<String, Object>> cached = cache.read(cacheKey, new TypeReference<>() {
        });
        if (cached != null) return cached;
        String sql = """
                SELECT m.id, m.title, m.poster_url posterUrl, m.release_date releaseDate,
                       m.average_rating averageRating, m.favorite_count favoriteCount, m.view_count viewCount,
                       (SELECT COUNT(*) FROM movie_category a JOIN movie_category b ON a.category_id=b.category_id WHERE a.movie_id=? AND b.movie_id=m.id) categoryMatches,
                       (SELECT COUNT(*) FROM movie_tag a JOIN movie_tag b ON a.tag_id=b.tag_id WHERE a.movie_id=? AND b.movie_id=m.id) tagMatches,
                       (SELECT COUNT(*) FROM movie_director a JOIN movie_director b ON a.director_id=b.director_id WHERE a.movie_id=? AND b.movie_id=m.id) directorMatches,
                       (SELECT COUNT(*) FROM movie_actor a JOIN movie_actor b ON a.actor_id=b.actor_id WHERE a.movie_id=? AND b.movie_id=m.id) actorMatches
                FROM movie m
                WHERE m.id<>? AND m.deleted=0 AND m.status=1
                HAVING categoryMatches + tagMatches + directorMatches + actorMatches > 0
                ORDER BY (categoryMatches*4 + tagMatches*3 + directorMatches*5 + LEAST(actorMatches,3)*1.5) DESC,
                         COALESCE(NULLIF(m.average_rating,0),m.tmdb_rating,0) DESC, m.view_count DESC
                LIMIT ?
                """;
        List<Map<String, Object>> result = db.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", rs.getLong("id"));
            row.put("title", rs.getString("title"));
            row.put("posterUrl", rs.getString("posterUrl"));
            row.put("releaseDate", rs.getDate("releaseDate") == null ? null : rs.getDate("releaseDate").toLocalDate());
            row.put("averageRating", rs.getBigDecimal("averageRating"));
            row.put("favoriteCount", rs.getInt("favoriteCount"));
            row.put("viewCount", rs.getInt("viewCount"));
            row.put("reason", reason(rs.getInt("categoryMatches"), rs.getInt("tagMatches"), rs.getInt("directorMatches"), rs.getInt("actorMatches")));
            return row;
        }, movieId, movieId, movieId, movieId, movieId, safeLimit);
        cache.write(cacheKey, result, Duration.ofMinutes(30));
        return result;
    }

    private String reason(int categories, int tags, int directors, int actors) {
        if (directors > 0) return "来自同一导演的作品";
        if (categories > 0 && tags > 0) return "类型与主题气质相近";
        if (categories > 0) return "与你正在浏览的影片类型相近";
        if (tags > 0) return "拥有相似的主题标签";
        return actors > 0 ? "包含熟悉的演出阵容" : "相似影片";
    }
}

