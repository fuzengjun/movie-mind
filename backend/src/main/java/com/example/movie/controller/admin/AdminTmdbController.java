package com.example.movie.controller.admin;

import com.example.movie.common.Result;
import com.example.movie.dto.admin.TmdbImportResultDTO;
import com.example.movie.service.TmdbImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/tmdb")
@RequiredArgsConstructor
public class AdminTmdbController {

    private final TmdbImportService tmdbImportService;

    @PostMapping("/import/popular")
    public Result<TmdbImportResultDTO> importPopular(@RequestParam(required = false) Integer limit) {
        return Result.success(tmdbImportService.importPopularMovies(limit));
    }

    @PostMapping("/add")
    public Result<TmdbImportResultDTO> addNewMovies(@RequestParam(required = false) Integer limit) {
        return Result.success(tmdbImportService.addNewMovies(limit));
    }

    @PostMapping("/refresh")
    public Result<TmdbImportResultDTO> refreshExisting(@RequestParam(required = false) Integer limit) {
        return Result.success(tmdbImportService.refreshExistingMovies(limit));
    }
}
