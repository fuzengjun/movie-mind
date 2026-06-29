package com.example.movie.controller.admin;

import com.example.movie.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/movies")
public class AdminMovieController {

    @GetMapping
    public Result<List<Object>> list() {
        return Result.success(List.of());
    }
}
