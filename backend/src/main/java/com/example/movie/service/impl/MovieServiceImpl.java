package com.example.movie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.movie.entity.Movie;
import com.example.movie.mapper.MovieMapper;
import com.example.movie.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieMapper movieMapper;

    @Override
    public List<Movie> listHotMovies() {
        return movieMapper.selectList(new LambdaQueryWrapper<Movie>()
                .eq(Movie::getDeleted, 0)
                .orderByDesc(Movie::getViewCount)
                .last("limit 10"));
    }
}
