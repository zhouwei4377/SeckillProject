-- =========================================
-- 秒杀系统数据库初始化脚本
-- 数据库名称: seckill_db
-- 字符集: UTF-8MB4
-- =========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `seckill_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `seckill_db`;

-- =========================================
-- 表1: t_seckill_product - 秒杀商品表
-- 描述: 存储秒杀活动中的商品信息、库存、价格等
-- =========================================
CREATE TABLE IF NOT EXISTS `t_seckill_product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `name` VARCHAR(255) NOT NULL COMMENT '商品名称',
  `description` TEXT COMMENT '商品描述',
  `original_price` DECIMAL(10, 2) NOT NULL COMMENT '原价',
  `seckill_price` DECIMAL(10, 2) NOT NULL COMMENT '秒杀价',
  `initial_stock` INT NOT NULL COMMENT '初始库存',
  `current_stock` INT NOT NULL COMMENT '当前库存',
  `sold_quantity` INT NOT NULL DEFAULT 0 COMMENT '已售数量',
  `start_time` DATETIME NOT NULL COMMENT '秒杀开始时间',
  `end_time` DATETIME NOT NULL COMMENT '秒杀结束时间',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '秒杀状态: 1-未开始, 2-进行中, 3-已结束, 4-已售罄',
  `image_url` VARCHAR(500) COMMENT '商品图片URL',
  `stock_version` INT NOT NULL DEFAULT 0 COMMENT '库存版本号(用于CAS乐观锁)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_end_time` (`end_time`),
  KEY `idx_create_time` (`create_time`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀商品表';

-- =========================================
-- 表2: t_stock_change_log - 库存变更日志表
-- 描述: 记录每次库存的变化情况，用于审计和数据追踪
-- =========================================
CREATE TABLE IF NOT EXISTS `t_stock_change_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `change_type` TINYINT NOT NULL COMMENT '变更类型: 1-扣减, 2-回退, 3-初始化',
  `change_quantity` INT NOT NULL COMMENT '变更数量',
  `stock_before` INT NOT NULL COMMENT '变更前库存',
  `stock_after` INT NOT NULL COMMENT '变更后库存',
  `order_id` BIGINT COMMENT '相关订单ID',
  `operator` VARCHAR(100) COMMENT '操作人',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_product_id` FOREIGN KEY (`product_id`) REFERENCES `t_seckill_product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存变更日志表';

-- =========================================
-- 表3: t_seckill_order - 秒杀订单表
-- 描述: 存储用户参加秒杀产生的订单信息
-- =========================================
CREATE TABLE IF NOT EXISTS `t_seckill_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(100) NOT NULL COMMENT '订单号(业务订单号)',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '订单数量',
  `seckill_price` DECIMAL(10, 2) NOT NULL COMMENT '秒杀价格',
  `total_price` DECIMAL(10, 2) NOT NULL COMMENT '订单总价',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态: 1-待支付, 2-已支付, 3-已发货, 4-已收货, 5-已取消, 6-已退款',
  `payment_method` TINYINT COMMENT '支付方式: 1-在线支付, 2-余额支付',
  `phone` VARCHAR(20) COMMENT '联系电话',
  `receiver_name` VARCHAR(100) COMMENT '收货人名称',
  `delivery_address` VARCHAR(500) COMMENT '收货地址',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单更新时间',
  `payment_time` DATETIME COMMENT '支付时间',
  `cancel_time` DATETIME COMMENT '取消时间',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号(乐观锁)',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_payment_time` (`payment_time`),
  KEY `idx_user_product` (`user_id`, `product_id`),
  CONSTRAINT `fk_order_product_id` FOREIGN KEY (`product_id`) REFERENCES `t_seckill_product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='秒杀订单表';

-- =========================================
-- 表4: t_payment_record - 支付记录表
-- 描述: 存储订单的支付信息和支付状态
-- =========================================
CREATE TABLE IF NOT EXISTS `t_payment_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
  `payment_no` VARCHAR(100) NOT NULL COMMENT '支付单号',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(100) NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `amount` DECIMAL(10, 2) NOT NULL COMMENT '支付金额',
  `payment_method` TINYINT NOT NULL COMMENT '支付方式: 1-在线支付, 2-余额支付, 3-积分支付',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '支付状态: 1-待支付, 2-支付成功, 3-支付失败, 4-已退款',
  `payment_channel` TINYINT COMMENT '支付渠道: 1-支付宝, 2-微信, 3-银联',
  `third_party_payment_no` VARCHAR(100) COMMENT '第三方支付单号',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `payment_time` DATETIME COMMENT '支付完成时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` INT NOT NULL DEFAULT 0 COMMENT '版本号(乐观锁)',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否, 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_payment_time` (`payment_time`),
  CONSTRAINT `fk_payment_order_id` FOREIGN KEY (`order_id`) REFERENCES `t_seckill_order` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- =========================================
-- 初始化测试数据
-- =========================================

-- 插入测试秒杀商品
INSERT INTO `t_seckill_product` (
  `name`, `description`, `original_price`, `seckill_price`, 
  `initial_stock`, `current_stock`, `start_time`, `end_time`, `status`, `image_url`
) VALUES 
(
  'iPhone 14 Pro Max',
  '苹果iPhone 14 Pro Max，256GB深空黑',
  9999.00,
  5999.00,
  100,
  100,
  DATE_ADD(NOW(), INTERVAL -1 HOUR),
  DATE_ADD(NOW(), INTERVAL 2 HOUR),
  2,
  'https://example.com/iphone14-pro-max.jpg'
),
(
  'iPad Air 5',
  'iPad Air 5代，256GB WiFi版本',
  6999.00,
  3999.00,
  50,
  50,
  DATE_ADD(NOW(), INTERVAL 1 HOUR),
  DATE_ADD(NOW(), INTERVAL 3 HOUR),
  1,
  'https://example.com/ipad-air-5.jpg'
),
(
  'MacBook Pro 14',
  'MacBook Pro 14英寸，M1 Pro芯片，512GB',
  12999.00,
  9999.00,
  30,
  30,
  DATE_ADD(NOW(), INTERVAL -2 HOUR),
  DATE_ADD(NOW(), INTERVAL 1 HOUR),
  2,
  'https://example.com/macbook-pro-14.jpg'
);

-- =========================================
-- 创建视图：秒杀活动统计视图
-- =========================================
CREATE OR REPLACE VIEW `v_seckill_statistics` AS
SELECT 
  p.id,
  p.name,
  p.original_price,
  p.seckill_price,
  p.initial_stock,
  p.current_stock,
  p.sold_quantity,
  ROUND((p.sold_quantity / p.initial_stock) * 100, 2) AS sold_percentage,
  p.start_time,
  p.end_time,
  p.status,
  COUNT(DISTINCT o.id) AS total_orders,
  COUNT(DISTINCT o.user_id) AS total_users,
  COALESCE(SUM(o.total_price), 0) AS total_amount
FROM 
  t_seckill_product p
LEFT JOIN 
  t_seckill_order o ON p.id = o.product_id AND o.deleted = 0
WHERE 
  p.deleted = 0
GROUP BY 
  p.id, p.name, p.original_price, p.seckill_price, 
  p.initial_stock, p.current_stock, p.sold_quantity,
  p.start_time, p.end_time, p.status;

-- =========================================
-- 创建存储过程：批量插入库存变更日志
-- =========================================
DELIMITER $$

CREATE PROCEDURE `sp_insert_stock_change_log`(
  IN p_product_id BIGINT,
  IN p_change_type TINYINT,
  IN p_change_quantity INT,
  IN p_stock_before INT,
  IN p_stock_after INT,
  IN p_order_id BIGINT,
  IN p_operator VARCHAR(100),
  IN p_remark VARCHAR(500)
)
BEGIN
  INSERT INTO `t_stock_change_log` (
    `product_id`, `change_type`, `change_quantity`,
    `stock_before`, `stock_after`, `order_id`,
    `operator`, `remark`
  ) VALUES (
    p_product_id, p_change_type, p_change_quantity,
    p_stock_before, p_stock_after, p_order_id,
    p_operator, p_remark
  );
END$$

DELIMITER ;

-- =========================================
-- 创建事件：订单自动超时取消事件（每小时执行）
-- =========================================
CREATE EVENT IF NOT EXISTS `evt_cancel_expired_orders`
ON SCHEDULE EVERY 1 HOUR
STARTS CURRENT_TIMESTAMP
DO
BEGIN
  -- 取消超过15分钟未支付的订单
  UPDATE `t_seckill_order`
  SET `status` = 5, `cancel_time` = NOW(), `update_time` = NOW()
  WHERE 
    `status` = 1 
    AND `create_time` < DATE_SUB(NOW(), INTERVAL 15 MINUTE)
    AND `deleted` = 0;
END;

-- =========================================
-- 创建事件：支付自动超时取消事件（每小时执行）
-- =========================================
CREATE EVENT IF NOT EXISTS `evt_cancel_expired_payments`
ON SCHEDULE EVERY 1 HOUR
STARTS CURRENT_TIMESTAMP
DO
BEGIN
  -- 取消超过30分钟未支付的支付单
  UPDATE `t_payment_record`
  SET `status` = 3, `update_time` = NOW()
  WHERE 
    `status` = 1 
    AND `create_time` < DATE_SUB(NOW(), INTERVAL 30 MINUTE)
    AND `deleted` = 0;
END;

-- =========================================
-- 创建索引以优化查询性能
-- =========================================

-- 订单表优化索引
ALTER TABLE `t_seckill_order` ADD INDEX `idx_status_create_time` (`status`, `create_time`);
ALTER TABLE `t_seckill_order` ADD INDEX `idx_user_status` (`user_id`, `status`);

-- 支付记录表优化索引
ALTER TABLE `t_payment_record` ADD INDEX `idx_status_create_time` (`status`, `create_time`);
ALTER TABLE `t_payment_record` ADD INDEX `idx_user_status` (`user_id`, `status`);

-- 库存变更日志表优化索引
ALTER TABLE `t_stock_change_log` ADD INDEX `idx_product_create_time` (`product_id`, `create_time`);

-- =========================================
-- 数据库字符集和排序规则验证
-- =========================================
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- =========================================
-- 初始化完成
-- =========================================
COMMIT;
