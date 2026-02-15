-- Colunas em Ad para indicações (type = RECOMMENDATION)
ALTER TABLE ads ADD COLUMN recommended_contact TEXT;
ALTER TABLE ads ADD COLUMN service_type VARCHAR(100);

-- Reações à indicação (like/dislike)
CREATE TABLE recommendation_reactions (
    id BIGSERIAL PRIMARY KEY,
    ad_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    kind VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_recommendation_reactions_ad FOREIGN KEY (ad_id) REFERENCES ads(id) ON DELETE CASCADE,
    CONSTRAINT fk_recommendation_reactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_recommendation_reactions_ad_user UNIQUE (ad_id, user_id)
);
CREATE INDEX idx_recommendation_reactions_ad_id ON recommendation_reactions(ad_id);
CREATE INDEX idx_recommendation_reactions_user_id ON recommendation_reactions(user_id);

-- Comentários na indicação
CREATE TABLE recommendation_comments (
    id BIGSERIAL PRIMARY KEY,
    ad_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_recommendation_comments_ad FOREIGN KEY (ad_id) REFERENCES ads(id) ON DELETE CASCADE,
    CONSTRAINT fk_recommendation_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_recommendation_comments_ad_id ON recommendation_comments(ad_id);
CREATE INDEX idx_recommendation_comments_created_at ON recommendation_comments(created_at);

-- Curtir comentário
CREATE TABLE comment_likes (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_comment_likes_comment FOREIGN KEY (comment_id) REFERENCES recommendation_comments(id) ON DELETE CASCADE,
    CONSTRAINT fk_comment_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_comment_likes_comment_user UNIQUE (comment_id, user_id)
);
CREATE INDEX idx_comment_likes_comment_id ON comment_likes(comment_id);
CREATE INDEX idx_comment_likes_user_id ON comment_likes(user_id);
