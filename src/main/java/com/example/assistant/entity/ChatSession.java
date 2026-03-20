package com.example.assistant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话会话实体
 *
 * 对应数据库表 chat_session，存储每个AI对话会话的元数据。
 * 与 MysqlSaver 的 checkpoint 是"影子关系"：
 *   - MysqlSaver 存 Agent 内部状态（AI能读，前端不能读）
 *   - 本表存会话元信息（前端可读，用于展示列表/标题）
 */
@Data
@TableName("chat_session")
public class ChatSession implements Serializable {

    /**
     * 主键，数据库自增
     * IdType.AUTO = 让数据库负责自增，不是MyBatis Plus生成
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话唯一ID（UUID字符串）
     * 这个值同时是 MysqlSaver 的 threadId，两边通过这个字段关联
     */
    private String sessionId;

    /** 会话所属用户的ID */
    private Long userId;

    /** 对话关联的游戏ID */
    private Long gameId;

    /**
     * 会话标题
     * 由第一条用户消息的前20个字生成，便于用户识别
     */
    private String title;

    /**
     * 最后一条AI回复的预览（截取前200字）
     * 在会话列表中展示，让用户快速了解该对话内容
     */
    private String lastMessage;

    /**
     * 该会话的消息总条数（user消息 + assistant消息合计）
     * 冗余字段：避免每次展示列表都 COUNT chat_message 表
     */
    private Integer messageCount;

    /**
     * 创建时间，由 MyBatisMetaObjectHandler 在 INSERT 时自动填充
     * 注意：字段名必须是 createdTime（驼峰），对应列名 created_time
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 最后更新时间，由 MyBatisMetaObjectHandler 在 INSERT 和 UPDATE 时自动填充
     * 每次对话结束后会更新此字段，用于按"最近使用"排序
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 逻辑删除时间
     *
     * @TableLogic 注解让 MyBatis Plus 自动处理软删除：
     *   - 查询时自动追加 AND deleted_time IS NULL
     *   - 删除时执行 UPDATE SET deleted_time = NOW() 而不是 DELETE
     *
     * 注意：不能依赖 application.yaml 里的 logic-delete-field: deleted_at
     * 因为那个全局配置与本项目的字段名不匹配，必须在每个实体上显式标注 @TableLogic
     */
    @TableLogic
    private LocalDateTime deletedTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}