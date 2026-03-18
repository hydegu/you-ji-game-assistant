-- ===========================================
-- Game实体升级迁移脚本
-- 执行时间: 2026-03-18
-- ===========================================

-- 1. 创建游戏分类表
CREATE TABLE IF NOT EXISTS game_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description VARCHAR(500) COMMENT '分类描述',
    sort_order INT DEFAULT 0 COMMENT '排序权重（数值越大越靠前）',
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_time TIMESTAMP NULL COMMENT '删除时间（逻辑删除）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏分类表';

-- 2. 修改game表，添加新字段
ALTER TABLE game 
    ADD COLUMN category_id BIGINT COMMENT '分类ID' AFTER image,
    ADD COLUMN status INT DEFAULT 1 COMMENT '状态: 0=下架, 1=上架' AFTER category_id,
    ADD COLUMN developer VARCHAR(200) COMMENT '开发商' AFTER status;

-- 3. 添加索引
ALTER TABLE game ADD INDEX idx_category_id (category_id);
ALTER TABLE game ADD INDEX idx_status (status);

-- 4. 插入默认分类数据
INSERT INTO game_category (name, description, sort_order) VALUES
('动作游戏', '动作类游戏', 100),
('冒险游戏', '冒险类游戏', 90),
('角色扮演', 'RPG角色扮演类游戏', 80),
('策略游戏', '策略类游戏', 70),
('模拟经营', '模拟经营类游戏', 60),
('休闲游戏', '休闲类游戏', 50),
('其他', '其他类型游戏', 10);

-- 5. 将现有游戏数据关联到"其他"分类
UPDATE game SET category_id = (SELECT id FROM game_category WHERE name = '其他' LIMIT 1) 
WHERE category_id IS NULL;
