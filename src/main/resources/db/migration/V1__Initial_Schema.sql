-- ─────────────────────────────────────────────────────────────────────────────
-- Extensions
-- ─────────────────────────────────────────────────────────────────────────────
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ─────────────────────────────────────────────────────────────────────────────
-- category
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE category (
    id   BIGSERIAL   PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

-- ─────────────────────────────────────────────────────────────────────────────
-- users
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE users (
    id                BIGSERIAL    PRIMARY KEY,
    email             VARCHAR(255) NOT NULL UNIQUE,
    password_hash     VARCHAR(60),
    nickname          VARCHAR(255),
    profile_image_url VARCHAR(255),
    department        VARCHAR(255),
    student_id        VARCHAR(255),
    phone_number      VARCHAR(255),
    bio               VARCHAR(500),
    role              VARCHAR(255) NOT NULL,
    email_verified    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP,
    deleted_at        TIMESTAMP
);

-- ─────────────────────────────────────────────────────────────────────────────
-- college / major
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE college (
    id   BIGSERIAL   PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE major (
    id         BIGSERIAL   PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    college_id BIGINT      NOT NULL REFERENCES college(id)
);

-- ─────────────────────────────────────────────────────────────────────────────
-- club
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE club (
    id                BIGSERIAL     PRIMARY KEY,
    slug              VARCHAR(100)  NOT NULL UNIQUE,
    name              VARCHAR(20)   NOT NULL,
    summary           VARCHAR(200)  NOT NULL,
    description       TEXT,
    logo_url          VARCHAR(2048),
    banner_url        VARCHAR(2048),
    join_form_url     VARCHAR(2048),
    recruiting_status VARCHAR(255)  NOT NULL,
    activity_cycle    VARCHAR(50),
    has_fee           BOOLEAN       NOT NULL DEFAULT FALSE,
    is_public         BOOLEAN       NOT NULL DEFAULT FALSE,
    major_id          BIGINT        REFERENCES major(id),
    category_id       BIGINT        NOT NULL REFERENCES category(id),
    created_by        BIGINT        NOT NULL REFERENCES users(id),
    created_at        TIMESTAMP     NOT NULL,
    updated_at        TIMESTAMP     NOT NULL,
    deleted_at        TIMESTAMP
);

-- ─────────────────────────────────────────────────────────────────────────────
-- club_likes
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE club_likes (
    user_id BIGINT NOT NULL REFERENCES users(id),
    club_id BIGINT NOT NULL REFERENCES club(id),
    PRIMARY KEY (user_id, club_id)
);

-- ─────────────────────────────────────────────────────────────────────────────
-- tag / club_tag
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE tag (
    id   BIGSERIAL   PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    CONSTRAINT uk_tag_name UNIQUE (name)
);

CREATE TABLE club_tag (
    id      BIGSERIAL PRIMARY KEY,
    club_id BIGINT    NOT NULL REFERENCES club(id) ON DELETE CASCADE,
    tag_id  BIGINT    NOT NULL REFERENCES tag(id)  ON DELETE CASCADE,
    CONSTRAINT uk_club_tag_club_tag UNIQUE (club_id, tag_id)
);

-- ─────────────────────────────────────────────────────────────────────────────
-- club_request
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE club_request (
    id           BIGSERIAL   PRIMARY KEY,
    public_id    UUID        NOT NULL UNIQUE,
    requested_by BIGINT      NOT NULL REFERENCES users(id),
    payload      TEXT        NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    admin_memo   VARCHAR(500),
    reviewed_by  BIGINT      REFERENCES users(id),
    reviewed_at  TIMESTAMP,
    created_at   TIMESTAMP   NOT NULL
);

-- ─────────────────────────────────────────────────────────────────────────────
-- signup_verification
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE signup_verification (
    id                      BIGSERIAL    PRIMARY KEY,
    email                   VARCHAR(255) NOT NULL,
    otp_code                VARCHAR(255) NOT NULL,
    otp_expires_at          TIMESTAMP    NOT NULL,
    verified_at             TIMESTAMP,
    signup_token_hash       VARCHAR(64),
    signup_token_expires_at TIMESTAMP,
    used                    BOOLEAN      NOT NULL DEFAULT FALSE,
    attempt_count           INTEGER      NOT NULL DEFAULT 0,
    resend_count            INTEGER      NOT NULL DEFAULT 0,
    created_at              TIMESTAMP    NOT NULL
);

-- ─────────────────────────────────────────────────────────────────────────────
-- club_admin
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE club_admin (
    id         BIGSERIAL   PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES users(id),
    club_id    BIGINT      NOT NULL REFERENCES club(id),
    role       VARCHAR(10) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_club_admin_user_club UNIQUE (user_id, club_id)
);

-- ─────────────────────────────────────────────────────────────────────────────
-- Indexes
-- ─────────────────────────────────────────────────────────────────────────────
CREATE INDEX idx_club_name_gin            ON club      USING GIN (name gin_trgm_ops);
CREATE INDEX idx_club_tag_tag_club        ON club_tag  (tag_id, club_id);
CREATE INDEX idx_club_admin_club_id       ON club_admin (club_id);
CREATE INDEX idx_club_admin_user_id       ON club_admin (user_id);
CREATE UNIQUE INDEX ux_club_admin_one_lead_per_club
    ON club_admin (club_id) WHERE role = 'LEAD';