-- Notifications and last seen ads per community

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT,
    ad_id BIGINT,
    comment_id BIGINT,
    community_id BIGINT,
    join_request_id BIGINT,
    report_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    read_at TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_ad FOREIGN KEY (ad_id) REFERENCES ads(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_comment FOREIGN KEY (comment_id) REFERENCES recommendation_comments(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_community FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_join_request FOREIGN KEY (join_request_id) REFERENCES community_join_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_report FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_id_created_at ON notifications(user_id, created_at DESC);
CREATE INDEX idx_notifications_user_id_read_at ON notifications(user_id, read_at);
CREATE INDEX idx_notifications_type ON notifications(type);

ALTER TABLE user_communities ADD COLUMN last_ads_seen_at TIMESTAMP;

