package com.example.assistant.component;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import lombok.AllArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
public class GameVectorStoreFactory {

    private final MilvusServiceClient milvusClient;
    private final EmbeddingModel embeddingModel;
    
    // 缓存：避免同一个Vector重复创建
    private final Map<Long, VectorStore> cache = new ConcurrentHashMap<>();

    /**
     * 根据游戏数据库主键获取对应的 VectorStore
     * @param gameId 游戏表的主键 (Long 类型，如 1001L)
     */
    public VectorStore getStore(Long gameId) {
        return cache.computeIfAbsent(gameId, id ->
                MilvusVectorStore.builder(milvusClient, embeddingModel)
                        .collectionName("game_" + id)  // "game_1001"
                        .databaseName("default")
                        .indexType(IndexType.IVF_FLAT)
                        .metricType(MetricType.COSINE)
                        .embeddingDimension(1024)
                        .initializeSchema(true)
                        .build()
        );
    }

}