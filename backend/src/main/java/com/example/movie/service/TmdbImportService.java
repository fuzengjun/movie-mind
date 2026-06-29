package com.example.movie.service;

import com.example.movie.dto.admin.TmdbImportResultDTO;

public interface TmdbImportService {

    TmdbImportResultDTO importPopularMovies(Integer limit);
}
