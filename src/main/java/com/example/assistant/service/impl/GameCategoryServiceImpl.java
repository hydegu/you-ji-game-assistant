package com.example.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.assistant.entity.GameCategory;
import com.example.assistant.mapper.GameCategoryMapper;
import com.example.assistant.service.GameCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 游戏分类服务实现
 */
@Service
public class GameCategoryServiceImpl extends ServiceImpl<GameCategoryMapper, GameCategory>
        implements GameCategoryService {

    @Override
    public List<GameCategory> listAllOrderBySort() {
        return list(new LambdaQueryWrapper<GameCategory>()
                .orderByDesc(GameCategory::getSortOrder));
    }

    @Override
    public GameCategory findByName(String name) {
        return getOne(new LambdaQueryWrapper<GameCategory>()
                .eq(GameCategory::getName, name));
    }

    @Override
    public boolean existsByName(String name) {
        return count(new LambdaQueryWrapper<GameCategory>()
                .eq(GameCategory::getName, name)) > 0;
    }

    @Override
    public boolean existsById(Long id) {
        return count(new LambdaQueryWrapper<GameCategory>()
                .eq(GameCategory::getId, id)) > 0;
    }


}
