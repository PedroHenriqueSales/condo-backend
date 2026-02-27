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
| `dev` | Desenvolvimento local: logs DEBUG, `show-sql`, CORS amplo, seed de dados (`DevDataSeeder`) |
| `homolog` | Homologação/staging: mesmo comportamento de produção, mas com banco/URLs/secrets de teste |
| `prod` | Produção: logs limitados, CORS via env, Cloudinary para storage, sem seed de dados |

### 7.2 Variáveis de ambiente

| Variável | Descrição |
|----------|-----------|
| `DB_HOST` | Host PostgreSQL (diferente para dev/homolog/prod) |
| `DB_PORT` | Porta PostgreSQL |
| `DB_NAME` | Nome do banco (pode variar por ambiente, ex.: `aquidolado_dev`, `aquidolado_homolog`, `aquidolado_prod`) |
| `DB_USER` | Usuário |
| `DB_PASSWORD` | Senha |
| `JWT_SECRET` | Chave JWT (≥256 bits em prod; usar valores distintos em homolog e prod) |
| `JWT_EXPIRATION_MS` | Expiração do token (ms) |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas (homolog/prod) |
| `PORT` | Porta da aplicação |
| `SPRING_PROFILES_ACTIVE` | Profile ativo: `dev` (local), `homolog` (staging) ou `prod` (produção) |

## 8. Tratamento de erros

`GlobalExceptionHandler` padroniza respostas de erro (400, 401, 403, 404, etc.) com mensagens consistentes.

## 9. Documentação OpenAPI

- **Swagger UI:** `/swagger-ui.html`
- **OpenAPI JSON:** `/v3/api-docs`
- Permite testar endpoints protegidos com token via botão "Authorize".

## 10. Deploy (homologação)

O backend usa **Render** (free tier) com custo zero. O banco PostgreSQL é provisionado no mesmo serviço.

**Dockerfile:**

- Multi-stage: build com `eclipse-temurin:21-jdk-alpine` e Maven; runtime com `eclipse-temurin:21-jre-alpine`
- Cria diretório `uploads` (ephemeral no Render)
- `EXPOSE 8080`, `ENTRYPOINT ["java", "-jar", "app.jar"]`

**Variáveis de ambiente (Render - homologação):**

- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` — do PostgreSQL Render (banco de homolog)
- `JWT_SECRET` — mínimo 32 caracteres (diferente do de produção)
- `CORS_ALLOWED_ORIGINS` — URL do frontend de homolog (ex.: `https://aquidolado-xxxx.vercel.app`)
- `SPRING_PROFILES_ACTIVE=homolog`
- `PORT` — definido automaticamente pelo Render

**Arquivos de exemplo:**

- `env.render.example` — documenta as variáveis para homologação
- `env.railway.example` — similar, para Railway (opcional)

**Limitações do free tier:**

- Spin-down após 15 min de inatividade; cold start ~1 min
- PostgreSQL expira em 30 dias
- Filesystem ephemeral: imagens em `uploads/` são perdidas em redeploy

### 10.1 Checklist de validação por ambiente

**Dev (`dev`)**

- Subir PostgreSQL local (Docker Compose).
- Rodar com `SPRING_PROFILES_ACTIVE=dev` (ou parâmetro do Maven).
- Acessar Swagger UI (`/swagger-ui.html`) e testar fluxo básico (registro, login, criação de anúncio).

**Homolog (`homolog`)**

- Garantir que `SPRING_PROFILES_ACTIVE=homolog` está configurado na plataforma (Render/Railway).
- Confirmar variáveis de banco (`DB_*`), JWT, Cloudinary e `CORS_ALLOWED_ORIGINS`.
- Verificar `/actuator/health`.
- Executar smoke test: registro/login, entrada em comunidade, criação de anúncio, upload de imagem, envio de email (mock ou real).

**Produção (`prod`)**

- Criar banco de produção (schema vazio, apenas com migrations Flyway).
- Configurar variáveis de ambiente com valores definitivos (banco, JWT, CORS, Cloudinary, EMAIL_FROM).
- Subir com `SPRING_PROFILES_ACTIVE=prod` e monitorar logs iniciais.
- Executar smoke test completo (login, CRUD básico, upload de imagem, envio de email real, navegação pelo fluxo principal).

## 11. Armazenamento de Imagens (Storage)

### 11.1 Implementações Disponíveis

O sistema suporta duas implementações de armazenamento via interface `StorageService`:

| Implementação | Profile/Config | Uso |
|---------------|----------------|-----|
| `LocalStorageService` | `app.storage.type=local` (padrão) | Desenvolvimento local |
| `CloudinaryStorageService` | `app.storage.type=cloudinary` + `@Profile("prod")` | Produção/homologação |

### 11.2 LocalStorageService (Desenvolvimento)

- Salva arquivos no filesystem local (`uploads/`)
- Servido via Spring MVC em `/uploads/**`
- Configuração em `app.storage.local.path` e `app.storage.local.url-prefix`
- **Limitação:** Não funciona em produção no Render (filesystem ephemeral)

### 11.3 CloudinaryStorageService (Produção / Homologação)

**Configuração:**

1. Criar conta no [Cloudinary](https://cloudinary.com) (free tier disponível)
2. Obter credenciais do Dashboard:
   - `cloud_name`
   - `api_key`
   - `api_secret`
3. Configurar variáveis de ambiente no Render (homolog) e no provedor de produção:
   ```
   CLOUDINARY_CLOUD_NAME=seu-cloud-name
   CLOUDINARY_API_KEY=sua-api-key
   CLOUDINARY_API_SECRET=sua-api-secret
   ```
4. Definir `app.storage.type=cloudinary` em `application-homolog.yml` e `application-prod.yml`

**Características:**

- Upload direto para Cloudinary via SDK Java
- URLs públicas retornadas (ex.: `https://res.cloudinary.com/...`)
- Validações: máximo 5MB, tipos JPEG/PNG/WebP
- Estrutura de pastas: `{folder}/{prefix}/{uuid}` (ex.: `aquidolado/ads/123/uuid.jpg`)
- Delete individual e por prefixo suportados

**Limites do Free Tier:**

- 25 créditos mensais (suficiente para testes)
- Para produção, considerar planos pagos (Plus: $89-99/mês, Advanced: $224-249/mês)
- Créditos cobrem: storage, bandwidth, transformações, CDN

**Estrutura no Cloudinary:**

```
aquidolado/
  └── ads/
      └── {adId}/
          └── {uuid}.jpg
```

### 11.4 Seleção Automática

A seleção entre implementações é feita automaticamente via anotações Spring:

- `@ConditionalOnProperty`: `LocalStorageService` só ativa quando `app.storage.type=local` (padrão)
- `@Profile({"prod", "homolog"})`: `CloudinaryStorageService` só ativa quando profile `prod` **ou** `homolog` está ativo **e** `app.storage.type=cloudinary`

Isso garante que apenas uma implementação esteja ativa por vez, evitando conflitos.

### 11.5 WebMvcConfig

O `WebMvcConfig` registra o handler `/uploads/**` apenas quando `app.storage.type=local`, evitando conflitos em produção com Cloudinary.
