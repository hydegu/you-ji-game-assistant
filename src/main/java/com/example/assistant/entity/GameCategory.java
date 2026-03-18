package com.example.assistant.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 游戏分类实体
 * @TableName game_category
 */
@TableName(value = "game_category")
@Data
public class GameCategory implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 排序权重（数值越大越靠前）
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 删除时间（逻辑删除）
     */
    @TableLogic
    private LocalDateTime deletedTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
