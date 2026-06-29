package com.example.movie.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("movie")
@EqualsAndHashCode(callSuper = true)
public class Movie extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tmdbId;
    private String title;
    private String originalTitle;
    private String overview;
    private String posterUrl;
    private String backdropUrl;
    private LocalDate releaseDate;
    private Integer runtime;
    private String region;
    private String language;
    private String originalLanguage;
    private String certification;
    private String productionCompanies;
    private String productionCountries;
    private String collectionName;
    private String releaseStatus;
    private String tagline;
    private String keywords;
    private BigDecimal averageRating;
    private BigDecimal tmdbRating;
    private Integer favoriteCount;
    private Integer viewCount;
    private Integer status;
    @TableLogic
    private Integer deleted;
}
