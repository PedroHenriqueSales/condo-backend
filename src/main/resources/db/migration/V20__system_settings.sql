-- Configurações globais do sistema (key/value), ex.: adsEnabled para ligar/desligar AdSense
CREATE TABLE IF NOT EXISTS system_settings (
    setting_key VARCHAR(255) PRIMARY KEY,
    value TEXT NOT NULL
);

-- Valor padrão: anúncios (AdSense) habilitados
INSERT INTO system_settings (setting_key, value) VALUES ('adsEnabled', 'true')
ON CONFLICT (setting_key) DO NOTHING;
