package com.example.movie.service;

import com.example.movie.dto.admin.AdminMovieListItemDTO;
import com.example.movie.dto.admin.AdminUserListItemDTO;
import com.example.movie.dto.admin.DashboardStatisticsDTO;

import java.util.List;

public interface AdminStatisticsService {

    DashboardStatisticsDTO getDashboardStatistics(Integer range);

    List<AdminMovieListItemDTO> listAdminMovies();

    List<AdminUserListItemDTO> listAdminUsers();
}
