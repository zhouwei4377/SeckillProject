CREATE TABLE IF NOT EXISTS outbox_event (
    id BIGINT NOT NULL AUTO_INCREMENT,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    payload JSON NOT NULL,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=PENDING,1=SENT,2=FAILED',
    retries INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_error VARCHAR(512) NULL,
    PRIMARY KEY (id),
    KEY idx_outbox_pending (status, created_at),
    KEY idx_outbox_aggregate (aggregate_type, aggregate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE t_seckill_order
    ADD COLUMN request_id VARCHAR(128) NULL AFTER product_id;

ALTER TABLE t_seckill_order
    ADD UNIQUE INDEX uk_t_seckill_order_request (user_id, product_id, request_id);

ALTER TABLE t_seckill_order
    ADD UNIQUE INDEX uk_t_seckill_order_no (order_no);

ALTER TABLE t_seckill_order
    ADD INDEX idx_t_seckill_order_status_create_time (status, create_time);
