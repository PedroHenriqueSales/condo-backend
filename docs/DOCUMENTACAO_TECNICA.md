# Documentação Técnica — Backend Aquidolado

## 1. Visão geral

O backend do Aquidolado é uma API REST em **Spring Boot 3.2** com **Java 21**, responsável por autenticação, gestão de condomínios e anúncios. Utiliza JWT stateless, JPA/Hibernate com PostgreSQL e Flyway para migrations.

## 2. Arquitetura

### 2.1 Estrutura de pacotes

```
br.com.aquidolado
├── config/          # Configurações (OpenAPI, Swagger, DevDataSeeder)
├── controller/      # REST controllers
├── domain/
│   ├── entity/     # Entidades JPA
│   └── enums/      # Enums (AdType, AdStatus, EventType, ReportReason)
├── dto/             # Request/Response DTOs
├── exception/       # GlobalExceptionHandler
├── repository/     # Spring Data JPA
├── security/        # JWT, filtros, UserDetails
├── service/         # Lógica de negócio
└── util/            # SecurityUtil
```

### 2.2 Fluxo de requisição

```
Cliente HTTP
    → JwtAuthenticationFilter (valida token, popula SecurityContext)
    → Controller
    → Service
    → Repository
    → Banco PostgreSQL
```

## 3. Modelo de dados

### 3.1 Entidades principais

| Entidade | Tabela | Descrição |
|----------|--------|-----------|
| `User` | `users` | Usuário com email, senha (BCrypt), WhatsApp, endereço |
| `Community` | `communities` | Condomínio com nome e access_code único |
| `Ad` | `ads` | Anúncio (título, descrição, tipo, preço, status) |
| `Report` | `reports` | Denúncia de anúncio |
| `EventLog` | `event_logs` | Log de eventos (métricas) |

### 3.2 Relacionamentos

- **User ↔ Community**: muitos-para-muitos via `user_communities`
- **Ad**: N:1 com User e Community
- **Report**: N:1 com Ad e User (reporter)
- **EventLog**: N:1 opcional com User e Community

### 3.3 Enums

| Enum | Valores |
|------|---------|
| `AdType` | SALE_TRADE, RENT, SERVICE |
| `AdStatus` | ACTIVE, CLOSED |
| `EventType` | LOGIN, REGISTER, CREATE_AD, CONTACT_CLICK, REPORT_AD |
| `ReportReason` | INAPPROPRIATE_CONTENT, SPAM, FRAUD, WRONG_CATEGORY, ALREADY_SOLD, OTHER |

## 4. Segurança

### 4.1 Autenticação JWT

- **Algoritmo:** HS256 (jjwt 0.12.5)
- **Expiração:** configurável via `jwt.expiration-ms` (padrão: 24h)
- **Header:** `Authorization: Bearer <token>`

### 4.2 Endpoints públicos

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /actuator/health`
- `GET /swagger-ui/**`, `/v3/api-docs/**`

### 4.3 Endpoints protegidos

Todos os demais endpoints exigem token JWT válido.

### 4.4 CORS

- **Dev:** origens liberadas para localhost e rede local (Vite dev server)
- **Prod:** configurar `app.cors.allowed-origins` (ex.: `CORS_ALLOWED_ORIGINS`)

### 4.5 Password

- BCrypt para hash de senhas

## 5. API REST

### 5.1 Autenticação

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/auth/register` | Registro (name, email, password, whatsapp?, address?) |
| POST | `/api/auth/login` | Login (email, password) → retorna `AuthResponse` com token |

### 5.2 Condomínios

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/communities` | Criar condomínio (name) |
| POST | `/api/communities/join` | Entrar por accessCode |
| GET | `/api/communities` | Listar meus condomínios |
| GET | `/api/communities/{id}` | Detalhes do condomínio |
| DELETE | `/api/communities/{id}/leave` | Sair do condomínio |

### 5.3 Anúncios

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/ads` | Criar anúncio |
| GET | `/api/ads?communityId=&type=&search=` | Listar (paginado) |
| GET | `/api/ads/me` | Meus anúncios |
| GET | `/api/ads/{id}` | Detalhes |
| PATCH | `/api/ads/{id}/close` | Encerrar (apenas criador) |

### 5.4 Outros

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/contact/click` | Registrar clique em contato |
| POST | `/api/reports` | Denunciar anúncio |
| POST | `/api/events` | Registrar evento genérico |

### 5.5 Paginação

Endpoints de listagem usam `Pageable` do Spring Data:

- `page` (0-based)
- `size` (padrão: 20)
- `sort` (opcional)

Resposta: `Page<T>` com `content`, `totalElements`, `totalPages`, `number`, etc.

## 6. Banco de dados

### 6.1 Migrations (Flyway)

- `V1__init.sql` — schema inicial (users, communities, user_communities, ads, event_logs, reports)
- `V2__fix_ads_description_text.sql` — ajustes em `ads.description`

### 6.2 Convenções

- Tabelas em **snake_case**
- `ddl-auto: validate` (não gera schema automaticamente)
- Índices em colunas de busca e foreign keys

### 6.3 Configuração

- **JDBC:** `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- **HikariCP:** pool configurável em `application.yml`

## 7. Configuração

### 7.1 Profiles

| Profile | Uso |
|---------|-----|
| `dev` | Logs DEBUG, show-sql, CORS amplo |
| `prod` | Logs limitados, CORS via env |

### 7.2 Variáveis de ambiente

| Variável | Descrição |
|----------|-----------|
| `DB_HOST` | Host PostgreSQL |
| `DB_PORT` | Porta PostgreSQL |
| `DB_NAME` | Nome do banco |
| `DB_USER` | Usuário |
| `DB_PASSWORD` | Senha |
| `JWT_SECRET` | Chave JWT (≥256 bits em prod) |
| `JWT_EXPIRATION_MS` | Expiração do token (ms) |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas (prod) |
| `PORT` | Porta da aplicação |

## 8. Tratamento de erros

`GlobalExceptionHandler` padroniza respostas de erro (400, 401, 403, 404, etc.) com mensagens consistentes.

## 9. Documentação OpenAPI

- **Swagger UI:** `/swagger-ui.html`
- **OpenAPI JSON:** `/v3/api-docs`
- Permite testar endpoints protegidos com token via botão "Authorize".
