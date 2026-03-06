-- Pedidos de exibição do código de acesso (comunidades próximas)

CREATE TABLE community_access_code_requests (
    id BIGSERIAL PRIMARY KEY,
    community_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_access_code_requests_community FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE,
    CONSTRAINT fk_access_code_requests_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_access_code_requests_community_user UNIQUE (community_id, user_id)
);

CREATE INDEX idx_access_code_requests_community_status ON community_access_code_requests(community_id, status);
CREATE INDEX idx_access_code_requests_user_id ON community_access_code_requests(user_id);
