package com.example.movie.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.movie.entity.User;
import com.example.movie.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (SecurityContextHolder.getContext().getAuthentication() == null
                && header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                String username = jwtUtil.getUsername(token);
                if (!jwtUtil.isExpired(token)) {
                    User user = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                            .eq(User::getUsername, username)
                            .eq(User::getStatus, 1));
                    if (user != null) {
                        var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase());
                        var authentication = new UsernamePasswordAuthenticationToken(username, null, List.of(authority));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}