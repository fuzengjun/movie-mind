package com.example.movie.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名不能超过 64 个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度应为 6 到 64 位")
    private String password;

    @NotBlank(message = "请再次输入密码")
    private String confirmPassword;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称不能超过 64 个字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱不能超过 128 个字符")
    private String email;
}