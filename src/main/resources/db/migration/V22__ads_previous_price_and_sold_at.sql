-- Extensões para anúncios: preço anterior (para exibir valor riscado)
-- e momento da venda (para manter \"Vendido\" no feed por 24h).

ALTER TABLE ads
    ADD COLUMN IF NOT EXISTS previous_price DECIMAL(12, 2),
    ADD COLUMN IF NOT EXISTS sold_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_ads_sold_at ON ads(sold_at);

