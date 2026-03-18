package com.example.assistant.constant;


import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.nio.charset.StandardCharsets;

public class Prompts {

    private final ResourceLoader resourceLoader;
    public static String PROMPT_VALID;
    public static String AGENT_VALID = """
            这是回答验证助手，负责验证回答信息是否安全且合格
            """;
    public static String AGENT_MAIN = """
            这是主要的游戏问答助手，负责解答用户的游戏问题
            """;

    // 构造注入
    public Prompts(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    // 项目启动时自动加载文件内容
    @PostConstruct
    public void init() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:prompts/valid/valid-security.md");
        PROMPT_VALID = resource.getContentAsString(StandardCharsets.UTF_8);
    }
}
