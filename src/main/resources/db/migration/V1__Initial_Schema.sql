-- Category Table
CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

-- Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(255),
    profile_image_url VARCHAR(255),
    department VARCHAR(255),
    student_id VARCHAR(255),
    phone_number VARCHAR(255),
    bio VARCHAR(500),
    role VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Club Table
CREATE TABLE club (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(20) NOT NULL,
    summary VARCHAR(200) NOT NULL,
    description TEXT,
    logo_url VARCHAR(2048),
    banner_url VARCHAR(2048),
    join_form_url VARCHAR(2048),
    recruiting_status VARCHAR(255) NOT NULL,
    department VARCHAR(50),
    activity_cycle VARCHAR(50),
    has_fee BOOLEAN NOT NULL DEFAULT FALSE,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    tags TEXT,
    category_id BIGINT NOT NULL REFERENCES category(id),
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

-- Join Table for Club Likes
CREATE TABLE club_likes (
    user_id BIGINT NOT NULL REFERENCES users(id),
    club_id BIGINT NOT NULL REFERENCES club(id),
    PRIMARY KEY (user_id, club_id),
    CONSTRAINT unique_user_club_like UNIQUE (user_id, club_id)
);

-- Create individual indexes as requested by entities
CREATE INDEX idx_club_tags ON club (tags);
