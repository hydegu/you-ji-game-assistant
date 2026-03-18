package com.example.assistant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.assistant.entity.GameCategory;

import java.util.List;

/**
 * 游戏分类服务接口
 */
public interface GameCategoryService extends IService<GameCategory> {

    /**
     * 获取所有分类（按排序权重降序）
     */
    List<GameCategory> listAllOrderBySort();

    /**
     * 根据名称查询分类
     */
    GameCategory findByName(String name);

    /**
     * 检查分类名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 检查分类id是否存在
     */
    boolean existsById(Long id);
}
