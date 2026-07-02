package com.example.movie.service.impl;

import com.example.movie.config.AiProperties;
import com.example.movie.dto.AiChatRequest;
import com.example.movie.service.AiAssistantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final Pattern BOOKMARK_PATTERN = Pattern.compile("《([^》]+)》");
    private static final int CONTEXT_ITEM_LIMIT = 20;

    private static final String BASE_SYSTEM_PROMPT =
            "你是一个专业的影视助手，名为「影片精灵」。你的能力包括：\n" +
            "1. 根据用户的描述推荐合适的电影或电视剧\n" +
            "2. 解答电影剧情大纲、人物关系、幕后故事等\n" +
            "3. 提供电影相关的趣味知识、影评观点、奖项信息等\n" +
            "4. 帮助用户理解和分析电影主题、导演风格等\n" +
            "请用专业且友好的语气回答，使用中文，回答简洁有条理。" +
            "你在推荐电影或提及片名时，请务必使用《》书名号包裹片名，方便系统识别。\n" +
            "如果用户问的问题与影视完全无关，请礼貌地引导回到影视话题。";

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate db;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build();

    @Override
    public Map<String, Object> chat(AiChatRequest request) {
        String systemPrompt = buildSystemPrompt(request.getUserContext());
        String aiResponse = callDeepSeek(systemPrompt, request.getMessages());
        List<Map<String, Object>> links = findLocalMovies(aiResponse);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", aiResponse);
        result.put("role", "assistant");
        result.put("links", links);
        return result;
    }

    private String buildSystemPrompt(Map<String, Object> userContext) {
        if (userContext == null || userContext.isEmpty()) return BASE_SYSTEM_PROMPT;

        StringBuilder sb = new StringBuilder(BASE_SYSTEM_PROMPT);
        sb.append("\n\n## 当前用户信息\n");

        String username = (String) userContext.getOrDefault("username", "用户");
        sb.append("用户名：").append(username).append("\n");

        appendList(sb, userContext, "favorites", "收藏的影片");
        appendList(sb, userContext, "ratings", "评分的影片（含评分）");
        appendList(sb, userContext, "watched", "看过的影片");
        appendList(sb, userContext, "watchlist", "想看的影片");

        sb.append("\n在推荐电影时，请参考以上用户的观影偏好，避免推荐用户已经看过或收藏过的影片，优先推荐风格相似的影片。");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private void appendList(StringBuilder sb, Map<String, Object> ctx, String key, String label) {
        Object val = ctx.get(key);
        if (val instanceof List<?> list && !list.isEmpty()) {
            int count = Math.min(list.size(), CONTEXT_ITEM_LIMIT);
            sb.append(label).append("（共 ").append(list.size()).append(" 部）：\n");
            for (int i = 0; i < count; i++) {
                Object item = list.get(i);
                if (item instanceof Map<?, ?> m) {
                    String title = (String) m.get("title");
                    Object score = m.get("score");
                    if (score != null && !"ratings".equals(key)) continue;
                    sb.append("- 《").append(title).append("》");
                    if (score != null) sb.append("（评分 ").append(score).append("）");
                    sb.append("\n");
                }
            }
            if (list.size() > CONTEXT_ITEM_LIMIT) sb.append("  ... 等\n");
        }
    }

    private String callDeepSeek(String systemPrompt, List<Map<String, String>> messages) {
        List<Map<String, String>> fullMessages = new ArrayList<>();
        fullMessages.add(Map.of("role", "system", "content", systemPrompt));
        fullMessages.addAll(messages);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", aiProperties.getModel());
        body.put("messages", fullMessages);
        body.put("stream", false);
        body.put("temperature", 0.7);
        body.put("max_tokens", 1024);

        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiProperties.getApiUrl()))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Authorization", "Bearer " + aiProperties.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                var root = objectMapper.readTree(response.body());
                return root.path("choices").get(0).path("message").path("content").asText();
            }

            log.error("DeepSeek API returned status {}: {}", status, response.body());
            return "抱歉，AI 服务暂时不可用，请稍后重试。";
        } catch (Exception e) {
            log.error("DeepSeek API call failed", e);
            return "抱歉，AI 助手响应超时或出错，请稍后重试。";
        }
    }

    private List<Map<String, Object>> findLocalMovies(String aiResponse) {
        List<Map<String, Object>> links = new ArrayList<>();
        if (aiResponse == null || aiResponse.isBlank()) return links;

        Set<String> titles = new LinkedHashSet<>();
        Matcher matcher = BOOKMARK_PATTERN.matcher(aiResponse);
        while (matcher.find()) {
            String title = matcher.group(1).trim();
            if (!title.isEmpty() && title.length() <= 150) {
                titles.add(title);
            }
        }

        if (titles.isEmpty()) return links;

        String placeholders = String.join(",", Collections.nCopies(titles.size(), "?"));
        try {
            List<Map<String, Object>> rows = db.queryForList(
                    "SELECT id, title, poster_url posterUrl FROM movie WHERE title IN (" + placeholders + ") AND deleted = 0 AND status = 1 LIMIT 10",
                    titles.toArray()
            );
            links.addAll(rows);
        } catch (Exception e) {
            log.warn("Failed to query local movies for links: {}", e.getMessage());
        }
        return links;
    }
}
