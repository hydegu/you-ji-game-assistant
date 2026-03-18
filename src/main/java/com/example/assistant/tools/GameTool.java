package com.example.assistant.tools;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.example.assistant.component.GameVectorStoreFactory;
import com.example.assistant.dto.response.ApiResponse;
import com.example.assistant.exception.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component
public class GameTool {

    private final GameVectorStoreFactory factory;
    public record Request(
            @JsonProperty(required = true) String query,
            @JsonProperty Integer topK
    ) {}
    public record Response(String content) {}

    // 存入某游戏的文档
    @Tool(description = "保存游戏的文档")
    public void ingestDocument(Long gameId, List<Document> docs) {
        factory.getStore(gameId).add(docs);
    }

    // 查询某游戏相关内容
    @Tool(description = "查询游戏相关内容")
    public Response search(Long gameId, Request req) {

        List<Document> docs = factory.getStore(gameId).similaritySearch(
                SearchRequest.builder()
                        .query(req.query())
                        .topK(req.topK())
                        .similarityThreshold(0.5) //过滤相关度低的文档 降低幻觉
                        .build()
        );
        // 没查到资料，LLM自动走澄清策略/兜底策略
        if (docs.isEmpty()) {
            return new Response("RETRIEVAL_RESULT: 未检索到相关文档，query=" + req.query());
        }

        return new Response(docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("")));
    }

}
