package com.example.assistant.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.messages.MessagesModelHook;
import com.alibaba.cloud.ai.graph.agent.interceptor.todolist.TodoListInterceptor;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.store.stores.RedisStore;
import com.example.assistant.component.UserPreferenceStore;
import com.example.assistant.constant.Prompts;
import com.example.assistant.dto.request.ChatRequest;
import com.example.assistant.dto.response.ChatResponse;
import com.example.assistant.exception.BusinessException;
import com.example.assistant.hooks.PreferenceLearningHook;
import com.example.assistant.hooks.QueryEnhancementHook;
import com.example.assistant.intercepter.AnswerValidationInterceptor;
import com.example.assistant.service.ChatService;
import com.example.assistant.service.ChatSessionService;
import com.example.assistant.tools.DatabaseQueryTool;
import com.example.assistant.tools.GameTool;
import com.example.assistant.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.tools.Tool;
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
    private final MemorySaver mysqlSaver;
    private final RedisStore redisStore;
    private final ChatSessionService chatSessionService;

    @Override
    public AssistantMessage chat(ChatRequest request) throws GraphRunnerException {
        log.info("RAG调用开始");
        // 获取当前登录用户的 ID
        Long userId = SecurityUtils.getCurrentUserId();
        // ========================================================
        // 【新增 Step A】幂等创建会话元数据
        // 首次调用时：在 chat_session 表新建一行，标题取自 question
        // 后续调用时：找到已有记录直接返回，不做任何修改
        // 必须放在 agent.call() 之前，这样 firstQuestion 才能被捕获为标题
        // ========================================================
        chatSessionService.createOrGet(
                request.getSessionId(),
                userId,
                request.getGameId(),
                request.getQuestion()
        );
        // 数据库搜索 这个工具其实可以不用tools而是使用methodtools传递
        // 不过因为文档搜索工具是动态传递gameId，所以顺便用toolCallBack包一层然后直接用tools
//        ToolCallback databaseQueryCallback = buildCallback("database_query",
//                dbQueryTool::query, Long.class, "查询内部数据库");
        ToolCallback databaseQueryCallback = FunctionToolCallback.builder(
                "database_query",
                (Function<DatabaseQueryTool.NoArgs,DatabaseQueryTool.Response>) args -> dbQueryTool.query(request.getGameId()))
                .description("查询当前游戏的基本信息，无需传入任何参数")
                .inputType(DatabaseQueryTool.NoArgs.class)
                .build();

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
                .saver(mysqlSaver)
                .instruction(String.format("""
                        当前用户正在咨询 gameId=%d 的游戏助手。
                        
                             【工具说明】
                             1. database_query - 查询当前游戏基本信息（名称、简介），无需传参
                             2. document_search - 搜索游戏详细文档（攻略、角色、剧情等）
                        
                             【何时调用工具】
                             仅当用户询问游戏内容时（如角色技能、攻略、剧情、版本信息等）才调用工具。
                        
                             【何时直接回答，不得调用工具】
                             以下情况直接根据对话上下文回答，禁止调用任何工具：
                             - 询问对话历史（如"我刚才说了什么"、"你上次回答了什么"）
                             - 日常寒暄（如"你好"、"谢谢"）
                             - 对已有回答的追问、确认或反馈
                        """, request.getGameId()))
                .chatOptions(DashScopeChatOptions.builder()
                        .temperature(0.3)
                        .topP(0.7)
                        .enableThinking(request.getIsThinking())
                        .maxToken(1024)
                        .build())
                .tools(List.of(databaseQueryCallback, documentSearchCallback))
                .build();

        RunnableConfig config = RunnableConfig.builder()
                .threadId(request.getSessionId())
                .addMetadata("user_id", SecurityUtils.getCurrentUserId())
                .store(redisStore)
                .build();

        AssistantMessage response = agent.call(request.getQuestion(),config);
        log.info("RAG调用完成");
        // ========================================================
        // 【新增 Step B】持久化本轮消息到 chat_message 表，更新会话元数据
        // 放在 agent.call() 之后：只有 AI 成功回复了才记录
        // 如果 agent.call() 抛异常，此处不执行，消息不会被错误写入
        // ========================================================
        String answerText = response.getText() != null ? response.getText() : "[无文字回复]";
        chatSessionService.onRoundComplete(
                request.getSessionId(),
                request.getQuestion(),
                answerText
        );
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
