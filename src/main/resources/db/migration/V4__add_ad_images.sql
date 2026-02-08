CREATE TABLE ad_images (
    id BIGSERIAL PRIMARY KEY,
    ad_id BIGINT NOT NULL,
    url VARCHAR(512) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_ad_images_ad FOREIGN KEY (ad_id) REFERENCES ads(id) ON DELETE CASCADE
);
CREATE INDEX idx_ad_images_ad_id ON ad_images(ad_id);
