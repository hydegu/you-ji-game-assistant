package com.example.assistant.config;

import com.alibaba.cloud.ai.document.DocumentParser;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.messages.MessagesModelHook;
import com.alibaba.cloud.ai.graph.agent.hook.summarization.SummarizationHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.CreateOption;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.MysqlSaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.alibaba.cloud.ai.graph.serializer.plain_text.jackson.JacksonStateSerializer;
import com.alibaba.cloud.ai.graph.store.stores.DatabaseStore;
import com.alibaba.cloud.ai.graph.store.stores.MemoryStore;
import com.alibaba.cloud.ai.graph.store.stores.RedisStore;
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
import org.apache.tika.Tika;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
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

import javax.sql.DataSource;
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
    private final Prompts prompts;

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

    @Bean
    public Tika tika(){
        return new Tika();
    }

    @Bean
    public MemorySaver mysqlSaver(DataSource datasource){
        return new MysqlSaver.Builder()
                .createOption(CreateOption.CREATE_IF_NOT_EXISTS)
                .dataSource(datasource)
                .build();
    }


    @Bean("summarize")
    public MessagesModelHook summarizationHook(){
        // 创建消息压缩 Hook
        return SummarizationHook.builder()
                .model(chatModel)
                .maxTokensBeforeSummary(4000)
                .messagesToKeep(20)
                .build();
    }

    @Bean
    public RedisStore memoryStore(DataSource dataSource){
        return new RedisStore("userProfiles:");
    }

    @Bean
    public RewriteQueryTransformer rewriteQueryTransformer(ChatClient.Builder builder) {
        return RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .targetSearchSystem("游戏知识库")
                .promptTemplate(new PromptTemplate("""
                 你的任务是改写用户的查询，使其更适合在{target}中检索。

                 【重要规则】
                 如果用户的输入是以下类型，请原样返回，不做任何改写：
                 - 关于对话本身的问题（如"我刚才说了什么"、"你之前回答了什么"）
                 - 日常寒暄或闲聊（如"你好"、"谢谢"）
                 - 对上一条回答的追问（如"为什么"、"能详细说说吗"）

                 只对明确询问游戏内容的问题进行改写，使其更具体、更适合检索。

                 用户输入：{query}
                 改写后的查询：
                 """))
                .build();
    }
}
