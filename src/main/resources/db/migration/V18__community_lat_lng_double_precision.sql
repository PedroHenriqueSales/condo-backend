-- Alinha tipo das colunas com JPA Double (evita schema-validation)
ALTER TABLE communities ALTER COLUMN latitude TYPE DOUBLE PRECISION USING latitude::double precision;
ALTER TABLE communities ALTER COLUMN longitude TYPE DOUBLE PRECISION USING longitude::double precision;
