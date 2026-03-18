package com.example.assistant.controller;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.example.assistant.dto.request.ChatRequest;
import com.example.assistant.dto.response.ApiResponse;
import com.example.assistant.dto.response.ChatResponse;
import com.example.assistant.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/ask")
    public ApiResponse<AssistantMessage> chat(@Valid @RequestBody ChatRequest request) throws GraphRunnerException {
        return ApiResponse.ok(chatService.chat(request));
    }
}
