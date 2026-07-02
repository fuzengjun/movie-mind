package com.example.movie;

import com.example.movie.config.AiProperties;
import com.example.movie.config.TmdbProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableConfigurationProperties({TmdbProperties.class, AiProperties.class})
public class MovieSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieSystemApplication.class, args);
    }
}
