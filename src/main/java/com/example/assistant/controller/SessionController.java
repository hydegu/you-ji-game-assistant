package com.example.assistant.controller;

import com.example.assistant.dto.response.ApiResponse;
import com.example.assistant.entity.ChatMessage;
import com.example.assistant.entity.ChatSession;
import com.example.assistant.service.ChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 对话会话管理 Controller
 *
 * 路径设计：/chat/sessions/**
 * 为什么这样设计？
 *   现有 SecurityConfig 已有规则：.requestMatchers("/chat/**").hasAnyRole("USER", "ADMIN")
 *   /chat/sessions/** 自动继承此权限规则，无需修改 SecurityConfig。
 */
@Tag(name = "会话管理", description = "AI对话会话的增删查")
@RestController
@RequestMapping("/chat/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final ChatSessionService chatSessionService;

    /**
     * 新建会话 —— 获取一个新的 sessionId
     *
     * 为什么这里只返回 UUID，不建立数据库记录？
     * 因为我们无法在这里知道"第一条消息是什么"（标题需要用第一条消息生成）。
     * 实际的 chat_session 记录在第一次 POST /chat/ask 时才创建。
     * 这样避免了"用户拿了 sessionId 但从未发消息"产生的空会话记录。
     *
     * @return 新生成的 sessionId（UUID格式）
     */
    @Operation(summary = "创建新会话", description = "生成新的会话ID，后续发消息时携带此ID")
    @PostMapping
    public ApiResponse<String> createSession() {
        return ApiResponse.ok(UUID.randomUUID().toString());
    }

    /**
     * 获取当前用户的会话列表
     * 按最后活跃时间倒序，已删除的不展示
     *
     * @return 当前用户的会话列表
     */
    @Operation(summary = "会话列表", description = "返回当前登录用户的所有未删除会话，按最近使用排序")
    @GetMapping
    public ApiResponse<List<ChatSession>> listSessions() {
        return ApiResponse.ok(chatSessionService.listByCurrentUser());
    }

    /**
     * 获取指定会话的消息历史
     * 会验证归属权，只有会话所有者才能查看
     *
     * @param sessionId URL路径中的会话ID
     * @return 消息列表，按时间升序（最早的消息在最前面）
     */
    @Operation(summary = "消息历史", description = "返回指定会话的所有消息，按时间升序排列")
    @GetMapping("/{sessionId}/messages")
    public ApiResponse<List<ChatMessage>> getHistory(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        return ApiResponse.ok(chatSessionService.getMessageHistory(sessionId));
    }

    /**
     * 软删除会话
     * 不删除数据库记录，只设置 deleted_time，会话从列表中消失
     * 底层 MysqlSaver 的 checkpoint 数据依然保留（AI状态不丢失）
     *
     * @param sessionId URL路径中的会话ID
     */
    @Operation(summary = "删除会话", description = "软删除指定会话（数据保留，仅不可见）")
    @DeleteMapping("/{sessionId}")
    public ApiResponse<Void> deleteSession(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        chatSessionService.deleteSession(sessionId);
        return ApiResponse.ok();
    }
}