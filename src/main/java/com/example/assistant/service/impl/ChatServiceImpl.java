package com.example.assistant.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.messages.MessagesModelHook;
import com.alibaba.cloud.ai.graph.agent.interceptor.todolist.TodoListInterceptor;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.example.assistant.constant.Prompts;
import com.example.assistant.dto.request.ChatRequest;
import com.example.assistant.dto.response.ChatResponse;
import com.example.assistant.exception.BusinessException;
import com.example.assistant.hooks.PreferenceLearningHook;
import com.example.assistant.hooks.QueryEnhancementHook;
import com.example.assistant.intercepter.AnswerValidationInterceptor;
import com.example.assistant.service.ChatService;
import com.example.assistant.tools.DatabaseQueryTool;
import com.example.assistant.tools.GameTool;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final DatabaseQueryTool dbQueryTool;
    private final GameTool gameTool;
    private final ChatModel chatModel;
    private final QueryEnhancementHook queryEnhancementHook;
    private final AnswerValidationInterceptor answerValidationInterceptor;
    @Qualifier("summarize")
    private final MessagesModelHook summarizationHook;
    private final PreferenceLearningHook preferenceLearningHook;

    @Override
    public AssistantMessage chat(ChatRequest request) throws GraphRunnerException {
        log.info("RAG调用开始");
        // 数据库搜索 这个工具其实可以不用tools而是使用methodtools传递
        // 不过因为文档搜索工具是动态传递gameId，所以顺便用toolCallBack包一层然后直接用tools
        ToolCallback databaseQueryCallback = buildCallback("database_query",
                dbQueryTool::query, Long.class, "查询内部数据库");

        // 依赖 gameId 的
        ToolCallback documentSearchCallback = FunctionToolCallback.builder(
                "document_search",
                        (Function<GameTool.Request, GameTool.Response>) req -> gameTool.search(request.getGameId(),req))
                .description("搜索文档库")
                .inputType(GameTool.Request.class)
                .build();

        ReactAgent agent = ReactAgent.builder()
                .name("game_agent_" + request.getGameId())
                .model(chatModel)
                .hooks(queryEnhancementHook,summarizationHook,preferenceLearningHook) //增强消息、自动压缩会话、自动生成用户画像（长期记忆）
                .interceptors(answerValidationInterceptor, TodoListInterceptor.builder().build()) //验证回答、自动规划
                .description(Prompts.AGENT_MAIN)
                .enableLogging(true)
                .instruction("你可以访问两个信息源：" +
                        "1. database_query - 用于内部数据\n" +
                        "2. documentSearch - 用于文档库\n" +
                        "根据问题选择最合适的工具。")
                .chatOptions(DashScopeChatOptions.builder()
                        .temperature(0.3)
                        .topP(0.7)
                        .enableThinking(request.getIsThinking())
                        .maxToken(1024)
                        .build())
                .tools(List.of(databaseQueryCallback, documentSearchCallback))
                .build();
        AssistantMessage response = agent.call(request.getQuestion());
        log.info("RAG调用完成");
        return response;
    }

    // 工厂方法
    private <Req, Res> ToolCallback buildCallback(
            String name,
            Function<Req, Res> fn,
            Class<Req> inputType,
            String description) {
        return FunctionToolCallback.builder(name, fn)
                .description(description)
                .inputType(inputType)
                .build();
    }
}
