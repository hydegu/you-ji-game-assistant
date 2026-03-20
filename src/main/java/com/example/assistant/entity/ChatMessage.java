package com.example.assistant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话消息实体
 *
 * 对应数据库表 chat_message，存储每条具体的消息（用户发的和AI回复的）。
 * 消息是不可变的：写入后不需要修改，所以没有 updatedTime 字段。
 * 消息不需要单独软删除：会话删除后消息自然不可见。
 */
@Data
@TableName("chat_message")
public class ChatMessage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的会话ID，对应 chat_session.session_id
     * 注意：这不是外键，是逻辑关联（不使用数据库外键约束）
     */
    private String sessionId;

    /**
     * 消息角色，只有两种值：
     *   "user"      = 用户发送的消息
     *   "assistant" = AI回复的消息
     */
    private String role;

    /** 消息正文，长度不限，对应数据库 TEXT 类型 */
    private String content;

    /**
     * 消息时间，INSERT 时由 MyBatisMetaObjectHandler 自动填充
     * 使用 DATETIME(3) 精度到毫秒，保证同一秒内的消息也能正确排序
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}