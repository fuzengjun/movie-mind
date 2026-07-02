package com.example.movie.service;

import com.example.movie.dto.LoginRequest;
import com.example.movie.dto.RegisterRequest;

import java.util.Map;

public interface AuthService {
    Map<String, Object> register(RegisterRequest request);

    Map<String, Object> login(LoginRequest request);
}