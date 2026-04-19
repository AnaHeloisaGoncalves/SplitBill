CREATE TABLE tbl_groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_id BINARY(16) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_group_created_by FOREIGN KEY (created_by) REFERENCES tbl_users(id)
);

CREATE TABLE tbl_group_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    joined_at DATETIME NOT NULL,
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES tbl_users(id),
    CONSTRAINT fk_member_group FOREIGN KEY (group_id) REFERENCES tbl_groups(id),
    CONSTRAINT uq_user_group UNIQUE (user_id, group_id)
);
