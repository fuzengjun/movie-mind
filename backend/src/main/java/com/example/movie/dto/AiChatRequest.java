package com.example.movie.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class AiChatRequest {
    private List<Map<String, String>> messages;
    private Map<String, Object> userContext;

    public AiChatRequest() {
        this.messages = new ArrayList<>();
    }
}
