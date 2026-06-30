package com.example.movie.config;

import com.example.movie.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AdminSecurityConfig {

    private final JdbcTemplate jdbcTemplate;
    private final JwtUtil jwtUtil;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain adminChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/api/admin/**")
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> writeError(response, 401, "管理员登录状态已失效"))
                        .accessDeniedHandler((request, response, exception) -> writeError(response, 403, "当前账号没有后台权限")))
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("ADMIN"))
                .addFilterBefore(new AdminJwtFilter(jdbcTemplate, jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + status + ",\"message\":\"" + message + "\",\"data\":null}");
    }

    private static class AdminJwtFilter extends OncePerRequestFilter {
        private final JdbcTemplate jdbc;
        private final JwtUtil jwt;

        AdminJwtFilter(JdbcTemplate jdbc, JwtUtil jwt) {
            this.jdbc = jdbc;
            this.jwt = jwt;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                try {
                    String token = header.substring(7);
                    String username = jwt.getUsername(token);
                    if (!jwt.isExpired(token)) {
                        List<String> roles = jdbc.queryForList(
                                "SELECT role FROM user WHERE username=? AND status=1 AND deleted=0",
                                String.class, username);
                        if (!roles.isEmpty()) {
                            var authority = new SimpleGrantedAuthority("ROLE_" + roles.getFirst().toUpperCase());
                            var authentication = new UsernamePasswordAuthenticationToken(username, null, List.of(authority));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        }
                    }
                } catch (Exception ignored) {
                    SecurityContextHolder.clearContext();
                }
            }
            chain.doFilter(request, response);
        }
    }
}