package com.example.movie.service;

import com.example.movie.dto.AiChatRequest;

import java.util.Map;

public interface AiAssistantService {
    Map<String, Object> chat(AiChatRequest request);
}
