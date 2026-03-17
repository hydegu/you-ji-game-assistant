package com.example.assistant.component;

import com.example.assistant.constant.DataBase;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameVectorStoreFactory {

    private final MilvusServiceClient milvusClient;
    private final EmbeddingModel embeddingModel;
    
    // 缓存：避免同一个游戏重复创建
    private final Map<Long, VectorStore> cache = new ConcurrentHashMap<>();

    public GameVectorStoreFactory(MilvusServiceClient milvusClient, 
                                   EmbeddingModel embeddingModel) {
        this.milvusClient = milvusClient;
        this.embeddingModel = embeddingModel;
    }

    /**
     * 根据游戏数据库主键获取对应的 VectorStore
     * @param gameId 游戏表的主键 (Long 类型，如 1001L)
     */
    public VectorStore getStore(Long gameId) {
        return cache.computeIfAbsent(gameId, id ->
                MilvusVectorStore.builder(milvusClient, embeddingModel)
                        .collectionName("game_" + id)  // "game_1001"
                        .databaseName(DataBase.NAME)
                        .embeddingDimension(1024)
                        .indexType(IndexType.IVF_FLAT)
                        .metricType(MetricType.COSINE)
                        .batchingStrategy(new TokenCountBatchingStrategy())
                        .initializeSchema(true)
                        .build()
        );
    }

}