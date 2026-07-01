package com.example.movie.controller;
import com.example.movie.common.Result;import lombok.RequiredArgsConstructor;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/movies") @RequiredArgsConstructor public class RankingController{
 private final JdbcTemplate db;
 @GetMapping("/rankings")public Result<List<Map<String,Object>>>rankings(@RequestParam(defaultValue="rating")String type,@RequestParam(defaultValue="20")int limit){
  String order=switch(type){case"favorite"->"favorite_count DESC,average_rating DESC";case"view"->"view_count DESC,average_rating DESC";case"latest"->"release_date DESC,id DESC";case"user-rating"->"average_rating DESC,(SELECT COUNT(*) FROM rating r WHERE r.movie_id=movie.id) DESC";default->"COALESCE(NULLIF(average_rating,0),tmdb_rating,0) DESC,view_count DESC";};
  int safe=Math.min(100,Math.max(1,limit));return Result.success(db.queryForList("SELECT id,title,poster_url posterUrl,release_date releaseDate,average_rating averageRating,favorite_count favoriteCount,view_count viewCount FROM movie WHERE deleted=0 AND status=1 ORDER BY "+order+" LIMIT ?",safe));
 }
}