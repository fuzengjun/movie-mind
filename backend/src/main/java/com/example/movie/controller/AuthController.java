package com.example.movie.controller;

import com.example.movie.common.Result;
import com.example.movie.dto.LoginRequest;
import com.example.movie.dto.RegisterRequest;
import com.example.movie.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success("注册成功", authService.register(request));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", authService.login(request));
    }
}