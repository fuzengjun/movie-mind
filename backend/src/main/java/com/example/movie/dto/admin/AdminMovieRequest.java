package com.example.movie.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class AdminMovieRequest {
    @NotBlank(message = "影片标题不能为空")
    private String title;
    private String originalTitle;
    private String overview;
    private String posterUrl;
    private String backdropUrl;
    private LocalDate releaseDate;
    private Integer runtime;
    private String region;
    private String language;
    private BigDecimal averageRating;
    private Integer status = 1;
    private List<Long> categoryIds;
    private List<Long> tagIds;
    private List<Long> actorIds;
    private List<Long> directorIds;
}
