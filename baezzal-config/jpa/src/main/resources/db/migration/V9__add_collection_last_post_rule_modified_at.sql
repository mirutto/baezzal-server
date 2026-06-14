ALTER TABLE collection
    ADD COLUMN last_post_rule_modified_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) AFTER is_published;
