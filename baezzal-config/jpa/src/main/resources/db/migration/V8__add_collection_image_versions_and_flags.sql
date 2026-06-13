ALTER TABLE collection
    ADD COLUMN description TEXT NOT NULL AFTER name,
    ADD COLUMN raw_url VARCHAR(2048) NOT NULL DEFAULT '' AFTER description,
    ADD COLUMN public_url VARCHAR(2048) NOT NULL DEFAULT '' AFTER raw_url,
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' AFTER thumbnail_url,
    ADD COLUMN aspect_ratio DOUBLE NOT NULL DEFAULT 1.0 AFTER status,
    ADD COLUMN is_custom_thumbnail BIT NOT NULL DEFAULT b'0' AFTER aspect_ratio,
    ADD COLUMN is_published BIT NOT NULL DEFAULT b'0' AFTER is_custom_thumbnail;

UPDATE collection
SET description = '',
    raw_url = thumbnail_url,
    public_url = thumbnail_url,
    status = 'SUCCESS',
    aspect_ratio = 1.0;
