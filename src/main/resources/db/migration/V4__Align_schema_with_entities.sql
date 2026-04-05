-- Create new tables for college and major
CREATE TABLE "public"."college" (
  "id" BIGSERIAL PRIMARY KEY,
  "name" VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE "public"."major" (
  "id" BIGSERIAL PRIMARY KEY,
  "name" VARCHAR(50) NOT NULL,
  "college_id" BIGINT NOT NULL,
  CONSTRAINT "major_college_id_fkey" FOREIGN KEY ("college_id") REFERENCES "public"."college" ("id") ON DELETE NO ACTION
);

-- Update users table: 자바 코드 매핑에 맞춰 email_verified 추가
ALTER TABLE "public"."users" ADD COLUMN "email_verified" BOOLEAN NOT NULL DEFAULT FALSE;

-- Update club table
ALTER TABLE "public"."club" ADD COLUMN "major_id" BIGINT;
ALTER TABLE "public"."club" ADD CONSTRAINT "club_major_id_fkey" FOREIGN KEY ("major_id") REFERENCES "public"."major" ("id") ON DELETE NO ACTION;
ALTER TABLE "public"."club" DROP COLUMN "department";

-- Create email_verification table
CREATE TABLE "public"."email_verification" (
  "id" BIGSERIAL PRIMARY KEY,
  "user_id" BIGINT NOT NULL UNIQUE,
  "verification_code" VARCHAR(255) NOT NULL,
  "expires_at" TIMESTAMP NOT NULL,
  "created_at" TIMESTAMP NOT NULL,
  CONSTRAINT "email_verification_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."users" ("id") ON DELETE NO ACTION
);

-- Create club_request table
CREATE TABLE "public"."club_request" (
  "id" BIGSERIAL PRIMARY KEY,
  "public_id" UUID NOT NULL UNIQUE,
  "requested_by" BIGINT NOT NULL,
  "payload" TEXT NOT NULL,
  "status" VARCHAR(255) NOT NULL,
  "admin_memo" VARCHAR(500),
  "reviewed_by" BIGINT,
  "reviewed_at" TIMESTAMP,
  "created_at" TIMESTAMP NOT NULL,
  CONSTRAINT "club_request_requested_by_fkey" FOREIGN KEY ("requested_by") REFERENCES "public"."users" ("id") ON DELETE NO ACTION,
  CONSTRAINT "club_request_reviewed_by_fkey" FOREIGN KEY ("reviewed_by") REFERENCES "public"."users" ("id") ON DELETE NO ACTION
);
