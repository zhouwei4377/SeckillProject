CREATE TABLE IF NOT EXISTS order_event_dedup (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_type VARCHAR(128) NOT NULL,
    aggregate_id VARCHAR(128) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_event_dedup (event_type, aggregate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payment_callback_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    transaction_id VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    payload JSON NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_callback_order_txn (order_id, transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
