-- Uma denúncia por usuário por anúncio
ALTER TABLE reports ADD CONSTRAINT uq_reports_ad_reporter UNIQUE (ad_id, reporter_user_id);

-- Distinguir pausa pelo usuário de suspensão automática por denúncias
ALTER TABLE ads ADD COLUMN suspended_by_reports_at TIMESTAMP;
