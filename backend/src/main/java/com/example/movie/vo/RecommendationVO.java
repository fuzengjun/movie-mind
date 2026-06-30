package com.example.movie.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class RecommendationVO {
    private Long id;
    private String title;
    private String overview;
    private String posterUrl;
    private String backdropUrl;
    private String region;
    private LocalDate releaseDate;
    private Integer runtime;
    private BigDecimal averageRating;
    private Integer favoriteCount;
    private Integer viewCount;
    private List<String> categories;
    private Double recommendScore;
    private String reason;
    private String algorithm;
}