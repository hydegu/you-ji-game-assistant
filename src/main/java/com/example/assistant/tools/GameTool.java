package com.example.assistant.tools;

import com.example.assistant.component.GameVectorStoreFactory;
import com.example.assistant.dto.response.ApiResponse;
import com.example.assistant.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class GameTool {

    private final ChatClient chatClient;
    private final GameVectorStoreFactory factory;

    // 存入某游戏的文档
    @Tool(description = "保存游戏的文档")
    public void ingestDocument(Long gameId, List<Document> docs) {
        factory.getStore(gameId).add(docs);
    }

    // 查询某游戏相关内容
    @Tool(description = "查询游戏相关内容")
    public String search(Long gameId, String query, Integer topK) {
        String hypotheticalDoc = chatClient.prompt()
                .user("请根据以下问题，生成一段简短的假设性回答（仅用于检索，不是最终答案）：\n" + query)
                .call()
                .content();
        List<Document> docs = factory.getStore(gameId).similaritySearch(
                SearchRequest.builder()
                        .query(hypotheticalDoc)
                        .topK(topK)
                        .similarityThreshold(0.5) //过滤相关度低的文档 降低幻觉
                        .build()
        );
        // 没查到资料，LLM自动走澄清策略/兜底策略
        if (docs == null || docs.isEmpty()) {
            return "RETRIEVAL_RESULT: 未检索到相关文档，query=" + query;
        }

        return docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining(""));
    }
}
