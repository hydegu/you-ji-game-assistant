package com.example.assistant.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 分类创建请求
 */
@Data
public class CategoryCreateRequest {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String description;

    private Integer sortOrder = 0;
}
