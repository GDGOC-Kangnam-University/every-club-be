-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    nickname VARCHAR(255),
    profile_image_url VARCHAR(255),
    department VARCHAR(255),
    student_id VARCHAR(255),
    phone_number VARCHAR(255),
    bio VARCHAR(500),
    role VARCHAR(50) NOT NULL DEFAULT 'GUEST',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Category table
CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

-- Club table
CREATE TABLE club (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(20) NOT NULL,
    summary VARCHAR(200) NOT NULL,
    description TEXT,
    logo_url VARCHAR(2048),
    banner_url VARCHAR(2048),
    join_form_url VARCHAR(2048),
    recruiting_status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    department VARCHAR(50),
    activity_cycle VARCHAR(50),
    has_fee BOOLEAN NOT NULL DEFAULT FALSE,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    tags TEXT,
    category_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_club_category FOREIGN KEY (category_id) REFERENCES category(id),
    CONSTRAINT fk_club_author FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Email verification table
CREATE TABLE email_verification (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    verification_code VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_email_verification_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Club likes join table
CREATE TABLE club_likes (
    user_id BIGINT NOT NULL,
    club_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, club_id),
    CONSTRAINT fk_club_likes_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_club_likes_club FOREIGN KEY (club_id) REFERENCES club(id)
);

-- Index on club tags (basic B-tree, V2 will add GIN index for trigram search)
CREATE INDEX idx_club_tags ON club(tags);