-- Usuários existentes antes da feature de verificação são considerados já verificados
UPDATE users SET email_verified = true WHERE email_verified = false;
