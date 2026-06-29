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
    public static class PersonVO {
        private String name;
        private String originalName;
        private String profileUrl;
        private String roleName;
    }
}
