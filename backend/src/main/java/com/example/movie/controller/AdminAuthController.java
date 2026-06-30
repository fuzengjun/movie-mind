package com.example.movie.controller;

import com.example.movie.common.Result;
import com.example.movie.dto.LoginRequest;
import com.example.movie.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AdminAuthController {
    private final JdbcTemplate jdbcTemplate; private final PasswordEncoder passwordEncoder; private final JwtUtil jwtUtil;
    @PostMapping("/admin-login")
    public Result<Map<String,Object>> login(@Valid @RequestBody LoginRequest request){
        var rows=jdbcTemplate.queryForList("SELECT id,username,password,nickname,email,avatar,role,status FROM user WHERE username=? AND deleted=0",request.getUsername());
        if(rows.isEmpty())throw new ResponseStatusException(UNAUTHORIZED,"用户名或密码错误");
        var user=rows.getFirst(); String stored=String.valueOf(user.get("password"));
        boolean valid=stored.startsWith("$2")?passwordEncoder.matches(request.getPassword(),stored):stored.equals(request.getPassword());
        if(!valid)throw new ResponseStatusException(UNAUTHORIZED,"用户名或密码错误");
        if(((Number)user.get("status")).intValue()!=1)throw new ResponseStatusException(UNAUTHORIZED,"账号已被禁用");
        if(!"ADMIN".equalsIgnoreCase(String.valueOf(user.get("role"))))throw new ResponseStatusException(UNAUTHORIZED,"当前账号没有后台权限");
        if(!stored.startsWith("$2"))jdbcTemplate.update("UPDATE user SET password=?,update_time=NOW() WHERE id=?",passwordEncoder.encode(request.getPassword()),user.get("id"));
        user.remove("password"); return Result.success(Map.of("token",jwtUtil.createToken(request.getUsername()),"user",user));
    }
}
