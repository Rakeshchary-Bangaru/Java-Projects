CREATE TABLE users (

    id BIGINT NOT NULL AUTO_INCREMENT,

    email VARCHAR(255) NOT NULL,

    password_hash VARCHAR(255) NULL,

    role VARCHAR(30) NOT NULL,

    status VARCHAR(30) NOT NULL,

    auth_provider VARCHAR(30) NOT NULL,

    provider_user_id VARCHAR(255) NULL,

    last_login_at DATETIME(6) NULL,

    last_activity_at DATETIME(6) NULL,

    created_at DATETIME(6) NOT NULL,

    updated_at DATETIME(6) NOT NULL,

    CONSTRAINT pk_users
        PRIMARY KEY (id),

    CONSTRAINT uk_users_email
        UNIQUE (email),

    CONSTRAINT uk_users_provider_identity
        UNIQUE (auth_provider, provider_user_id)
);
