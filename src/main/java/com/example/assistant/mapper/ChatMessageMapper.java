package com.example.assistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.assistant.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 消息记录 Mapper
 *
 * 除了 BaseMapper 的标准方法外，额外定义了一个按会话ID查询并排序的方法。
 * 为什么不用 BaseMapper 的 selectList(wrapper)?
 * 因为我们需要 ORDER BY created_time ASC, id ASC 保证时间顺序，
 * 虽然 QueryWrapper 也能写 orderByAsc，但习惯上有明确排序需求的查询用 XML 更清晰。
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 按会话ID查询该会话的所有消息，按时间升序排列
     *
     * @param sessionId 会话ID（对应 chat_session.session_id）
     * @return 消息列表，按 created_time ASC, id ASC 排序
     */
    List<ChatMessage> selectBySessionId(String sessionId);
}