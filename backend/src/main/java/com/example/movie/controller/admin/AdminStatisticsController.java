package com.example.movie.controller.admin;

import com.example.movie.common.Result;
import com.example.movie.dto.admin.DashboardStatisticsDTO;
import com.example.movie.service.AdminStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    @GetMapping
    public Result<DashboardStatisticsDTO> overview(@RequestParam(required = false) Integer range) {
        return Result.success(adminStatisticsService.getDashboardStatistics(range));
    }
}
