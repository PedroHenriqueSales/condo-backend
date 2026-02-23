-- Communities: type (open/private) and postal code (CEP); admins and join requests

-- Add columns to communities (existing rows get defaults first)
ALTER TABLE communities ADD COLUMN is_private BOOLEAN;
UPDATE communities SET is_private = false WHERE is_private IS NULL;
ALTER TABLE communities ALTER COLUMN is_private SET NOT NULL;
ALTER TABLE communities ALTER COLUMN is_private SET DEFAULT false;

ALTER TABLE communities ADD COLUMN postal_code VARCHAR(10);
UPDATE communities SET postal_code = '00000000' WHERE postal_code IS NULL;
ALTER TABLE communities ALTER COLUMN postal_code SET NOT NULL;

CREATE INDEX idx_communities_is_private ON communities(is_private);

-- community_admins: who can administer the community (creator is first admin)
CREATE TABLE community_admins (
    community_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (community_id, user_id),
    CONSTRAINT fk_community_admins_community FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE,
    CONSTRAINT fk_community_admins_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_community_admins_user_id ON community_admins(user_id);
CREATE INDEX idx_community_admins_community_id ON community_admins(community_id);

-- Insert creator as admin for all existing communities
INSERT INTO community_admins (community_id, user_id)
SELECT id, created_by FROM communities
ON CONFLICT DO NOTHING;

-- community_join_requests: for private communities, pending approval
CREATE TABLE community_join_requests (
    id BIGSERIAL PRIMARY KEY,
    community_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_join_requests_community FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE,
    CONSTRAINT fk_join_requests_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_join_requests_community_user UNIQUE (community_id, user_id)
);
CREATE INDEX idx_join_requests_community_status ON community_join_requests(community_id, status);
CREATE INDEX idx_join_requests_user_id ON community_join_requests(user_id);
