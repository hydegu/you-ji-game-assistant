package com.example.assistant.service;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.example.assistant.dto.request.ChatRequest;
import com.example.assistant.dto.response.ChatResponse;
import org.springframework.ai.chat.messages.AssistantMessage;

public interface ChatService {
    AssistantMessage chat(ChatRequest request) throws GraphRunnerException;
}
