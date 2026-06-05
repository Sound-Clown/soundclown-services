CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(50)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('ADMIN','ARTIST','LISTENER') NOT NULL,
    is_active     BIT(1)       NOT NULL,
    created_at    DATETIME(6)  DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE password_reset_tokens (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    token      VARCHAR(255) NOT NULL,
    expires_at DATETIME(6)  NOT NULL,
    created_at DATETIME(6)  DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_prt_user_id (user_id),
    UNIQUE KEY uk_prt_token (token)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
