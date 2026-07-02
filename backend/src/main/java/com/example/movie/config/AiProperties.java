package com.example.movie.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "deepseek")
public class AiProperties {

    private String apiKey;
    private String apiUrl;
    private String model;
}
