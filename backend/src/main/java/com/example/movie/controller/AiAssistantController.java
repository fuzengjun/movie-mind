package com.example.movie.controller;

import com.example.movie.common.Result;
import com.example.movie.dto.AiChatRequest;
import com.example.movie.service.AiAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody AiChatRequest request) {
        Map<String, Object> result = aiAssistantService.chat(request);
        return Result.success(result);
    }
}
