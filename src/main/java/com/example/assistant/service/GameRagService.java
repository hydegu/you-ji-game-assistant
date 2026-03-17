package com.example.assistant.service;

import com.example.assistant.component.GameVectorStoreFactory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameRagService {

    private final GameVectorStoreFactory factory;

    public GameRagService(GameVectorStoreFactory factory) {
        this.factory = factory;
    }

    // 存入某游戏的文档
    public void ingestDocument(Long gameId, List<Document> docs) {
        factory.getStore(gameId).add(docs);
    }

    // 查询某游戏相关内容
    public List<Document> search(Long gameId, String query) {
        return factory.getStore(gameId)
                      .similaritySearch(query);
    }
}