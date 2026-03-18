package com.example.assistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.assistant.entity.Game;
import com.example.assistant.exception.BusinessException;
import com.example.assistant.mapper.GameMapper;
import com.example.assistant.service.GameService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 游戏服务实现
 */
@Service
public class GameServiceImpl extends ServiceImpl<GameMapper, Game>
        implements GameService {

    @Override
    public List<Game> listByCategoryId(Long categoryId) {
        return list(new LambdaQueryWrapper<Game>()
                .eq(Game::getCategoryId, categoryId)
                .orderByDesc(Game::getCreatedTime));
    }

    @Override
    public List<Game> listActive() {
        return list(new LambdaQueryWrapper<Game>()
                .eq(Game::getStatus, 1)
                .orderByDesc(Game::getCreatedTime));
    }

    @Override
    public Game getWithCategoryById(Long id) {
        return baseMapper.selectGameWithCategoryById(id);
    }

    @Override
    public List<Game> searchByName(String name) {
        return list(new LambdaQueryWrapper<Game>()
                .like(Game::getName, name)
                .orderByDesc(Game::getCreatedTime));
    }

    @Override
    public boolean toggleStatus(Long id) {
        Game game = getById(id);
        if (game == null) {
            throw new BusinessException("游戏不存在");
        }
        int newStatus = game.getStatus() == 1 ? 0 : 1;
        return update(new LambdaUpdateWrapper<Game>()
                .eq(Game::getId, id)
                .set(Game::getStatus, newStatus));
    }

    @Override
    public boolean existsByName(String name) {
        return count(new LambdaQueryWrapper<Game>()
                .eq(Game::getName, name)) > 0;
    }

    @Override
    public boolean existsById(Long id) {
        return count(new LambdaQueryWrapper<Game>()
                .eq(Game::getId, id)) > 0;
    }
}