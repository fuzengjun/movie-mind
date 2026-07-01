package com.example.movie.service;

import com.example.movie.dto.admin.TmdbImportResultDTO;
import java.util.List;
import java.util.Map;

public interface TmdbImportService {

    List<Map<String,Object>> searchMovies(String query, Integer page);

    TmdbImportResultDTO importMovie(Long tmdbId);

    TmdbImportResultDTO importPopularMovies(Integer limit);

    TmdbImportResultDTO addNewMovies(Integer limit, String source);

    TmdbImportResultDTO refreshExistingMovies(Integer limit);
}
