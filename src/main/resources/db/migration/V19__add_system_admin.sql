-- Administrador do sistema: apenas estes usuários podem acessar o painel /api/admin
ALTER TABLE users ADD COLUMN IF NOT EXISTS system_admin BOOLEAN NOT NULL DEFAULT false;

-- Opcional: promover um usuário existente por email (descomente e ajuste o email)
-- UPDATE users SET system_admin = true WHERE email = 'admin@example.com';
