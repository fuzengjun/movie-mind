package com.example.movie.controller;

import com.example.movie.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/profile")
    public Result<Map<String, Object>> profile() {
        return Result.success(Map.of("nickname", "Movie Mind User"));
    }
}
