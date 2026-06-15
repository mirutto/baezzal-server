ALTER TABLE member
    MODIFY COLUMN profile_image VARCHAR(2048) NOT NULL,
    ADD COLUMN public_profile_image VARCHAR(2048) NOT NULL DEFAULT '' AFTER profile_image,
    ADD COLUMN thumbnail_profile_image VARCHAR(2048) NOT NULL DEFAULT '' AFTER public_profile_image,
    ADD COLUMN profile_image_status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' AFTER thumbnail_profile_image,
    ADD COLUMN profile_image_aspect_ratio DOUBLE NOT NULL DEFAULT 1.0 AFTER profile_image_status;

UPDATE member
SET profile_image = 'https://static.wowan.me/baezzal/members/raw/mirutto_default_profile.png',
    public_profile_image = 'https://static.wowan.me/baezzal/members/public/mirutto_default_profile.png',
    thumbnail_profile_image = 'https://static.wowan.me/baezzal/members/thumbnail/mirutto_default_profile.webp',
    profile_image_status = 'SUCCESS',
    profile_image_aspect_ratio = 1.0
WHERE profile_image = ''
   OR profile_image = 'https://static.wowan.me/baezzal/images/mirutto_default.png';

UPDATE member
SET public_profile_image = profile_image,
    thumbnail_profile_image = profile_image,
    profile_image_status = 'SUCCESS',
    profile_image_aspect_ratio = 1.0
WHERE public_profile_image = ''
  AND thumbnail_profile_image = '';
