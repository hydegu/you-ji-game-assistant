package com.example.assistant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.assistant.entity.ChatMessage;
import com.example.assistant.entity.ChatSession;

import java.util.List;

/**
 * 对话会话 Service 接口
 *
 * 继承 IService<ChatSession> 获得通用 CRUD 方法（save, getById, list 等）。
 * 接口中只定义本业务特有的方法，通用 CRUD 由 IService 提供。
 */
public interface ChatSessionService extends IService<ChatSession> {

    /**
     * 幂等地创建或获取会话
     *
     * 幂等含义：无论调用多少次，数据库里只会存在一条 sessionId 对应的记录。
     * 原因：对话接口每次调用都会调用此方法，第一次时创建，后续都是查到已有记录直接返回。
     *
     * @param sessionId     会话唯一ID（前端生成的UUID）
     * @param userId        当前登录用户ID
     * @param gameId        对话关联的游戏ID
     * @param firstQuestion 第一条用户消息，用于生成会话标题
     * @return 会话实体（新建的或已存在的）
     */
    ChatSession createOrGet(String sessionId, Long userId, Long gameId, String firstQuestion);

    /**
     * 获取当前登录用户的所有会话列表
     * 按 updated_time 倒序（最近使用的在最前面）
     * 自动过滤已逻辑删除的会话（@TableLogic 自动加 AND deleted_time IS NULL）
     *
     * @return 当前用户的会话列表
     */
    List<ChatSession> listByCurrentUser();

    /**
     * 获取指定会话的消息历史
     * 会先验证该会话是否属于当前登录用户
     *
     * @param sessionId 会话ID
     * @return 按时间升序的消息列表
     */
    List<ChatMessage> getMessageHistory(String sessionId);

    /**
     * 软删除会话
     * 会先验证该会话是否属于当前登录用户
     * 软删除只修改 deleted_time，不删除数据（chat_message 记录保留）
     *
     * @param sessionId 会话ID
     */
    void deleteSession(String sessionId);

    /**
     * 验证会话归属权
     * 如果该 sessionId 不存在，或者不属于当前登录用户，则抛出 BusinessException
     *
     * @param sessionId 会话ID
     * @throws com.example.assistant.exception.BusinessException 会话不存在或无权访问
     */
    void validateOwnership(String sessionId);

    /**
     * 一轮对话结束后，更新持久化数据
     * 包含三个操作（在一个事务内）：
     *   1. 插入用户消息记录到 chat_message
     *   2. 插入AI回复记录到 chat_message
     *   3. 更新 chat_session 的 last_message、message_count、updated_time
     *
     * @param sessionId      会话ID
     * @param userContent    用户发送的问题
     * @param assistantContent AI 回复的内容
     */
    void onRoundComplete(String sessionId, String userContent, String assistantContent);
}