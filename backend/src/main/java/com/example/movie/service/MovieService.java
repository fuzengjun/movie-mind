package com.example.movie.service;

import com.example.movie.entity.Movie;
import com.example.movie.vo.MovieVO;

import java.util.List;

public interface MovieService {

    List<MovieVO> listMovies();

    MovieVO getMovieDetail(Long id);

    List<Movie> listHotMovies();
}
