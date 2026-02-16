-- Corrige tipo da coluna rating para INTEGER (compatível com JPA Integer)
ALTER TABLE recommendation_reactions ALTER COLUMN rating TYPE INTEGER USING rating::integer;
