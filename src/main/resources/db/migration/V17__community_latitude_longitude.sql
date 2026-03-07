-- Geolocalização: latitude e longitude; postal_code opcional para legado

ALTER TABLE communities ADD COLUMN latitude DECIMAL(10, 7);
ALTER TABLE communities ADD COLUMN longitude DECIMAL(10, 7);

ALTER TABLE communities ALTER COLUMN postal_code DROP NOT NULL;
