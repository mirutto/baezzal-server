ALTER TABLE member
    ADD COLUMN username VARCHAR(36) NULL AFTER nickname,
    ADD COLUMN description TEXT NULL AFTER profile_image;

UPDATE member
SET username = UUID()
WHERE username IS NULL;

UPDATE member
SET description = ''
WHERE description IS NULL;

ALTER TABLE member
    MODIFY COLUMN description TEXT NOT NULL,
    MODIFY COLUMN username VARCHAR(36) NOT NULL,
    ADD CONSTRAINT uk_member_username UNIQUE (username);
