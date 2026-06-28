CREATE TABLE tag_search_stat_daily (
    tag_search_stat_daily_id BIGINT NOT NULL AUTO_INCREMENT,
    tag_id BIGINT NOT NULL,
    stat_date DATE NOT NULL,
    search_count BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (tag_search_stat_daily_id),
    CONSTRAINT uk_tag_search_stat_daily_tag_id_stat_date UNIQUE (tag_id, stat_date)
);

CREATE INDEX idx_tag_search_stat_daily_stat_date
    ON tag_search_stat_daily (stat_date);

CREATE INDEX idx_tag_search_stat_daily_tag_id_stat_date
    ON tag_search_stat_daily (tag_id, stat_date);

CREATE TABLE post_engagement_stat_daily (
    post_engagement_stat_daily_id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    stat_date DATE NOT NULL,
    view_count BIGINT NOT NULL,
    collection_added_count BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (post_engagement_stat_daily_id),
    CONSTRAINT uk_post_engagement_stat_daily_post_id_stat_date UNIQUE (post_id, stat_date)
);

CREATE INDEX idx_post_engagement_stat_daily_stat_date
    ON post_engagement_stat_daily (stat_date);

CREATE INDEX idx_post_engagement_stat_daily_post_id_stat_date
    ON post_engagement_stat_daily (post_id, stat_date);

ALTER TABLE tag_search_stat_daily
    ADD CONSTRAINT fk_tag_search_stat_daily_tag_id
        FOREIGN KEY (tag_id)
        REFERENCES tag (tag_id);

ALTER TABLE post_engagement_stat_daily
    ADD CONSTRAINT fk_post_engagement_stat_daily_post_id
        FOREIGN KEY (post_id)
        REFERENCES post (post_id);
