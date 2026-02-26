-- Aceite de Termos de Uso e Política de Privacidade (LGPD)
ALTER TABLE users ADD COLUMN terms_accepted_at TIMESTAMP;
ALTER TABLE users ADD COLUMN privacy_accepted_at TIMESTAMP;
