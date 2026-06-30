package com.example.movie.service;

import com.example.movie.config.TmdbProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@Component
@RequiredArgsConstructor
public class TmdbApiClient {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(6);
    private static final long UNAVAILABLE_COOLDOWN_MILLIS = 2 * 60 * 1000L;

    private final TmdbProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
    private final ConcurrentHashMap<String, Long> unavailableUntil = new ConcurrentHashMap<>();

    public JsonNode get(String pathAndQuery) {
        List<String> baseUrls = availableBaseUrls();
        if (baseUrls.isEmpty()) {
            throw new ResponseStatusException(BAD_GATEWAY, "TMDB 主域名和备用域名暂时不可用，请稍后重试");
        }

        List<String> failures = new ArrayList<>();
        for (String baseUrl : baseUrls) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(trimTrailingSlash(baseUrl) + pathAndQuery))
                        .timeout(REQUEST_TIMEOUT)
                        .header("Authorization", "Bearer " + properties.getReadAccessToken())
                        .header("Accept", "application/json")
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();
                if (status >= 200 && status < 300) {
                    unavailableUntil.remove(baseUrl);
                    return objectMapper.readTree(response.body());
                }
                markUnavailable(baseUrl);
                failures.add(host(baseUrl) + " 返回状态码 " + status);
            } catch (ResponseStatusException exception) {
                throw exception;
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new ResponseStatusException(BAD_GATEWAY, "TMDB 请求被中断", exception);
            } catch (Exception exception) {
                markUnavailable(baseUrl);
                failures.add(host(baseUrl) + " 连接失败或超时");
            }
        }
        throw new ResponseStatusException(BAD_GATEWAY,
                "TMDB 主域名与备用域名均不可用：" + String.join("；", failures));
    }

    private List<String> availableBaseUrls() {
        long now = System.currentTimeMillis();
        List<String> result = new ArrayList<>();
        addIfAvailable(result, properties.getBaseUrl(), now);
        if (!sameUrl(properties.getBaseUrl(), properties.getFallbackBaseUrl())) {
            addIfAvailable(result, properties.getFallbackBaseUrl(), now);
        }
        return result;
    }

    private void addIfAvailable(List<String> result, String baseUrl, long now) {
        if (StringUtils.hasText(baseUrl) && unavailableUntil.getOrDefault(baseUrl, 0L) <= now) {
            result.add(baseUrl);
        }
    }

    private void markUnavailable(String baseUrl) {
        unavailableUntil.put(baseUrl, System.currentTimeMillis() + UNAVAILABLE_COOLDOWN_MILLIS);
    }

    private boolean sameUrl(String first, String second) {
        return StringUtils.hasText(first) && StringUtils.hasText(second)
                && trimTrailingSlash(first).equalsIgnoreCase(trimTrailingSlash(second));
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String host(String baseUrl) {
        try {
            return URI.create(baseUrl).getHost();
        } catch (Exception ignored) {
            return baseUrl;
        }
    }
}