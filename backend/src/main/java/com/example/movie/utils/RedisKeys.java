package com.example.movie.utils;

public final class RedisKeys {

    public static final String MOVIE_HOT = "movie:hot";
    public static final String MOVIE_TOP_RATED = "movie:top-rated";
    public static final String MOVIE_LATEST = "movie:latest";
    public static final String MOVIE_DETAIL_PREFIX = "movie:detail:";
    public static final String RECOMMEND_USER_PREFIX = "recommend:user:";
    public static final String STATISTICS_DASHBOARD = "statistics:dashboard";

    private RedisKeys() {
    }
}
