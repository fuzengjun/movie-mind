package com.example.movie.controller;

import com.example.movie.common.Result;
import com.example.movie.vo.MovieVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    @GetMapping
    public Result<List<MovieVO>> list() {
        return Result.success(List.of());
    }

    @GetMapping("/{id}")
    public Result<MovieVO> detail(@PathVariable Long id) {
        MovieVO movieVO = new MovieVO();
        movieVO.setId(id);
        movieVO.setTitle("示例影片");
        return Result.success(movieVO);
    }
}
