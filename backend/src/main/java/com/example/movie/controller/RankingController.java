package com.example.movie.controller;

import com.example.movie.common.PageResult;
import com.example.movie.common.Result;
import com.example.movie.service.MovieCacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.time.Duration;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class RankingController {
    private final JdbcTemplate db;
    private final MovieCacheService cache;

    @GetMapping("/rankings")
    public Result<PageResult<Map<String, Object>>> rankings(
            @RequestParam(defaultValue = "rating") String type,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize) {
        String order = switch (type) {
            case "favorite" -> "m.favorite_count DESC, m.average_rating DESC, m.id DESC";
            case "view" -> "m.view_count DESC, m.average_rating DESC, m.id DESC";
            case "latest" -> "m.release_date DESC, m.id DESC";
            case "user-rating" -> "m.average_rating DESC, rating_count DESC, m.id DESC";
            case "rating" ->
                    "COALESCE(NULLIF(m.average_rating, 0), m.tmdb_rating, 0) DESC, m.view_count DESC, m.id DESC";
            default -> throw new IllegalArgumentException("不支持的排行榜类型");
        };
        long safePage = Math.max(1, pageNum);
        long safeSize = Math.min(50, Math.max(1, pageSize));
        long offset = (safePage - 1) * safeSize;
        String cacheKey = "movie:ranking:" + type + ":" + safePage + ":" + safeSize;
        PageResult<Map<String, Object>> cached = cache.read(cacheKey, new TypeReference<>() {
        });
        if (cached != null) return Result.success(cached);
        Long total = db.queryForObject("SELECT COUNT(*) FROM movie WHERE deleted=0 AND status=1", Long.class);
        String sql = """
                SELECT m.id, m.title, m.poster_url posterUrl, m.release_date releaseDate,
                       m.average_rating averageRating, m.favorite_count favoriteCount,
                       m.view_count viewCount,
                       (SELECT COUNT(*) FROM rating r WHERE r.movie_id=m.id) ratingCount
                FROM movie m
                WHERE m.deleted=0 AND m.status=1
                ORDER BY %s LIMIT ? OFFSET ?
                """.formatted(order);
        List<Map<String, Object>> rows = db.queryForList(sql, safeSize, offset);
        PageResult<Map<String, Object>> result = new PageResult<>(total, safePage, safeSize, rows);
        cache.write(cacheKey, result, Duration.ofMinutes(30));
        return Result.success(result);
    }
}
