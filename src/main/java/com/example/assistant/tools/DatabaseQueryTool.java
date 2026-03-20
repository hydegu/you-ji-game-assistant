package com.example.assistant.tools;

import com.example.assistant.component.GameVectorStoreFactory;
import com.example.assistant.entity.Game;
import com.example.assistant.mapper.GameMapper;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DatabaseQueryTool {
    private final GameMapper gameMapper;
    public record NoArgs() {}
    public Response query(Long gameId) {
        try {
            Game info = gameMapper.selectById(gameId);
            if (info == null) {
                return new Response("QUERY_RESULT: 未找到游戏信息，gameId=" + gameId);
            }
            return new Response(
                    "QUERY_RESULT: 游戏名称=" + info.getName()
                            + "，简介=" + info.getDescription()
            );
        } catch (Exception e) {
            return new Response("QUERY_RESULT: 查询失败，原因=" + e.getMessage());
        }
    }


    public record Response(String result) {}
}
