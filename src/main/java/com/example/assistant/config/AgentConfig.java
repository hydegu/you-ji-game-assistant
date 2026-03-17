package com.example.assistant.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

    @Value("${milvus.host}")
    private String host;
    @Value("${milvus.port}")
    private Integer port;

    /**
     * 定义一个名为 milvusServiceClient 的Bean，用于创建并返回一个 MilvusServiceClient 实例。
     */
    @Bean
    public MilvusServiceClient milvusServiceClient() {
        return new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withHost(host)
                        .withPort(port)
                        .build());
    }

    @Bean
    public RewriteQueryTransformer rewriteQueryTransformer(ChatClient.Builder builder) {
        return RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .targetSearchSystem("游戏知识库")  // 设定改写场景
                .build();
    }
}
