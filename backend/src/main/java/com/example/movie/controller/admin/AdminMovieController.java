package com.example.movie.controller.admin;

import com.example.movie.common.Result;
import com.example.movie.dto.admin.AdminMovieListItemDTO;
import com.example.movie.service.AdminStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
public class AdminMovieController {

    private final AdminStatisticsService adminStatisticsService;

    @GetMapping
    public Result<List<AdminMovieListItemDTO>> list() {
        return Result.success(adminStatisticsService.listAdminMovies());
    }
}
