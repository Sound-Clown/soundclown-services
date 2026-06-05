CREATE TABLE albums (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    name            VARCHAR(255) NOT NULL,
    cover_image     VARCHAR(500) DEFAULT NULL,
    artist_id       BIGINT       NOT NULL,
    artist_username VARCHAR(50)  DEFAULT NULL,
    created_at      DATETIME(6)  DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_album_artist_id (artist_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE songs (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    title           VARCHAR(255) NOT NULL,
    audio_file      VARCHAR(500) NOT NULL,
    cover_image     VARCHAR(500) DEFAULT NULL,
    artist_id       BIGINT       NOT NULL,
    artist_username VARCHAR(50)  NOT NULL,
    album_id        BIGINT       DEFAULT NULL,
    status          ENUM('APPROVED','PENDING','REJECTED') NOT NULL,
    reject_reason   VARCHAR(500) DEFAULT NULL,
    play_count      INT          NOT NULL,
    like_count      INT          NOT NULL,
    created_at      DATETIME(6)  DEFAULT NULL,
    updated_at      DATETIME(6)  DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_status (status),
    KEY idx_artist_id (artist_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE likes (
    user_id    BIGINT      NOT NULL,
    song_id    BIGINT      NOT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (song_id, user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
