package com.example.movie.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class PersonDetailVO {

    private Long id;
    private String type;
    private String typeLabel;
    private String name;
    private String originalName;
    private String profileUrl;
    private String gender;
    private LocalDate birthday;
    private String nationality;
    private String biography;
    private Integer movieCount;
    private String backdropUrl;
    private List<WorkSectionVO> sections;

    @Data
    public static class WorkSectionVO {
        private String key;
        private String title;
        private List<WorkMovieVO> movies;
    }

    @Data
    public static class WorkMovieVO {
        private Long id;
        private String title;
        private String posterUrl;
        private String backdropUrl;
        private LocalDate releaseDate;
        private BigDecimal averageRating;
        private List<String> categories;
        private String roleName;
    }
}