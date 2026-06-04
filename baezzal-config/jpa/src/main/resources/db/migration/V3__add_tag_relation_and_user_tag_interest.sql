CREATE TABLE tag_relation (
    tag_relation_id BIGINT NOT NULL AUTO_INCREMENT,
    source_tag_id BIGINT NOT NULL,
    target_tag_id BIGINT NOT NULL,
    relation_type VARCHAR(30) NOT NULL,
    score BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (tag_relation_id),
    CONSTRAINT uk_tag_relation_source_target_type UNIQUE (source_tag_id, target_tag_id, relation_type)
);

CREATE INDEX idx_tag_relation_source_tag_id
    ON tag_relation (source_tag_id);

CREATE INDEX idx_tag_relation_target_tag_id
    ON tag_relation (target_tag_id);

CREATE TABLE user_tag_interest (
    user_tag_interest_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    score INT NOT NULL,
    last_interacted_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (user_tag_interest_id),
    CONSTRAINT uk_user_tag_interest_user_id_tag_id UNIQUE (user_id, tag_id)
);

CREATE INDEX idx_user_tag_interest_user_id
    ON user_tag_interest (user_id);

CREATE INDEX idx_user_tag_interest_tag_id
    ON user_tag_interest (tag_id);

ALTER TABLE tag_relation
    ADD CONSTRAINT fk_tag_relation_source_tag_id
        FOREIGN KEY (source_tag_id)
        REFERENCES tag (tag_id);

ALTER TABLE tag_relation
    ADD CONSTRAINT fk_tag_relation_target_tag_id
        FOREIGN KEY (target_tag_id)
        REFERENCES tag (tag_id);

ALTER TABLE user_tag_interest
    ADD CONSTRAINT fk_user_tag_interest_user_id
        FOREIGN KEY (user_id)
        REFERENCES member (member_id);

ALTER TABLE user_tag_interest
    ADD CONSTRAINT fk_user_tag_interest_tag_id
        FOREIGN KEY (tag_id)
        REFERENCES tag (tag_id);
