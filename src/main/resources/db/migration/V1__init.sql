-- Aquidolado - Schema inicial
-- Tabelas em snake_case

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    whatsapp VARCHAR(50),
    address VARCHAR(500),
    invites_remaining INTEGER NOT NULL DEFAULT 5,
    active BOOLEAN NOT NULL DEFAULT true
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(active);

CREATE TABLE communities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    access_code VARCHAR(20) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    created_by BIGINT NOT NULL,
    CONSTRAINT fk_communities_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_communities_access_code ON communities(access_code);
CREATE INDEX idx_communities_created_by ON communities(created_by);

CREATE TABLE user_communities (
    user_id BIGINT NOT NULL,
    community_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, community_id),
    CONSTRAINT fk_user_communities_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_communities_community FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_communities_user_id ON user_communities(user_id);
CREATE INDEX idx_user_communities_community_id ON user_communities(community_id);

CREATE TABLE ads (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(20) NOT NULL,
    price DECIMAL(12, 2),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    user_id BIGINT NOT NULL,
    community_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_ads_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_ads_community FOREIGN KEY (community_id) REFERENCES communities(id)
);

CREATE INDEX idx_ads_user_id ON ads(user_id);
CREATE INDEX idx_ads_community_id ON ads(community_id);
CREATE INDEX idx_ads_status ON ads(status);
CREATE INDEX idx_ads_community_status ON ads(community_id, status);

CREATE TABLE event_logs (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    user_id BIGINT,
    community_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_event_logs_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_event_logs_community FOREIGN KEY (community_id) REFERENCES communities(id)
);

CREATE INDEX idx_event_logs_event_type ON event_logs(event_type);
CREATE INDEX idx_event_logs_user_id ON event_logs(user_id);
CREATE INDEX idx_event_logs_community_id ON event_logs(community_id);
CREATE INDEX idx_event_logs_created_at ON event_logs(created_at);

CREATE TABLE reports (
    id BIGSERIAL PRIMARY KEY,
    ad_id BIGINT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    reporter_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_reports_ad FOREIGN KEY (ad_id) REFERENCES ads(id),
    CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_user_id) REFERENCES users(id)
);

CREATE INDEX idx_reports_ad_id ON reports(ad_id);
CREATE INDEX idx_reports_reporter_user_id ON reports(reporter_user_id);
