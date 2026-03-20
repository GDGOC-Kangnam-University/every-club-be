-- Create tag table
CREATE TABLE "public"."tag" (
  "id" BIGSERIAL PRIMARY KEY,
  "name" VARCHAR(30) NOT NULL,
  CONSTRAINT "uk_tag_name" UNIQUE ("name")
);

-- Create club_tag table
CREATE TABLE "public"."club_tag" (
  "id" BIGSERIAL PRIMARY KEY,
  "club_id" BIGINT NOT NULL,
  "tag_id" BIGINT NOT NULL,
  CONSTRAINT "club_tag_club_id_fkey" FOREIGN KEY ("club_id") REFERENCES "public"."club" ("id") ON DELETE CASCADE,
  CONSTRAINT "club_tag_tag_id_fkey" FOREIGN KEY ("tag_id") REFERENCES "public"."tag" ("id") ON DELETE CASCADE,
  CONSTRAINT "uk_club_tag_club_tag" UNIQUE ("club_id", "tag_id")
);

-- Create index for reverse lookup
CREATE INDEX "idx_club_tag_tag_club" ON "public"."club_tag" ("tag_id", "club_id");
