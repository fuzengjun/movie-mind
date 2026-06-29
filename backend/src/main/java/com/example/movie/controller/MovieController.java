package com.example.movie.controller;

import com.example.movie.common.Result;
import com.example.movie.service.MovieService;
import com.example.movie.vo.MovieVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public Result<List<MovieVO>> list() {
        return Result.success(movieService.listMovies());
    }

    @GetMapping("/{id}")
    public Result<MovieVO> detail(@PathVariable Long id) {
        MovieVO movieVO = movieService.getMovieDetail(id);
        if (movieVO == null) {
            return Result.fail("影片不存在");
        }
        return Result.success(movieVO);
    }

    @GetMapping("/hot")
    public Result<List<MovieVO>> hot() {
        return Result.success(movieService.listMovies().stream().limit(10).toList());
    }
}
