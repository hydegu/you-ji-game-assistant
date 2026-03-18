package com.example.assistant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.assistant.dto.request.CategoryCreateRequest;
import com.example.assistant.dto.request.GameCreateRequest;
import com.example.assistant.dto.request.GameUpdateRequest;
import com.example.assistant.dto.response.ApiResponse;
import com.example.assistant.entity.Game;
import com.example.assistant.entity.GameCategory;
import com.example.assistant.exception.BusinessException;
import com.example.assistant.service.GameCategoryService;
import com.example.assistant.service.GameService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 游戏管理控制器
 */
@RestController
@RequestMapping("/api")
public class GameController {

    private final GameService gameService;
    private final GameCategoryService categoryService;

    public GameController(GameService gameService, GameCategoryService categoryService) {
        this.gameService = gameService;
        this.categoryService = categoryService;
    }

    // ==================== 游戏 API ====================

    /**
     * 游戏列表（分页）
     */
    @GetMapping("/games")
    public ApiResponse<Page<Game>> listGames(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        
        Page<Game> pageParam = new Page<>(page, size);
        
        if (keyword != null && !keyword.isEmpty()) {
            List<Game> games = gameService.searchByName(keyword);
            Page<Game> result = new Page<>(page, size, games.size());
            result.setRecords(games);
            return ApiResponse.ok(result);
        }
        
        if (categoryId != null) {
            List<Game> games = gameService.listByCategoryId(categoryId);
            Page<Game> result = new Page<>(page, size, games.size());
            result.setRecords(games);
            return ApiResponse.ok(result);
        }
        
        return ApiResponse.ok(gameService.page(pageParam));
    }

    /**
     * 游戏详情
     */
    @GetMapping("/games/{id}")
    public ApiResponse<Game> getGame(@PathVariable Long id) {
        Game game = gameService.getWithCategoryById(id);
        if (game == null) {
            throw new BusinessException("游戏不存在");
        }
        return ApiResponse.ok(game);
    }

    /**
     * 创建游戏
     */
    @PostMapping("/games")
    public ApiResponse<Game> createGame(@Valid @RequestBody GameCreateRequest request) {
        if (gameService.existsByName(request.getName())) {
            throw new BusinessException("游戏名称已存在");
        }
        
        // 检查分类是否存在
        if (!categoryService.existsById(request.getCategoryId())) {
            throw new BusinessException("分类不存在");
        }
        
        Game game = new Game();
        game.setName(request.getName());
        game.setDescription(request.getDescription());
        game.setImage(request.getImage());
        game.setCategoryId(request.getCategoryId());
        game.setStatus(request.getStatus());
        game.setDeveloper(request.getDeveloper());
        
        gameService.save(game);
        return ApiResponse.ok(game);
    }

    /**
     * 更新游戏
     */
    @PutMapping("/games/{id}")
    public ApiResponse<Game> updateGame(@PathVariable Long id, 
                                         @Valid @RequestBody GameUpdateRequest request) {
        Game game = gameService.getById(id);
        if (game == null) {
            throw new BusinessException("游戏不存在");
        }
        
        // 检查分类是否存在
        if (request.getCategoryId() != null && 
            !categoryService.existsById(request.getCategoryId())) {
            throw new BusinessException("分类不存在");
        }
        
        if (request.getName() != null) {
            game.setName(request.getName());
        }
        if (request.getDescription() != null) {
            game.setDescription(request.getDescription());
        }
        if (request.getImage() != null) {
            game.setImage(request.getImage());
        }
        if (request.getCategoryId() != null) {
            game.setCategoryId(request.getCategoryId());
        }
        if (request.getStatus() != null) {
            game.setStatus(request.getStatus());
        }
        if (request.getDeveloper() != null) {
            game.setDeveloper(request.getDeveloper());
        }
        
        gameService.updateById(game);
        return ApiResponse.ok(game);
    }

    /**
     * 删除游戏
     */
    @DeleteMapping("/games/{id}")
    public ApiResponse<Void> deleteGame(@PathVariable Long id) {
        if (!gameService.existsById(id)) {
            throw new BusinessException("游戏不存在");
        }
        gameService.removeById(id);
        return ApiResponse.ok();
    }

    /**
     * 切换游戏状态
     */
    @PostMapping("/games/{id}/toggle")
    public ApiResponse<Void> toggleGameStatus(@PathVariable Long id) {
        gameService.toggleStatus(id);
        return ApiResponse.ok();
    }

    // ==================== 分类 API ====================

    /**
     * 分类列表
     */
    @GetMapping("/categories")
    public ApiResponse<List<GameCategory>> listCategories() {
        return ApiResponse.ok(categoryService.listAllOrderBySort());
    }

    /**
     * 分类详情
     */
    @GetMapping("/categories/{id}")
    public ApiResponse<GameCategory> getCategory(@PathVariable Long id) {
        GameCategory category = categoryService.getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        return ApiResponse.ok(category);
    }

    /**
     * 创建分类
     */
    @PostMapping("/categories")
    public ApiResponse<GameCategory> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        if (categoryService.existsByName(request.getName())) {
            throw new BusinessException("分类名称已存在");
        }
        
        GameCategory category = new GameCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());
        
        categoryService.save(category);
        return ApiResponse.ok(category);
    }

    /**
     * 更新分类
     */
    @PutMapping("/categories/{id}")
    public ApiResponse<GameCategory> updateCategory(@PathVariable Long id, 
                                                     @Valid @RequestBody CategoryCreateRequest request) {
        GameCategory category = categoryService.getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());
        
        categoryService.updateById(category);
        return ApiResponse.ok(category);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        if (!categoryService.existsById(id)) {
            throw new BusinessException("分类不存在");
        }
        
        // 检查分类下是否还有游戏
        List<Game> games = gameService.listByCategoryId(id);
        if (!games.isEmpty()) {
            throw new BusinessException("该分类下还有游戏，无法删除");
        }
        
        categoryService.removeById(id);
        return ApiResponse.ok();
    }
}
