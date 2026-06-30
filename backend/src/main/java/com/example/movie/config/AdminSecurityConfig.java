package com.example.movie.config;

import com.example.movie.security.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Configuration @RequiredArgsConstructor
public class AdminSecurityConfig {
    private final JdbcTemplate jdbcTemplate; private final JwtUtil jwtUtil;
    @Bean public PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
    @Bean @Order(1) public SecurityFilterChain adminChain(HttpSecurity http)throws Exception{
        return http.securityMatcher("/api/admin/**").csrf(c->c.disable())
            .authorizeHttpRequests(a->a.anyRequest().hasRole("ADMIN"))
            .addFilterBefore(new AdminJwtFilter(jdbcTemplate,jwtUtil), UsernamePasswordAuthenticationFilter.class).build();
    }
    private static class AdminJwtFilter extends OncePerRequestFilter{
        private final JdbcTemplate jdbc; private final JwtUtil jwt;
        AdminJwtFilter(JdbcTemplate jdbc,JwtUtil jwt){this.jdbc=jdbc;this.jwt=jwt;}
        @Override protected void doFilterInternal(HttpServletRequest request,jakarta.servlet.http.HttpServletResponse response,FilterChain chain)throws ServletException,IOException{
            String header=request.getHeader("Authorization");
            if(header!=null&&header.startsWith("Bearer "))try{String username=jwt.getUsername(header.substring(7));if(!jwt.isExpired(header.substring(7))){var roles=jdbc.queryForList("SELECT role FROM user WHERE username=? AND status=1 AND deleted=0",String.class,username);if(!roles.isEmpty()){var auth=new UsernamePasswordAuthenticationToken(username,null,List.of(new SimpleGrantedAuthority("ROLE_"+roles.getFirst().toUpperCase())));SecurityContextHolder.getContext().setAuthentication(auth);}}}catch(Exception ignored){}
            chain.doFilter(request,response);
        }
    }
}
