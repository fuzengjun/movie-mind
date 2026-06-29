package com.example.movie.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class MovieVO {

    private Long id;
    private String title;
    private String originalTitle;
    private String overview;
    private String posterUrl;
    private String backdropUrl;
    private String region;
    private String language;
    private String originalLanguage;
    private String certification;
    private List<String> productionCompanies;
    private List<String> productionCountries;
    private String collectionName;
    private String releaseStatus;
    private String tagline;
    private List<String> keywords;
    private String watchProviderRegion;
    private List<WatchProviderVO> watchProviders;
    private LocalDate releaseDate;
    private Integer runtime;
    private BigDecimal averageRating;
    private BigDecimal tmdbRating;
    private Integer favoriteCount;
    private Integer viewCount;
    private List<String> categories;
    private List<PersonVO> actors;
    private List<PersonVO> directors;

    @Data
    public static class WatchProviderVO {
        private Long providerId;
        private String name;
        private String logoUrl;
        private String region;
        private String accessType;
    }

    @Data
    public static class PersonVO {
        private Long id;
        private String personType;
        private String name;
        private String originalName;
        private String profileUrl;
        private String roleName;
    }
}