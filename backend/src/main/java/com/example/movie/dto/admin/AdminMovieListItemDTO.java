package com.example.movie.dto.admin;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class AdminMovieListItemDTO {

    private Long id;
    private String title;
    private String posterUrl;
    private String region;
    private LocalDate releaseDate;
    private BigDecimal averageRating;
    private Integer favoriteCount;
    private Integer viewCount;
    private Integer status;
    private List<String> categories;
    private List<String> directors;
    private List<String> actors;
}
