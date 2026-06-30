package com.example.movie.service;

import com.example.movie.vo.RecommendationVO;

import java.util.List;

public interface RecommendationService {
    List<RecommendationVO> recommendFor(String username, int limit);
}