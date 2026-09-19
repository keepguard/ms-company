# ms-company - System Design

## 1. Propósito e Domínio
- **Responsabilidade Principal:** Gerenciar o cadastro e o ciclo de vida de empresas (PJ) na plataforma KeepGuard, incluindo dados cadastrais, endereços, contatos, representantes, contas bancárias, CNAEs, políticas internas e canais de MFA. Expõe API REST para CRUD/consulta e orquestra provisionamento de roles no serviço de autenticação após criação.
- **Domínio/Subdomínio:** Cadastro Empresarial / Company Master Data (subdomínio de Tenant & Organização; suporte a IAM via `tenantId` e provisionamento de roles).

## 2. Tech Stack Local
- **Linguagem & Framework:** Java 25 / Spring Boot 3.5.3; Spring Cloud 2025.0.3 (OpenFeign); Spring Web, Data JPA, Validation, Cache, AOP, Actuator; Springdoc OpenAPI 2.3.0; Lombok 1.18.40; Resilience4j 2.2.0; biblioteca compartilhada `lib-common` (`1.0.37-SNAPSHOT`); virtual threads habilitadas (`spring.threads.virtual.enabled: true`).
- **Persistência e Cache:** PostgreSQL (`keepguard_api_db`, schema Hibernate `ms_company`, HikariCP); Redis (standalone em `local`; cluster de 6 nós em `dev`/`prod`) com TTL de ~30 dias para company, address, bank-account, contact e representative.
- **Mensageria:** RabbitMQ via `spring-boot-starter-amqp` — **não há consumers** (`@RabbitListener`) neste serviço. Publicação de auditoria outbound configurada em `keepguard.audit` (exchange por perfil: `srv-audit-exchange-local|dev|prod`, routing-key `audit.event`, `source-service: ms-company`), acionada por `@LogOperation(audit = true)` da `lib-common` nos Command Services.

## 3. Arquitetura Interna
- **Padrão Utilizado:** Arquitetura Hexagonal (Ports & Adapters) com elementos de DDD (agregado `Company` e entidades de domínio ricas) e CQRS leve (separação `*CommandService` / `*QueryService` / `*UseCaseService` por bounded context interno).
- **Módulos Principais:**
  - `adapters/in/rest` — Controllers REST + DTOs + Adapter Mappers (company, address, bankaccount, cnae, contact, representative, companypolicy, health, helper).
  - `adapters/out/feign` — Cliente Feign `AuthProvisionClient` + `AuthRoleProvisionAdapter`.
  - `application/port/in` — Ports de entrada (`CompanyPort`, `AddressPort`, `BankAccountPort`, `CnaePort`, `ContactPort`, `RepresentativePort`, `CompanyPolicyPort`).
  - `application/port/out` — Ports de saída (persistence, cache, metrics, auth).
  - `application/service/{company,address,bankaccount,cnae,contact,representative,companypolicy}` — Use cases Command/Query.
  - `domain/entity` — `Company` (agregado raiz), `Address`, `BankAccount`, `Cnae`, `Contact`, `Representative`, `CompanyPolicy`, `CompanyMfaChannel`.
  - `domain/enums` — `CompanyStatusEnum`, `TaxRegimeEnum`, `AccountTypeEnum`, `PolicyStatusEnum`, `MfaChannelEnum`, `MfaPolicyEnum`.
  - `infrastructure/persistence` — JPA entities, Spring Data repositories, Repository Adapters, JPA mappers.
  - `infrastructure/redis` — Cache services com Circuit Breaker Resilience4j (`redisCache`).
  - `infrastructure/{config,filter,metrics,rest,context}` — Swagger, resilience, Correlation ID, Actuator/Prometheus, `GlobalExceptionHandler`.

## 4. Superfície de Contato (I/O)
- **Endpoints Expostos Principais:**
  - `POST/PUT/GET/DELETE /api/v1/companies` — CRUD; lookups por `{id}`, `x-tenant-id/{tenantId}`, `code/{codeCompany}`, `cnpj/{cnpj}`, `search`.
  - Transições de status: `PATCH /{id}/approve|reject|activate|deactivate|suspend|block`; MFA: `PUT /{id}/mfa-channels`.
  - ` /api/v1/addresses` — CRUD + activate/deactivate + listagens/search por company.
  - ` /api/v1/bank-accounts` — CRUD + activate/deactivate + listagens/search por company.
  - ` /api/v1/contacts` — CRUD + activate/deactivate + listagens/search por company.
  - ` /api/v1/representatives` — CRUD + activate/deactivate + busca por CPF/email + listagens por company.
  - ` /api/v1/companies/{companyId}/cnaes` — CRUD + activate/deactivate + `set-principal` + principal/active.
  - ` /api/v1/companies/{companyId}/policies` — create/update/delete + list/active.
  - Health/ops: `/api/v1/health`, `/api/v1/helper/health|info`; Actuator: `/actuator/health|info|prometheus`.
  - Porta HTTP: `8583` (profile `local`), `8083` (`dev`/`prod`/base).
- **Dependências Externas:**
  - **ms-auth** (Feign `auth-service`, URL `AUTH_SERVICE_URL` default `http://localhost:8081`) — `POST /api/v1/companies/{companyId}/roles/provision` na criação de empresa.
  - **PostgreSQL** — schema `ms_company` (tabelas: `companies`, `company_addresses`, `company_bank_accounts`, `company_cnaes`, `company_contacts`, `company_representatives`, `company_policies`, MFA channels).
  - **Redis** — cache de leitura/lookup (id, cnpj, codeCompany, tenantId, views simples).
  - **RabbitMQ** — publicação de eventos de auditoria (`keepguard.audit.*`) via `lib-common`.
  - **lib-common** — exceções, validação de CNPJ (`BrazilianValidationUtils`), `@LogOperation`/audit, `@MetricsEndpoint`, `MetricsConfig`.

## 5. Invariantes Locais e Observações
- **Multi-tenancy:** cada `Company` possui `tenantId` UUID único, imutável (`updatable = false`); gerado na criação se ausente. Lookup dedicado por tenant e cache por `tenantId`. Header `X-Tenant-Id` é capturado no `CorrelationIdFilter` (contexto de log), sem enforcement de isolamento por filtro nesta camada.
- **Identificadores imutáveis/únicos:** `id`, `codeCompany` e `tenantId` únicos e não atualizáveis; `cnpj` único (14 dígitos, validado via `BrazilianValidationUtils`).
- **Ciclo de vida (`CompanyStatusEnum`):** criação inicia em `PENDING_APPROVAL`. `approve` só de `PENDING_APPROVAL` → `ACTIVE` e exige endereço ativo, conta bancária ativa, CNAE principal ativo, contato ativo, representante ativo **e** pelo menos uma `CompanyPolicy` ativa. `reject` → `BLOCKED`. `activate` só de `INACTIVE`; `deactivate`/`suspend` só de `ACTIVE`; `block` sem restrição de origem. Empresas `BLOCKED`/`SUSPENDED` bloqueiam operações de edição (`validateStatusForOperations`).
- **Cardinalidade no agregado:** endereço ativo e conta bancária ativa são exclusivos (novo ativo desativa os demais); múltiplos contatos/representantes ativos permitidos; um único CNAE principal por vez.
- **Regime tributário padrão:** `SIMPLES_NACIONAL` se omitido.
- **Cache + resiliência:** Redis com Circuit Breaker/Retry (`redisCache`); DB com Retry/Bulkhead (`databaseOperation`). TTL padrão 2.592.000s (~30 dias). Invalidação ampla (`clearAllCompanyCache`) em mutações.
- **JPA:** `ddl-auto: update` em local/dev; `validate` em prod. Schema fixo `ms_company`.
- **Observabilidade:** Prometheus/Actuator, métricas de negócio (`company_*_total`), OpenAPI/Swagger habilitado, Correlation ID (`X-Correlation-ID`) por request.
- **Sem `.env.example`** no diretório do serviço; variáveis relevantes observadas nos YAMLs/código: `RABBITMQ_*`, `AUTH_SERVICE_URL`, `KEEPGUARD_AUDIT_EXCHANGE`.
