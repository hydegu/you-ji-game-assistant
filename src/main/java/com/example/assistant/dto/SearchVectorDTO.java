package com.example.assistant.dto;

import com.example.assistant.annotation.AtLeastOneNotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SearchVectorDTO {

    @Schema(description = "关键词，用于模糊搜索")
    private String keyword = "";
}
