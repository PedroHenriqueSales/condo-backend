-- Remove imagens placeholder aleatórias (picsum.photos) dos anúncios.
-- Os anúncios sem imagem passam a exibir a logo em negativo no frontend.
DELETE FROM ad_images WHERE url LIKE 'https://picsum.photos/%';
