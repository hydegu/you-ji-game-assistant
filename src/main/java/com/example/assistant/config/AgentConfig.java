package com.example.assistant.config;

import com.alibaba.cloud.ai.document.DocumentParser;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.parser.tika.TikaDocumentParser;
import com.alibaba.cloud.ai.transformer.splitter.SentenceSplitter;
import com.example.assistant.component.GameVectorStoreFactory;
import com.example.assistant.constant.Prompts;
import com.example.assistant.hooks.QueryEnhancementHook;
import com.example.assistant.intercepter.AnswerValidationInterceptor;
import com.example.assistant.tools.DatabaseQueryTool;
import com.example.assistant.tools.GameTool;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Function;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AgentConfig {

    @Value("${milvus.host}")
    private String host;
    @Value("${milvus.port}")
    private Integer port;
    @Value("${game.assistant.chunk-size}")
    private Integer chunkSize;

    private final ChatModel chatModel;

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

    @Bean
    @Qualifier("validAgent")
    public ReactAgent validAgent(){
        return ReactAgent.builder()
                .name("valid_agent")
                .model(chatModel)
                .instruction(Prompts.PROMPT_VALID)
                .description(Prompts.AGENT_VALID)
                .build();
    }

    /**
     * 切句子的Bean
     * @return
     */
    @Bean
    public SentenceSplitter sentenceSplitter(){
        return new SentenceSplitter(400);
    }

    /**
     * token计算
     */
    @Bean
    public TokenCountEstimator tokenCountEstimator(){
        return new JTokkitTokenCountEstimator();
    }

    /**
     * 文档解析
     */
    @Bean
    public DocumentParser parser(){
        return new TikaDocumentParser();
    }

    @Bean
    public TokenTextSplitter textSplitter(){
        return new TokenTextSplitter(
                chunkSize, // chunkSize
                50,        // minChunkSizeChars
                5,         // minChunkLengthToEmbed
                10000,     // maxNumChunks
                true       // keepSeparator
        );
    }
}
