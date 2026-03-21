package com.example.assistant.controller;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.example.assistant.dto.request.ChatRequest;
import com.example.assistant.dto.response.ApiResponse;
import com.example.assistant.dto.response.ChatResponse;
import com.example.assistant.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Tag(name = "AI对话", description = "与游戏AI助手进行对话")
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "发送消息", description = "向AI助手提问，基于游戏知识库进行回答")
    @PostMapping("/ask")
    public ApiResponse<AssistantMessage> chat(@Valid @RequestBody ChatRequest request) throws GraphRunnerException {
        return ApiResponse.ok(chatService.chat(request));
    }



    @Operation(summary = "提问（流式）")
    @PostMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@Valid @RequestBody ChatRequest request) throws GraphRunnerException {
        return chatService.chatAsStream(request);
    }
}
