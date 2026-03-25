package com.example.assistant.constant;


import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class Prompts {

    private final ResourceLoader resourceLoader;

    /** 主游戏问答 Agent 的 instruction 模板，含 %d 占位符（gameId） */
    public static String PROMPT_MAIN;

    /** 回答质量验证 Agent 的 instruction */
    public static String PROMPT_VALID;

    /** 查询改写 PromptTemplate 内容，含 {target} / {query} 占位符 */
    public static String PROMPT_QUERY_REWRITE;

    /** 用户偏好提取 prompt 模板，含 %s 占位符（对话记录） */
    public static String PROMPT_PREFERENCE_EXTRACT;

    /** 验证不通过时的重试 system 提示 */
    public static final String PROMPT_VALIDATION_RETRY =
            "请重新检查你的答案，确保基于工具检索结果，不要编造内容，回答需准确完整。";

    /** validAgent 的 description */
    public static final String AGENT_VALID =
            "你是一个回答质量验证助手，负责对游戏问答助手的回答进行安全性与合规性审核，并以 JSON 格式输出验证结论。";

    /** 主游戏问答 Agent 的 description */
    public static final String AGENT_MAIN =
            "你是一个专业的游戏问答助手，仅依据工具检索结果回答用户的游戏相关问题，禁止编造内容。";

    public Prompts(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() throws Exception {
        PROMPT_MAIN = load("classpath:prompts/main/main-instruction.md");
        PROMPT_VALID = load("classpath:prompts/valid/valid-security.md");
        PROMPT_QUERY_REWRITE = load("classpath:prompts/query/rewrite-query.md");
        PROMPT_PREFERENCE_EXTRACT = load("classpath:prompts/preference/preference-extract.md");
    }

    private String load(String location) throws Exception {
        Resource resource = resourceLoader.getResource(location);
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }
}
