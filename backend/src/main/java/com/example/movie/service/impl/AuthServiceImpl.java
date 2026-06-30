package com.example.movie.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.movie.dto.LoginRequest;
import com.example.movie.dto.RegisterRequest;
import com.example.movie.entity.User;
import com.example.movie.mapper.UserMapper;
import com.example.movie.security.JwtUtil;
import com.example.movie.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String nickname = request.getNickname().trim();
        String email = normalizeEmail(request.getEmail());

        if (username.isEmpty()) throw new IllegalArgumentException("用户名不能为空");
        if (nickname.isEmpty()) throw new IllegalArgumentException("昵称不能为空");
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("两次输入的密码不一致");
        }
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("密码内容过长，请控制在 72 个字节以内");
        }
        if (existsByUsername(username)) throw new IllegalArgumentException("用户名已被注册");
        if (email != null && existsByEmail(email)) throw new IllegalArgumentException("邮箱已被注册");

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(nickname);
        user.setEmail(email);
        user.setRole("USER");
        user.setStatus(1);
        user.setDeleted(0);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException exception) {
            throw new IllegalArgumentException("用户名或邮箱已被注册");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> login(LoginRequest request) {
        String username = request.getUsername().trim();
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        if (user == null || !matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new IllegalArgumentException("账号已被禁用");
        }
        if (!user.getPassword().startsWith("$2")) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userMapper.updateById(user);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", jwtUtil.createToken(user.getUsername()));
        result.put("user", userProfile(user));
        return result;
    }

    private boolean existsByUsername(String username) {
        return userMapper.selectCount(Wrappers.<User>lambdaQuery().eq(User::getUsername, username)) > 0;
    }

    private boolean existsByEmail(String email) {
        return userMapper.selectCount(Wrappers.<User>lambdaQuery().eq(User::getEmail, email)) > 0;
    }

    private boolean matches(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(storedPassword)) return false;
        return storedPassword.startsWith("$2")
                ? passwordEncoder.matches(rawPassword, storedPassword)
                : storedPassword.equals(rawPassword);
    }

    private String normalizeEmail(String email) {
        return StringUtils.hasText(email) ? email.trim().toLowerCase() : null;
    }

    private Map<String, Object> userProfile(User user) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("nickname", user.getNickname());
        profile.put("email", user.getEmail());
        profile.put("avatar", user.getAvatar());
        profile.put("role", user.getRole());
        profile.put("status", user.getStatus());
        return profile;
    }
}