package com.example.assistant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 游戏创建请求
 */
@Data
public class GameCreateRequest {

    @NotBlank(message = "游戏名称不能为空")
    private String name;

    private String description;

    private String image;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    private Integer status = 1;

    private String developer;
}
