CREATE TABLE sellers (

    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    business_name VARCHAR(200) NOT NULL,

    approval_status VARCHAR(30)
        NOT NULL DEFAULT 'PENDING',

    approved_at TIMESTAMP(6) NULL,

    approved_by_user_id BIGINT NULL,

    created_at TIMESTAMP(6) NOT NULL,

    updated_at TIMESTAMP(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_sellers_user
        UNIQUE (user_id),

    CONSTRAINT fk_sellers_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),

    CONSTRAINT fk_sellers_approved_by
        FOREIGN KEY (approved_by_user_id)
        REFERENCES users(id)
);