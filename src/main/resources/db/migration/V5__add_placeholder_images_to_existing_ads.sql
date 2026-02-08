-- Adiciona imagens placeholder aos anúncios existentes (2 por anúncio)
INSERT INTO ad_images (ad_id, url, sort_order)
SELECT id, 'https://picsum.photos/300/200?random=' || id || '-0', 0 FROM ads;
INSERT INTO ad_images (ad_id, url, sort_order)
SELECT id, 'https://picsum.photos/300/200?random=' || id || '-1', 1 FROM ads;
