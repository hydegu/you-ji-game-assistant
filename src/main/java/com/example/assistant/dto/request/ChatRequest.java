package com.example.assistant.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatRequest {

    @NotNull(message = "gameId不能为空")
    @Schema(description = "游戏id")
    private Long gameId;

    @NotBlank(message = "问题不能为空")
    @Schema(description = "问题")
    private String question;

    @NotNull(message = "会话id不可为空")
    @Schema(description = "会话id")
    private String sessionId;

    @Schema(description = "是否开启思考模式")
    private Boolean isThinking = false;
}
