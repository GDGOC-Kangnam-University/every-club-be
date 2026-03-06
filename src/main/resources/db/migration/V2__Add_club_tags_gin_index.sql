-- Enable trigram extension for GIN index on LIKE queries
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Add GIN index for tag search performance
CREATE INDEX IF NOT EXISTS idx_club_tags_gin ON club USING GIN (tags gin_trgm_ops);
