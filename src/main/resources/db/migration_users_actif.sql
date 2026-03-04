-- Add actif flag to users (admin can disable login). Run on your DB; ignore if column exists.
ALTER TABLE users ADD COLUMN actif TINYINT(1) DEFAULT 1;
