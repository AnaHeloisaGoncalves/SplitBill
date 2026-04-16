ALTER TABLE tbl_refresh_tokens
    ADD COLUMN ip_address VARCHAR(255),
    ADD COLUMN user_agent VARCHAR(255);
