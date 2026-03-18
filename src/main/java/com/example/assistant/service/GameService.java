package com.example.assistant.service;

import com.example.assistant.entity.Game;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 游戏服务接口
 */
public interface GameService extends IService<Game> {

    /**
     * 根据分类ID查询游戏列表
     */
    List<Game> listByCategoryId(Long categoryId);

    /**
     * 查询上架游戏列表
     */
    List<Game> listActive();

    /**
     * 查询游戏及其分类信息
     */
    Game getWithCategoryById(Long id);

    /**
     * 根据名称模糊查询
     */
    List<Game> searchByName(String name);

    /**
     * 切换游戏上下架状态
     */
    boolean toggleStatus(Long id);

    /**
     * 检查游戏名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 检查分类id是否存在
     */
    boolean existsById(Long id);
}