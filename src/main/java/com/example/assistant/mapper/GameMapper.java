package com.example.assistant.mapper;

import com.example.assistant.entity.Game;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 针对表【game】的数据库操作Mapper
 */
public interface GameMapper extends BaseMapper<Game> {

    /**
     * 查询游戏及其分类信息
     */
    Game selectGameWithCategoryById(@Param("id") Long id);
}