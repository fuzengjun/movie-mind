package com.example.movie.controller;

import com.example.movie.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    @GetMapping("/me")
    public Result<List<Object>> recommend() {
        return Result.success(List.of());
    }
}
