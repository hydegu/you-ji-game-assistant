package com.example.assistant.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class DelVectorDTO {

    @NotNull(message = "文档名不可为空")
    private String fileName;
}
