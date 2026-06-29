package com.example.movie.dto.admin;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class DashboardStatisticsDTO {

    private Summary summary = new Summary();
    private List<TrendPoint> userGrowth = new ArrayList<>();
    private List<ActivityPoint> dailyActivity = new ArrayList<>();
    private List<NamedValue> categoryDistribution = new ArrayList<>();
    private List<NamedValue> ratingDistribution = new ArrayList<>();
    private List<NamedValue> yearDistribution = new ArrayList<>();
    private List<NamedValue> regionDistribution = new ArrayList<>();
    private List<MovieSpotlight> hotMovies = new ArrayList<>();
    private List<RankingItem> favoriteRanking = new ArrayList<>();
    private List<RankingItem> viewRanking = new ArrayList<>();
    private Integer rangeDays;
    private LocalDateTime generatedAt;

    @Data
    public static class Summary {
        private Long totalUsers = 0L;
        private Long totalMovies = 0L;
        private Long totalComments = 0L;
        private Long totalFavorites = 0L;
        private Long totalRatings = 0L;
        private Long todayUsers = 0L;
        private Long todayComments = 0L;
    }

    @Data
    public static class TrendPoint {
        private LocalDate date;
        private Long value = 0L;
    }

    @Data
    public static class ActivityPoint {
        private LocalDate date;
        private Long comments = 0L;
        private Long favorites = 0L;
    }

    @Data
    public static class NamedValue {
        private String name;
        private Long value = 0L;
    }

    @Data
    public static class MovieSpotlight {
        private Long id;
        private String title;
        private String overview;
        private String posterUrl;
        private String backdropUrl;
        private String region;
        private BigDecimal averageRating;
        private Integer favoriteCount;
        private Integer viewCount;
        private String releaseYear;
        private List<String> categories = new ArrayList<>();
    }

    @Data
    public static class RankingItem {
        private Long id;
        private String title;
        private String posterUrl;
        private BigDecimal averageRating;
        private Long metricValue = 0L;
        private String metricLabel;
    }
}
