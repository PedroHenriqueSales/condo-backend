# ComuMinha API

API REST para anúncios entre moradores de condomínio. MVP minimalista focado em simplicidade e confiança.

## Stack

- Java 17+
- Spring Boot 3.x
- Spring Security (JWT)
- PostgreSQL
- Flyway
- Maven
- Swagger/OpenAPI (SpringDoc)

## Pré-requisitos

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

## Rodando localmente

1. **Suba o PostgreSQL local (recomendado via Docker Compose):**
   ```bash
   cd ../infra
   docker compose up -d
   ```

2. **Configure as variáveis de ambiente** (ou use os defaults em `application.yml`):
   ```bash
   export DB_HOST=localhost
   export DB_PORT=5432
   export DB_NAME=comuminha
   export DB_USER=postgres
   export DB_PASSWORD=postgres
   export JWT_SECRET=sua-chave-secreta-minimo-256-bits-para-producao
   ```

3. **Execute a aplicação:**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

4. A API estará disponível em `http://localhost:8080`

## Documentação da API (Swagger)

A documentação interativa da API está disponível através do Swagger UI:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

Para testar endpoints protegidos no Swagger:
1. Faça login ou registro através do endpoint `/api/auth/login` ou `/api/auth/register`
2. Copie o token JWT retornado
3. Clique no botão **"Authorize"** no topo da página do Swagger
4. Cole o token no formato: `Bearer <seu-token>` ou apenas `<seu-token>`
5. Agora você pode testar os endpoints protegidos

## Endpoints principais

### Autenticação (públicos)
- `POST /api/auth/register` - Cadastro
- `POST /api/auth/login` - Login (retorna JWT)

### Condomínios (autenticado)
- `POST /api/communities` - Criar condomínio
- `POST /api/communities/join` - Entrar por accessCode
- `GET /api/communities` - Listar meus condomínios
- `GET /api/communities/{id}` - Detalhes do condomínio

### Anúncios (autenticado)
- `POST /api/ads` - Criar anúncio
- `GET /api/ads?communityId=X&type=&search=` - Listar (paginado)
- `GET /api/ads/me` - Meus anúncios
- `GET /api/ads/{id}` - Detalhes
- `PATCH /api/ads/{id}/close` - Encerrar anúncio

### Contato
- `POST /api/contact/click` - Registrar clique em "Entrar em contato"

### Denúncia
- `POST /api/reports` - Denunciar anúncio

### Métricas
- `POST /api/events` - Registrar evento genérico

## Deploy no Railway

Use as variáveis de ambiente conforme o arquivo `env.railway.example`.
