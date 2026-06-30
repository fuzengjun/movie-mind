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
                       GROUP_CONCAT(DISTINCT c.name ORDER BY c.name SEPARATOR ',') AS categories
                FROM movie m
                LEFT JOIN movie_category mc ON mc.movie_id = m.id
                LEFT JOIN category c ON c.id = mc.category_id AND c.deleted = 0
                WHERE m.deleted = 0 AND m.status = 1
                GROUP BY m.id
                ORDER BY m.view_count DESC, m.favorite_count DESC, m.id DESC
                LIMIT 200
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
                       m.original_language,
                       m.certification,
                       m.production_companies,
                       m.production_countries,
                       m.collection_name,
                       m.release_status,
                       m.tagline,
                       m.keywords,
                       m.release_date,
                       m.runtime,
                       m.average_rating,
                       m.tmdb_rating,
                       m.favorite_count,
                       m.view_count,
                       GROUP_CONCAT(DISTINCT c.name ORDER BY c.name SEPARATOR ',') AS categories
                FROM movie m
                LEFT JOIN movie_category mc ON mc.movie_id = m.id
                LEFT JOIN category c ON c.id = mc.category_id AND c.deleted = 0
                WHERE m.deleted = 0 AND m.id = ?
                GROUP BY m.id
                """;
        List<MovieVO> movies = jdbcTemplate.query(sql, (rs, rowNum) -> mapMovieDetail(rs), id);
        if (movies.isEmpty()) {
            return null;
        }
        MovieVO movie = movies.get(0);
        movie.setActors(loadActors(id));
        movie.setDirectors(loadDirectors(id));
        List<MovieVO.WatchProviderVO> watchProviders = loadWatchProviders(id);
        movie.setWatchProviders(watchProviders);
        if (!watchProviders.isEmpty()) {
            movie.setWatchProviderRegion(watchProviders.get(0).getRegion());
        }
        return movie;
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
        movie.setOriginalLanguage(rs.getString("original_language"));
        movie.setCertification(rs.getString("certification"));
        movie.setProductionCompanies(parseCommaSeparated(rs.getString("production_companies")));
        movie.setProductionCountries(parseCommaSeparated(rs.getString("production_countries")));
        movie.setCollectionName(rs.getString("collection_name"));
        movie.setReleaseStatus(rs.getString("release_status"));
        movie.setTagline(rs.getString("tagline"));
        movie.setKeywords(parseCommaSeparated(rs.getString("keywords")));
        movie.setTmdbRating(rs.getBigDecimal("tmdb_rating"));
        return movie;
    }

    private List<MovieVO.PersonVO> loadActors(Long movieId) {
        String sql = """
                SELECT a.id, 'actor' AS person_type, a.name, a.original_name, a.profile_url, ma.role_name
                FROM movie_actor ma
                INNER JOIN actor a ON a.id = ma.actor_id AND a.deleted = 0
                WHERE ma.movie_id = ?
                ORDER BY ma.id ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapPerson(rs), movieId);
    }

    private List<MovieVO.PersonVO> loadDirectors(Long movieId) {
        String sql = """
                SELECT d.id, 'director' AS person_type, d.name, d.original_name, d.profile_url, NULL AS role_name
                FROM movie_director md
                INNER JOIN director d ON d.id = md.director_id AND d.deleted = 0
                WHERE md.movie_id = ?
                ORDER BY md.id ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapPerson(rs), movieId);
    }

    private List<MovieVO.WatchProviderVO> loadWatchProviders(Long movieId) {
        String sql = """
                SELECT provider_id, provider_name, logo_url, region, access_type
                FROM movie_watch_provider
                WHERE movie_id = ?
                ORDER BY display_priority ASC, provider_name ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            MovieVO.WatchProviderVO provider = new MovieVO.WatchProviderVO();
            provider.setProviderId(rs.getLong("provider_id"));
            provider.setName(rs.getString("provider_name"));
            provider.setLogoUrl(rs.getString("logo_url"));
            provider.setRegion(rs.getString("region"));
            provider.setAccessType(rs.getString("access_type"));
            return provider;
        }, movieId);
    }

    private MovieVO.PersonVO mapPerson(ResultSet rs) throws SQLException {
        MovieVO.PersonVO person = new MovieVO.PersonVO();
        person.setId(rs.getLong("id"));
        person.setPersonType(rs.getString("person_type"));
        person.setName(rs.getString("name"));
        person.setOriginalName(rs.getString("original_name"));
        person.setProfileUrl(rs.getString("profile_url"));
        person.setRoleName(rs.getString("role_name"));
        return person;
    }

    private List<String> parseCategories(String categories) {
        return parseCommaSeparated(categories);
    }

    private List<String> parseCommaSeparated(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return List.of(value.split(","));
    }
}