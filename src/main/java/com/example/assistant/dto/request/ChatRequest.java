package com.example.assistant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatRequest {

    @NotNull(message = "gameId不能为空")
    private Long gameId;

    @NotBlank(message = "问题不能为空")
    private String question;

    private Boolean isThinking = false;
}
