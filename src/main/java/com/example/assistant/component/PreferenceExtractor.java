package com.example.assistant.component;

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

    private static final String EXTRACT_PROMPT = """
        你是一个信息提取助手，请从下面的对话记录中提取用户的游戏偏好信息。
        
        只提取明确提到的信息，没有提到的字段返回 null。
        必须返回合法的 JSON，不要有任何其他文字。
        
        需要提取的字段：
        - currentGame: 当前在玩的游戏
        - chapter: 游戏进度/章节
        - playstyle: 游戏风格（如：全收集、速通、剧情向）
        - difficulty: 偏好难度
        - favoriteGenre: 喜欢的游戏类型
        
        对话记录：
        %s
        
        返回格式示例：
        {"currentGame":"黑神话：悟空","chapter":"第三回","playstyle":"全成就","difficulty":null,"favoriteGenre":null}
        """;

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
        String prompt = String.format(EXTRACT_PROMPT, conversation);
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