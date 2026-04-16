-- V2__create_refresh_token_table.sql
CREATE TABLE tbl_refresh_tokens (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  token_id BINARY(16) NOT NULL UNIQUE,
  token VARCHAR(255) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  expiry_date TIMESTAMP(6) NOT NULL,
  revoked BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES tbl_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_rt_user_id ON tbl_refresh_tokens(user_id);