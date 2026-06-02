CREATE TABLE member (
    member_id BIGINT NOT NULL AUTO_INCREMENT,
    nickname VARCHAR(50) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_key VARCHAR(255) NOT NULL,
    profile_image VARCHAR(500) NOT NULL,
    preferred_team_id BIGINT NULL,
    role VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (member_id),
    CONSTRAINT uk_member_provider_provider_key UNIQUE (provider, provider_key)
);

CREATE INDEX idx_member_preferred_team_id
    ON member (preferred_team_id);

CREATE TABLE team (
    team_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (team_id)
);

CREATE TABLE post (
    post_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    image_url VARCHAR(2048) NOT NULL,
    image_width INT NULL,
    image_height INT NULL,
    image_aspect_ratio DOUBLE NULL,
    thumbnail_url VARCHAR(2048) NOT NULL,
    thumbnail_width INT NULL,
    thumbnail_height INT NULL,
    thumbnail_aspect_ratio DOUBLE NULL,
    thumbnail_status VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    team_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (post_id)
);

CREATE INDEX idx_post_member_id
    ON post (member_id);

CREATE INDEX idx_post_team_id
    ON post (team_id);

CREATE TABLE collection (
    collection_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    thumbnail_url VARCHAR(2048) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (collection_id)
);

CREATE INDEX idx_collection_member_id
    ON collection (member_id);

CREATE TABLE collection_post (
    collection_post_id BIGINT NOT NULL AUTO_INCREMENT,
    collection_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (collection_post_id),
    CONSTRAINT uk_collection_post_collection_id_post_id UNIQUE (collection_id, post_id)
);

CREATE INDEX idx_collection_post_collection_id
    ON collection_post (collection_id);

CREATE INDEX idx_collection_post_post_id
    ON collection_post (post_id);

CREATE TABLE follow (
    follow_id BIGINT NOT NULL AUTO_INCREMENT,
    follower_id BIGINT NOT NULL,
    followee_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (follow_id),
    CONSTRAINT uk_follow_follower_id_followee_id UNIQUE (follower_id, followee_id)
);

CREATE INDEX idx_follow_follower_id
    ON follow (follower_id);

CREATE INDEX idx_follow_followee_id
    ON follow (followee_id);

CREATE TABLE tag (
    tag_id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (tag_id),
    CONSTRAINT uk_tag_title UNIQUE (title)
);

CREATE TABLE post_tag (
    post_tag_id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (post_tag_id),
    CONSTRAINT uk_post_tag_post_id_tag_id UNIQUE (post_id, tag_id)
);

CREATE INDEX idx_post_tag_post_id
    ON post_tag (post_id);

CREATE INDEX idx_post_tag_tag_id
    ON post_tag (tag_id);

CREATE TABLE event_outbox (
    id BIGINT NOT NULL AUTO_INCREMENT,
    topic VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    event_id VARCHAR(64) NOT NULL,
    payload JSON NOT NULL,
    created_at DATETIME(6) NOT NULL,
    published_at DATETIME(6) NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_outbox_published_at_created_at
    ON event_outbox (published_at, created_at);

CREATE TABLE notification_device (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NULL,
    platform VARCHAR(20) NOT NULL,
    token VARCHAR(511) NOT NULL,
    enabled BIT NOT NULL,
    last_seen_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_notification_device_token UNIQUE (token)
);

CREATE INDEX idx_notification_device_user_id
    ON notification_device (user_id);

ALTER TABLE member
    ADD CONSTRAINT fk_member_preferred_team_id
        FOREIGN KEY (preferred_team_id)
        REFERENCES team (team_id)
        ON DELETE SET NULL;

ALTER TABLE post
    ADD CONSTRAINT fk_post_member_id
        FOREIGN KEY (member_id)
        REFERENCES member (member_id);

ALTER TABLE post
    ADD CONSTRAINT fk_post_team_id
        FOREIGN KEY (team_id)
        REFERENCES team (team_id)
        ON DELETE SET NULL;

ALTER TABLE collection
    ADD CONSTRAINT fk_collection_member_id
        FOREIGN KEY (member_id)
        REFERENCES member (member_id);

ALTER TABLE collection_post
    ADD CONSTRAINT fk_collection_post_collection_id
        FOREIGN KEY (collection_id)
        REFERENCES collection (collection_id);

ALTER TABLE collection_post
    ADD CONSTRAINT fk_collection_post_post_id
        FOREIGN KEY (post_id)
        REFERENCES post (post_id);

ALTER TABLE follow
    ADD CONSTRAINT fk_follow_follower_id
        FOREIGN KEY (follower_id)
        REFERENCES member (member_id);

ALTER TABLE follow
    ADD CONSTRAINT fk_follow_followee_id
        FOREIGN KEY (followee_id)
        REFERENCES member (member_id);

ALTER TABLE post_tag
    ADD CONSTRAINT fk_post_tag_post_id
        FOREIGN KEY (post_id)
        REFERENCES post (post_id);

ALTER TABLE post_tag
    ADD CONSTRAINT fk_post_tag_tag_id
        FOREIGN KEY (tag_id)
        REFERENCES tag (tag_id);

ALTER TABLE notification_device
    ADD CONSTRAINT fk_notification_device_user_id
        FOREIGN KEY (user_id)
        REFERENCES member (member_id)
        ON DELETE SET NULL;
