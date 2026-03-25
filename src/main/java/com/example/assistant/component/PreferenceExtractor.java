package com.example.assistant.component;

import com.example.assistant.constant.Prompts;
import com.example.assistant.pojo.UserGamePreference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PreferenceExtractor {

    private final ChatModel chatModel;

    /**
     * 从对话消息列表中提取偏好
     */
    public UserGamePreference extract(List<Message> messages) {
        // 把消息列表转成纯文本
        String conversation = messages.stream()
                .filter(m -> m instanceof UserMessage || m instanceof AssistantMessage)
                .map(m -> {
                    String role = m instanceof UserMessage ? "用户" : "助手";
                    return role + "：" + m.getText();
                })
                .collect(Collectors.joining("\n"));

        // 调用 LLM 提取
        String prompt = String.format(Prompts.PROMPT_PREFERENCE_EXTRACT, conversation);
        String jsonResult = ChatClient.builder(chatModel)
                .build()
                .prompt()
                .user(prompt)
                .call()
                .content();

        try {
            return new ObjectMapper().readValue(jsonResult.trim(), UserGamePreference.class);
        } catch (Exception e) {
            return new UserGamePreference(); // 全 null，视为空
        }
    }
}