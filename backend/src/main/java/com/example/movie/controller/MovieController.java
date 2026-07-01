package com.example.movie.controller;

import com.example.movie.common.Result;
import com.example.movie.common.PageResult;
import com.example.movie.service.MovieService;
import com.example.movie.service.MovieCatalogService;
import com.example.movie.service.MovieCacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Duration;
import com.example.movie.vo.MovieVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final MovieCatalogService catalogService;
    private final MovieCacheService cacheService;

    @GetMapping
    public Result<List<MovieVO>> list() {
        List<MovieVO> cached=cacheService.read("movie:home",new TypeReference<>() {});
        if(cached!=null)return Result.success(cached);
        List<MovieVO> movies=movieService.listMovies();cacheService.write("movie:home",movies,Duration.ofMinutes(30));
        return Result.success(movies);
    }

    @GetMapping("/{id}")
    public Result<MovieVO> detail(@PathVariable Long id) {
        MovieVO cached=cacheService.read("movie:detail:"+id,new TypeReference<>() {});
        if(cached!=null)return Result.success(cached);
        MovieVO movieVO=movieService.getMovieDetail(id);
        if(movieVO==null)return Result.fail("影片不存在");
        cacheService.write("movie:detail:"+id,movieVO,Duration.ofMinutes(10));return Result.success(movieVO);
    }

    @GetMapping("/hot")
    public Result<List<MovieVO>> hot() {
        List<MovieVO> cached=cacheService.read("movie:hot",new TypeReference<>() {});if(cached!=null)return Result.success(cached);
        List<MovieVO> movies=movieService.listMovies().stream().limit(10).toList();cacheService.write("movie:hot",movies,Duration.ofMinutes(30));return Result.success(movies);
    }

    @GetMapping("/page")
    public Result<PageResult<Map<String,Object>>> page(
            @org.springframework.web.bind.annotation.RequestParam(required=false) String keyword,
            @org.springframework.web.bind.annotation.RequestParam(required=false) String category,
            @org.springframework.web.bind.annotation.RequestParam(required=false) String region,
            @org.springframework.web.bind.annotation.RequestParam(required=false) Integer year,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue="hot") String sort,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue="1") long pageNum,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue="20") long pageSize) {
        return Result.success(catalogService.page(keyword, category, region, year, sort, pageNum, pageSize));
    }

    @GetMapping("/filters")
    public Result<Map<String,Object>> filters() {
        return Result.success(catalogService.filters());
    }}
