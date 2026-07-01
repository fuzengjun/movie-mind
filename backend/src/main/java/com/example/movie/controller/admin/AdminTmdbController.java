package com.example.movie.controller.admin;

import com.example.movie.common.Result;
import com.example.movie.dto.admin.TmdbImportResultDTO;
import com.example.movie.service.TmdbImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/tmdb")
@RequiredArgsConstructor
public class AdminTmdbController {

    private final TmdbImportService tmdbImportService;


    @GetMapping("/search")
    public Result<List<Map<String,Object>>> search(@RequestParam String query,
                                                   @RequestParam(defaultValue = "1") Integer page) {
        return Result.success(tmdbImportService.searchMovies(query, page));
    }

    @PostMapping("/import/{tmdbId}")
    public Result<TmdbImportResultDTO> importOne(@PathVariable Long tmdbId) {
        return Result.success(tmdbImportService.importMovie(tmdbId));
    }
    @PostMapping("/import/popular")
    public Result<TmdbImportResultDTO> importPopular(@RequestParam(required = false) Integer limit) {
        return Result.success(tmdbImportService.importPopularMovies(limit));
    }

    @PostMapping("/add")
    public Result<TmdbImportResultDTO> addNewMovies(@RequestParam(required = false) Integer limit,
                                                        @RequestParam(defaultValue = "popular") String source) {
        return Result.success(tmdbImportService.addNewMovies(limit, source));
    }

    @PostMapping("/refresh")
    public Result<TmdbImportResultDTO> refreshExisting(@RequestParam(required = false) Integer limit) {
        return Result.success(tmdbImportService.refreshExistingMovies(limit));
    }
}
