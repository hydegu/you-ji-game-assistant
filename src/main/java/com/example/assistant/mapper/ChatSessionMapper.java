package com.example.assistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.assistant.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话元数据 Mapper
 *
 * 继承 BaseMapper<ChatSession> 后，自动拥有以下方法（无需编写SQL）：
 *   - insert(ChatSession)             → INSERT INTO chat_session
 *   - selectById(Long)                → SELECT WHERE id=? AND deleted_time IS NULL
 *   - selectList(QueryWrapper)        → SELECT WHERE ... AND deleted_time IS NULL
 *   - updateById(ChatSession)         → UPDATE WHERE id=?
 *   - removeById(Long)                → UPDATE SET deleted_time=NOW() WHERE id=? （因为有@TableLogic）
 *
 * 以上方法已足够，无需自定义 SQL，所以没有对应的 XML 文件。
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
    // BaseMapper 提供的 CRUD 方法已满足需求，无需添加自定义方法
}