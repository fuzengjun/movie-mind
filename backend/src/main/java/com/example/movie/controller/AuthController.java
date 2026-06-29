package com.example.movie.controller;

import com.example.movie.common.Result;
import com.example.movie.dto.LoginRequest;
import com.example.movie.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success("注册接口骨架创建成功", Map.of("username", request.getUsername()));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录接口骨架创建成功", Map.of("username", request.getUsername(), "token", ""));
    }
}
