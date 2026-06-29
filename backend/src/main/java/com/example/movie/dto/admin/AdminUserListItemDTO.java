package com.example.movie.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserListItemDTO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
    private Integer favoriteCount;
    private Integer commentCount;
    private Integer ratingCount;
}
