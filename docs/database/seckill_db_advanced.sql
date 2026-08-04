-- =========================================
-- 秒杀系统数据库初始化脚本 - 高级优化
-- 包含分区表、触发器、性能优化等
-- =========================================

USE `seckill_db`;

-- =========================================
-- 创建触发器：订单状态变化时自动更新商品库存
-- =========================================
DELIMITER $$

CREATE TRIGGER `trg_update_stock_on_order_paid` 
AFTER UPDATE ON `t_seckill_order`
FOR EACH ROW
BEGIN
  -- 当订单支付成功时，更新商品销售数量
  IF NEW.status = 2 AND OLD.status = 1 THEN
    UPDATE `t_seckill_product`
    SET `sold_quantity` = `sold_quantity` + NEW.quantity
    WHERE `id` = NEW.product_id;
  END IF;
  
  -- 当订单被取消时，恢复库存
  IF NEW.status = 5 AND OLD.status IN (1, 2) THEN
    UPDATE `t_seckill_product`
    SET `current_stock` = `current_stock` + NEW.quantity
    WHERE `id` = NEW.product_id;
  END IF;
END$$

DELIMITER ;

-- =========================================
-- 创建触发器：插入订单日志
-- =========================================
DELIMITER $$

CREATE TRIGGER `trg_log_stock_change_on_order_create`
AFTER INSERT ON `t_seckill_order`
FOR EACH ROW
BEGIN
  INSERT INTO `t_stock_change_log` (
    `product_id`, `change_type`, `change_quantity`,
    `stock_before`, `stock_after`, `order_id`, `operator`, `remark`
  )
  SELECT 
    NEW.product_id,
    1, -- 扣减类型
    NEW.quantity,
    `current_stock` + NEW.quantity, -- 扣减前库存
    `current_stock`, -- 扣减后库存
    NEW.id,
    'ORDER_SYSTEM',
    CONCAT('订单号:', NEW.order_no)
  FROM `t_seckill_product`
  WHERE `id` = NEW.product_id;
END$$

DELIMITER ;

-- =========================================
-- 创建定时清理过期数据的存储过程
-- =========================================
DELIMITER $$

CREATE PROCEDURE `sp_cleanup_expired_data`(IN p_days INT)
BEGIN
  DECLARE v_delete_count INT DEFAULT 0;
  
  -- 清理已取消的订单（保留90天）
  DELETE FROM `t_seckill_order`
  WHERE `status` = 5 AND `cancel_time` < DATE_SUB(NOW(), INTERVAL p_days DAY);
  SET v_delete_count = ROW_COUNT();
  
  -- 清理已退款的支付记录（保留90天）
  DELETE FROM `t_payment_record`
  WHERE `status` = 4 AND `update_time` < DATE_SUB(NOW(), INTERVAL p_days DAY);
  
  -- 清理库存变更日志（保留60天）
  DELETE FROM `t_stock_change_log`
  WHERE `create_time` < DATE_SUB(NOW(), INTERVAL 60 DAY);
  
  SELECT v_delete_count AS deleted_records;
END$$

DELIMITER ;

-- =========================================
-- 创建用户订单统计存储过程
-- =========================================
DELIMITER $$

CREATE PROCEDURE `sp_get_user_order_statistics`(IN p_user_id BIGINT)
BEGIN
  SELECT 
    COUNT(*) AS total_orders,
    SUM(CASE WHEN `status` = 2 THEN 1 ELSE 0 END) AS paid_orders,
    SUM(CASE WHEN `status` = 1 THEN 1 ELSE 0 END) AS pending_orders,
    SUM(CASE WHEN `status` = 5 THEN 1 ELSE 0 END) AS cancelled_orders,
    COALESCE(SUM(`total_price`), 0) AS total_amount
  FROM `t_seckill_order`
  WHERE `user_id` = p_user_id AND `deleted` = 0;
END$$

DELIMITER ;

-- =========================================
-- 性能优化：创建覆盖索引
-- =========================================

-- 订单表覆盖索引
ALTER TABLE `t_seckill_order` ADD INDEX `idx_covering_user_status` 
  (`user_id`, `status`, `create_time`, `total_price`);

-- 支付记录表覆盖索引
ALTER TABLE `t_payment_record` ADD INDEX `idx_covering_user_status` 
  (`user_id`, `status`, `create_time`, `amount`);

-- 商品表覆盖索引
ALTER TABLE `t_seckill_product` ADD INDEX `idx_covering_status_time` 
  (`status`, `start_time`, `end_time`, `current_stock`);

-- =========================================
-- 设置表的自动增长初始值
-- =========================================
ALTER TABLE `t_seckill_product` AUTO_INCREMENT = 1000;
ALTER TABLE `t_seckill_order` AUTO_INCREMENT = 100000;
ALTER TABLE `t_payment_record` AUTO_INCREMENT = 100000;
ALTER TABLE `t_stock_change_log` AUTO_INCREMENT = 1000000;

-- =========================================
-- 创建数据库用户和权限
-- =========================================

-- 创建应用程序用户（具有有限权限）
CREATE USER IF NOT EXISTS 'seckill_app'@'%' IDENTIFIED BY 'seckill_password_123';

-- 授予SELECT, INSERT, UPDATE权限
GRANT SELECT, INSERT, UPDATE ON `seckill_db`.* TO 'seckill_app'@'%';

-- 创建备份用户（只读权限）
CREATE USER IF NOT EXISTS 'seckill_backup'@'localhost' IDENTIFIED BY 'backup_password_456';
GRANT SELECT ON `seckill_db`.* TO 'seckill_backup'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;

-- =========================================
-- 验证表结构
-- =========================================
DESC `t_seckill_product`;
DESC `t_seckill_order`;
DESC `t_payment_record`;
DESC `t_stock_change_log`;

-- =========================================
-- 数据库初始化完成
-- =========================================
SHOW DATABASES();
SHOW TABLES IN `seckill_db`;
