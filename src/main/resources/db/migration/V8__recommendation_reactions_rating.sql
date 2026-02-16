-- Avaliações por nota (0-5) nas indicações (substitui like/dislike)
ALTER TABLE recommendation_reactions ADD COLUMN rating SMALLINT;

UPDATE recommendation_reactions SET rating = 5 WHERE kind = 'LIKE';
UPDATE recommendation_reactions SET rating = 0 WHERE kind = 'DISLIKE';

ALTER TABLE recommendation_reactions ALTER COLUMN rating SET NOT NULL;
ALTER TABLE recommendation_reactions DROP COLUMN kind;
ALTER TABLE recommendation_reactions ADD CONSTRAINT chk_recommendation_reactions_rating CHECK (rating >= 0 AND rating <= 5);
