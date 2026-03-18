package com.example.assistant.dto.request;

import lombok.Data;

/**
 * 游戏更新请求
 */
@Data
public class GameUpdateRequest {

    private String name;

    private String description;

    private String image;

    private Long categoryId;

    private Integer status;

    private String developer;
}
