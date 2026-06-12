ALTER TABLE post
    ADD COLUMN public_url VARCHAR(2048) NOT NULL DEFAULT '' AFTER image_url;

UPDATE post
SET public_url = image_url
WHERE public_url = '';

UPDATE post
SET thumbnail_status = 'PROCESSING'
WHERE thumbnail_status = 'PENDING';
