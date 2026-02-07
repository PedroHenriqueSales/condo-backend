-- Corrige instalações antigas onde ads.description ficou como BYTEA
-- (o endpoint de listagem usa LOWER(description) e isso quebra com BYTEA)

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'ads'
          AND column_name = 'description'
          AND data_type = 'bytea'
    ) THEN
        ALTER TABLE ads
            ALTER COLUMN description TYPE TEXT
            USING convert_from(description, 'UTF8');
    END IF;
END $$;

