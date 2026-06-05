CREATE TABLE user_profiles (
    user_id    BIGINT       NOT NULL,
    username   VARCHAR(50)  NOT NULL,
    email      VARCHAR(255) NOT NULL,
    role       ENUM('ADMIN','ARTIST','LISTENER') NOT NULL,
    is_active  BIT(1)       NOT NULL,
    created_at DATETIME(6)  DEFAULT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_user_profiles_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
