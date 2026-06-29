package com.example.movie.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@TableName("director")
@EqualsAndHashCode(callSuper = true)
public class Director extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tmdbId;
    private String name;
    private String originalName;
    private String gender;
    private String profileUrl;
    private LocalDate birthday;
    private String nationality;
    private String biography;
    @TableLogic
    private Integer deleted;
}
