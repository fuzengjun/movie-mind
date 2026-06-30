package com.example.movie.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tmdb")
public class TmdbProperties {

    private String baseUrl;
    private String fallbackBaseUrl;
    private String imageBaseUrl;
    private String readAccessToken;
}
