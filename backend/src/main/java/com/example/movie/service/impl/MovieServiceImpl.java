package com.example.movie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.movie.entity.Movie;
import com.example.movie.mapper.MovieMapper;
import com.example.movie.service.MovieService;
import com.example.movie.vo.MovieVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieMapper movieMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<MovieVO> listMovies() {
        String sql = """
                SELECT m.id,
                       m.title,
                       m.overview,
                       m.poster_url,
                       m.backdrop_url,
                       m.region,
                       m.release_date,
                       m.runtime,
                       m.average_rating,
                       m.favorite_count,
                       m.view_count,
                       GROUP_CONCAT(c.name ORDER BY c.name SEPARATOR ',') AS categories
                FROM movie m
                LEFT JOIN movie_category mc ON mc.movie_id = m.id
                LEFT JOIN category c ON c.id = mc.category_id AND c.deleted = 0
                WHERE m.deleted = 0 AND m.status = 1
                GROUP BY m.id
                ORDER BY m.view_count DESC, m.favorite_count DESC, m.id DESC
                LIMIT 24
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapMovieVO(rs));
    }

    @Override
    public MovieVO getMovieDetail(Long id) {
        String sql = """
                SELECT m.id,
                       m.title,
                       m.original_title,
                       m.overview,
                       m.poster_url,
                       m.backdrop_url,
                       m.region,
                       m.language,
                       m.release_date,
                       m.runtime,
                       m.average_rating,
                       m.tmdb_rating,
                       m.favorite_count,
                       m.view_count,
                       GROUP_CONCAT(c.name ORDER BY c.name SEPARATOR ',') AS categories
                FROM movie m
                LEFT JOIN movie_category mc ON mc.movie_id = m.id
                LEFT JOIN category c ON c.id = mc.category_id AND c.deleted = 0
                WHERE m.deleted = 0 AND m.id = ?
                GROUP BY m.id
                """;
        List<MovieVO> movies = jdbcTemplate.query(sql, (rs, rowNum) -> mapMovieDetail(rs), id);
        return movies.isEmpty() ? null : movies.get(0);
    }

    @Override
    public List<Movie> listHotMovies() {
        return movieMapper.selectList(new LambdaQueryWrapper<Movie>()
                .eq(Movie::getDeleted, 0)
                .orderByDesc(Movie::getViewCount)
                .last("limit 10"));
    }

    private MovieVO mapMovieVO(ResultSet rs) throws SQLException {
        MovieVO movie = new MovieVO();
        movie.setId(rs.getLong("id"));
        movie.setTitle(rs.getString("title"));
        movie.setOverview(rs.getString("overview"));
        movie.setPosterUrl(rs.getString("poster_url"));
        movie.setBackdropUrl(rs.getString("backdrop_url"));
        movie.setRegion(rs.getString("region"));
        java.sql.Date releaseDate = rs.getDate("release_date");
        movie.setReleaseDate(releaseDate == null ? null : releaseDate.toLocalDate());
        movie.setRuntime(rs.getObject("runtime", Integer.class));
        movie.setAverageRating(rs.getBigDecimal("average_rating"));
        movie.setFavoriteCount(rs.getObject("favorite_count", Integer.class));
        movie.setViewCount(rs.getObject("view_count", Integer.class));
        movie.setCategories(parseCategories(rs.getString("categories")));
        return movie;
    }

    private MovieVO mapMovieDetail(ResultSet rs) throws SQLException {
        MovieVO movie = mapMovieVO(rs);
        movie.setOriginalTitle(rs.getString("original_title"));
        movie.setLanguage(rs.getString("language"));
        movie.setTmdbRating(rs.getBigDecimal("tmdb_rating"));
        return movie;
    }

    private List<String> parseCategories(String categories) {
        if (categories == null || categories.isBlank()) {
            return List.of();
        }
        return List.of(categories.split(","));
    }
}
