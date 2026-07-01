package com.example.movie.controller;

import com.example.movie.common.Result;
import com.example.movie.service.RecommendationService;
import com.example.movie.vo.RecommendationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
@Slf4j
public class RecommendController {

    private final RecommendationService recommendationService;

    @GetMapping("/me")
    public Result<List<RecommendationVO>> recommend(Authentication authentication,
                                                     @RequestParam(defaultValue = "12") int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 30));
        try {
            return Result.success(recommendationService.recommendFor(authentication.getName(), safeLimit));
        } catch (RuntimeException exception) {
            log.error("生成用户 {} 的推荐失败，已降级为空列表", authentication.getName(), exception);
            return Result.success("推荐服务暂时不可用", List.of());
        }
    }
}