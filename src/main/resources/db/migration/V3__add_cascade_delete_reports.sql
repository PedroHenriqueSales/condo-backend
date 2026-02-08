-- Permite exclusão real de anúncios: reports são removidos em cascata
ALTER TABLE reports DROP CONSTRAINT fk_reports_ad;
ALTER TABLE reports ADD CONSTRAINT fk_reports_ad
  FOREIGN KEY (ad_id) REFERENCES ads(id) ON DELETE CASCADE;
