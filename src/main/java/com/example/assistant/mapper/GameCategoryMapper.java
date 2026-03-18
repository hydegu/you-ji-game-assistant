package com.example.assistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.assistant.entity.GameCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 游戏分类Mapper接口
 */
@Mapper
public interface GameCategoryMapper extends BaseMapper<GameCategory> {
}
