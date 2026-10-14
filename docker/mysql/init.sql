-- init schema for SeckillProject (simplified for local testing)

CREATE TABLE IF NOT EXISTS seckill_product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255),
  description TEXT,
  original_price DECIMAL(10,2),
  seckill_price DECIMAL(10,2),
  initial_stock INT DEFAULT 0,
  current_stock INT DEFAULT 0,
  stock_version INT DEFAULT 0,
  sold_quantity INT DEFAULT 0,
  status INT DEFAULT 1,
  start_time DATETIME,
  end_time DATETIME,
  image_url VARCHAR(512),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS seckill_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(64),
  user_id BIGINT,
  product_id BIGINT,
  quantity INT,
  seckill_price DECIMAL(10,2),
  total_price DECIMAL(12,2),
  status INT,
  payment_method INT,
  phone VARCHAR(32),
  receiver_name VARCHAR(128),
  delivery_address VARCHAR(512),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  payment_time DATETIME,
  cancel_time DATETIME,
  version INT DEFAULT 0,
  deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS stock_change_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT,
  change_type INT,
  change_quantity INT,
  stock_before INT,
  stock_after INT,
  order_id BIGINT,
  operator VARCHAR(64),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS compensation_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT,
  product_id BIGINT,
  quantity INT,
  type INT,
  status INT,
  attempts INT DEFAULT 0,
  last_attempt_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
