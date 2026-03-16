-- GIN trigram index on club.name for ILIKE and word_similarity search
-- pg_trgm extension is already enabled in V2
CREATE INDEX IF NOT EXISTS idx_club_name_gin
    ON club USING GIN (name gin_trgm_ops);