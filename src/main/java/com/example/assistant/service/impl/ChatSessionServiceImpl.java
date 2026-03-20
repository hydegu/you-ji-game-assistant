package com.example.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.assistant.entity.ChatMessage;
import com.example.assistant.entity.ChatSession;
import com.example.assistant.exception.BusinessException;
import com.example.assistant.mapper.ChatMessageMapper;
import com.example.assistant.mapper.ChatSessionMapper;
import com.example.assistant.service.ChatSessionService;
import com.example.assistant.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 对话会话 Service 实现
 *
 * ServiceImpl<ChatSessionMapper, ChatSession> 已内置：
 *   - save()         → INSERT
 *   - getById()      → SELECT WHERE id=? AND deleted_time IS NULL
 *   - list(wrapper)  → SELECT WHERE ... AND deleted_time IS NULL
 *   - updateById()   → UPDATE WHERE id=?
 *   - removeById()   → UPDATE SET deleted_time=NOW() WHERE id=? （因为有@TableLogic）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession>
        implements ChatSessionService {

    // 注意：ChatSessionMapper 不需要显式注入，ServiceImpl 内部已持有（this.baseMapper）
    // 但 ChatMessageMapper 是额外依赖，需要单独注入
    private final ChatMessageMapper chatMessageMapper;

    @Override
    @Transactional  // 保证"检查是否存在"和"插入"是原子操作
    public ChatSession createOrGet(String sessionId, Long userId, Long gameId, String firstQuestion) {
        // 先查询是否已存在（后续调用的幂等处理）
        // getOne() 是 ServiceImpl 提供的方法，找不到返回 null
        ChatSession existing = getOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sessionId));

        if (existing != null) {
            // 已存在，直接返回，不做任何修改
            return existing;
        }

        // 不存在，新建会话记录
        // 会话标题 = 首条消息的前20个字 + "..."（如果超过20字）
        String title;
        if (firstQuestion == null || firstQuestion.isBlank()) {
            title = "新对话";
        } else if (firstQuestion.length() > 20) {
            title = firstQuestion.substring(0, 20) + "...";
        } else {
            title = firstQuestion;
        }

        ChatSession session = new ChatSession();
        session.setSessionId(sessionId);
        session.setUserId(userId);
        session.setGameId(gameId);
        session.setTitle(title);
        session.setLastMessage("");    // 初始为空，第一轮对话结束后更新
        session.setMessageCount(0);   // 初始为0

        // createdTime 和 updatedTime 由 MyBatisMetaObjectHandler 自动填充，无需手动设置

        try {
            save(session);  // 对应 INSERT INTO chat_session ...
            log.info("新建会话: sessionId={}, userId={}, gameId={}, title={}", sessionId, userId, gameId, title);
        } catch (DuplicateKeyException e) {
            // 极端并发情况：两个请求同时用相同 sessionId 到达
            // 第一个 INSERT 成功，第二个触发唯一键冲突
            // 此时查询一次已存在的记录返回即可
            log.warn("会话创建冲突（并发），查询已有记录: sessionId={}", sessionId);
            return getOne(new LambdaQueryWrapper<ChatSession>()
                    .eq(ChatSession::getSessionId, sessionId));
        }

        return session;
    }

    @Override
    public List<ChatSession> listByCurrentUser() {
        // 获取当前登录用户ID（SecurityUtils 会从 SecurityContextHolder 中读取）
        Long userId = SecurityUtils.getCurrentUserId();

        // LambdaQueryWrapper 使用方法引用而非字符串，避免拼写错误
        // MyBatis Plus 会自动追加 AND deleted_time IS NULL（因为 @TableLogic）
        return list(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .orderByDesc(ChatSession::getUpdatedTime));  // 最近使用的在最前面
    }

    @Override
    public List<ChatMessage> getMessageHistory(String sessionId) {
        // 先验证归属权，防止 A 用户查看 B 用户的消息历史
        validateOwnership(sessionId);
        // 调用自定义 XML 查询，按时间升序返回所有消息
        return chatMessageMapper.selectBySessionId(sessionId);
    }

    @Override
    @Transactional
    public void deleteSession(String sessionId) {
        // 先验证归属权
        validateOwnership(sessionId);

        // 查出数据库主键 id（removeById 需要主键）
        ChatSession session = getOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sessionId));

        if (session != null) {
            // removeById 因为 @TableLogic 会执行：
            // UPDATE chat_session SET deleted_time = NOW() WHERE id = ?
            // 而不是 DELETE FROM chat_session WHERE id = ?
            removeById(session.getId());
            log.info("软删除会话: sessionId={}", sessionId);
        }
    }

    @Override
    public void validateOwnership(String sessionId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 同时匹配 sessionId 和 userId，只有两者都匹配才能找到记录
        // MyBatis Plus 自动追加 AND deleted_time IS NULL
        ChatSession session = getOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sessionId)
                .eq(ChatSession::getUserId, currentUserId));

        if (session == null) {
            // 记录不存在（可能未建立、已删除、或不属于当前用户）
            throw new BusinessException("会话不存在或无权访问");
        }
    }

    @Override
    @Transactional  // 三个数据库操作必须在同一事务内，任何一个失败则全部回滚
    public void onRoundComplete(String sessionId, String userContent, String assistantContent) {

        // === 操作1：插入用户消息 ===
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(userContent);
        // createdTime 由 MyBatisMetaObjectHandler 自动填充
        chatMessageMapper.insert(userMsg);

        // === 操作2：插入AI回复消息 ===
        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(assistantContent);
        chatMessageMapper.insert(assistantMsg);

        // === 操作3：更新会话元数据 ===
        // last_message 只保留前200字（避免 VARCHAR(255) 截断）
        String preview;
        if (assistantContent == null) {
            preview = "";
        } else if (assistantContent.length() > 200) {
            preview = assistantContent.substring(0, 200) + "...";
        } else {
            preview = assistantContent;
        }

        ChatSession session = getOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getSessionId, sessionId));

        if (session != null) {
            session.setLastMessage(preview);
            session.setMessageCount(session.getMessageCount() + 2);  // 用户消息 + AI回复 = 2条
            // updatedTime 由 MyBatisMetaObjectHandler 在 UPDATE 时自动刷新，无需手动设置
            updateById(session);
        } else {
            log.warn("onRoundComplete: 找不到会话记录，sessionId={}", sessionId);
        }
    }
}