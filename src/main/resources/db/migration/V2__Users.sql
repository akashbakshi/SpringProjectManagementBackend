ALTER TABLE users ADD COLUMN password_reset_token varchar(512);
ALTER TABLE users ADD COLUMN password_reset_date timestamp;