CREATE TABLE payments (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    username      VARCHAR(50)  NOT NULL,
    amount        BIGINT       NOT NULL,
    currency      VARCHAR(10)  NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    provider      VARCHAR(20)  NOT NULL,
    txn_ref       VARCHAR(64)  NOT NULL,
    bank_txn_no   VARCHAR(64)  DEFAULT NULL,
    order_info    VARCHAR(255) DEFAULT NULL,
    duration_days INT          NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    paid_at       DATETIME(6)  DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payments_txn_ref (txn_ref),
    KEY idx_payments_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
