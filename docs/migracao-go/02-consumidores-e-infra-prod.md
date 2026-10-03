# ms-company — consumidores e infra de produção (levantamento para troca Java → Go)

Data: 2026-10-03. Caminhos relativos a `/Users/rafaelnogueirasoares/Projetos/keepguard/`.
`KC` = `keepguard-core/`, `MSC` = `keepguard-core/backend/ms/ms-company/`.
Dados "ao vivo" vieram de `kubectl get/top` read-only no namespace `keepguard` (sem valores de secret).

---

## 0. Resumo executivo

- **Só 2 dos ~90 endpoints do ms-company são chamados por alguém**: `GET /api/v1/companies/x-tenant-id/{tenantId}` (4 consumidores) e `GET /api/v1/companies/{id}` (1 consumidor, ms-auth). Nenhum endpoint de escrita tem consumidor no repo, nem o front. O ms-company não está exposto no Ingress.
- **Existe um segundo contrato, fora do HTTP: o Redis.** bff-auth e bff-core leem direto a chave `company_cache:tenantId:<tenantId>` (e o bff-auth também `company_cache:simple:tenantId:<tenantId>`), gravada pelo ms-company com JSON do `CompanyViewDTO`. O serviço em Go tem que manter **o mesmo nome de chave, a mesma normalização (trim + lowercase), o mesmo formato JSON e o mesmo Redis/db 0**. Se não fizer isso, os BFFs passam a ter cache miss e todo request vai bater HTTP. Não quebra nada, mas a carga sobe.
- **Terceiro contrato: RabbitMQ (auditoria).** As escritas anotadas com `@LogOperation` publicam evento de auditoria no exchange `srv-audit-exchange-prod`, routing key `audit.event`, `sourceService=ms-company` (via lib-common). Hoje nenhum consumidor dispara escrita, então na prática nada é publicado. Se o Go não publicar, nada quebra agora.
- **Prod diverge do repo:** a imagem rodando é `ms-company:1.0.20-1.0.0` (versão antiga, não é SHA), com profile `local`, **sem probes**, limit de 1Gi, sem `AUTH_SERVICE_URL` e sem release Helm. O Helm do repo (512Mi, probes, tag 1.0.9) não é o que está aplicado.
- **Ninguém cria o schema por script.** Quem cria tabelas é o Hibernate `ddl-auto: update` (o profile `local` está ativo em prod). Não há Flyway nem nenhum `CREATE TABLE` de company no repo. A carga inicial de dados veio por `pg_dump`/`pg_restore` (`KC/scripts/sync-apps-data-to-prod.sh`).
- **Memória:** o pod usa 772Mi (os Java do core ficam entre 464 e 817Mi). Os ms em Go do cluster usam 11–12Mi. Os 8 ms Java somam ≈5,4Gi RSS num nó único de ~16Gi que já está com 72% de memória ocupada.

---

## 1. Consumidores

### 1.1 Tabela endpoint × consumidor

| Endpoint do ms-company | bff-auth | bff-core | bff-invest (investbot) | ms-auth | front / achadinhos / ms-billing / ms-user / srv-* |
|---|---|---|---|---|---|
| `GET /api/v1/companies/x-tenant-id/{tenantId}` | **sim** (middleware em todo request + use cases) | **sim** (middleware em todo request + register/oauth/knowledge/collector/me) | **sim** | **sim** (`resolveBaseUrl`, chamada com o argumento errado, ver 1.5) | não |
| `GET /api/v1/companies/{id}` | — | — | — | **sim** (canais de MFA no login) | não |
| `GET /actuator/health/liveness` | — | sim (painel de conexões) | — | — | — |
| `GET /actuator/prometheus` | Prometheus | | | | |
| Todos os demais (CRUD company, approve/reject/activate/deactivate/suspend/block, `code/`, `cnpj/`, `search`, `mfa-channels`, addresses, bank-accounts, contacts, representatives, cnaes, policies, `/api/v1/health`, `/api/v1/helper/*`) | — | — | — | — | **ninguém** |

**Endpoints que ninguém chama** (lista em `MSC/src/main/java/com/keepguard/ms_company/adapters/in/rest/`):
- `CompanyController.java`: POST `/` (:40), PUT `/{id}` (:56), PATCH `/{id}/approve|reject|activate|deactivate|suspend|block` (:74–149), GET `/code/{codeCompany}` (:194), GET `/cnpj/{cnpj}` (:209), GET `/search` (:224), PUT `/{id}/mfa-channels` (:260), DELETE `/{id}` (:276).
- `address/AddressController.java` (`/api/v1/addresses`, 11 rotas), `bankaccount/BankAccountController.java` (`/api/v1/bank-accounts`, 11), `contact/ContactController.java` (`/api/v1/contacts`, 10), `representative/RepresentativeController.java` (`/api/v1/representatives`, 12), `cnae/CnaeController.java` (`/api/v1/companies/{companyId}/cnaes`, 10), `companypolicy/CompanyPolicyController.java` (`/api/v1/companies/{companyId}/policies`, 5), `health/HealthController.java`, `helper/HelperController.java`.
- A tela "Conexões" do bff-core só lista o ms-company (`KC/backend/bff/bff-core/internal/application/connections/catalog.go:37`). O front não chama o ms-company: os únicos hits de `compan*` em `KC/frontend` são telas de billing/LLM. O Ingress também não roteia nada para ele (`KC/helm/infra/templates/ingress.yaml:47-93`, só `bff-auth-service`/`bff-core-service`).
- Hoje a criação/edição de company em prod só acontece por SQL direto (ex.: `MSC/scripts/seed_company_mfa_email_only.sql:6-19`) ou por chamada manual.

### 1.2 bff-auth (Go)
- Cliente: `KC/backend/bff/bff-auth/internal/adapters/out/http/client/company_client.go:47-105`.
  - `GET {base}/api/v1/companies/x-tenant-id/{tenantId}` (:48). Headers `X-Correlation-ID` e `Content-Type: application/json` (:52-53). Não envia Authorization.
  - resty com timeout de 30s e 3 retries de transporte (:29-31).
  - Status diferente de 200 vira `HTTPError`. O cliente tenta parsear ProblemDetail (`detail`/`title`/`properties.errorCode`, :64-72) ou `{error,message}` (:74-83). 404 vira `COMPANY_NOT_FOUND` / "Empresa não encontrada" (:107-127).
  - Atenção: ele procura `errorCode` dentro de `properties`, mas o Spring serializa as properties do ProblemDetail no nível raiz. Na prática esse caminho nunca é usado e o código cai no mapeamento por status.
- DTO lido: `KC/backend/bff/bff-auth/internal/application/dto/wire.go:206-213` declara `id, tenantId, name, legalName, cnpj, status`. **Só `ID` é usado de fato** (`company_resolve_middleware.go:35,39`, `login_usecase.go:36,41`, `reset_password_usecase.go:44`, `send_reset_password_message_usecase.go:49`, `lifecycle/port.go:61,65`).
- Middleware em todo request, exceto `/health` e `/swagger`: `KC/backend/bff/bff-auth/internal/adapters/in/http/middleware/company_resolve_middleware.go:14-44`. O tenant vem do JWT, com o header `X-Tenant-Id` como fallback (:46-59). Erro do client é propagado. `ID==""` vira 404.
- Cadeia de decorators (`KC/backend/bff/bff-auth/cmd/bff-auth/main.go:196-221`): metrics → **Redis cache** → retry (2 tentativas, 50ms) → circuit breaker → logging.
- Redis: `KC/backend/bff/bff-auth/internal/adapters/out/http/decorator/company/redis_cache_decorator.go:83-108`. Lê `company_cache:tenantId:<lower(tenant)>` e depois `company_cache:simple:tenantId:<lower(tenant)>`, faz unmarshal e só aceita se tiver `id`. **Não grava** no cache.
- Retry só em 429/408/503/504/502 (`.../decorator/company/retry_decorator.go:52-64`).
- Config: `services.company.base_url`, default `http://localhost:8083` (`internal/infrastructure/config/config.go:187-189`). Em prod a env é `BFF_AUTH_SERVICES_COMPANY_BASE_URL=http://ms-company:8083` (`helm/templates/deployment.yaml:34-35`, confirmado ao vivo). Redis `redis:6379` db 0 (`application-prod.yml:52-56`).

### 1.3 bff-core (Go)
- Cliente: `KC/backend/bff/bff-core/internal/adapters/outbound/http/client/company_client.go:52-75`. Mesma URL e mesmos headers (:53-58). Status diferente de 200 passa por `MapHTTPError` (`error_mapper.go:24-44`), que procura `message`/`error`. Como o ms-company devolve ProblemDetail (`detail`/`title`), a mensagem cai no default por status ("Recurso não encontrado" etc., :86-114).
- DTO: `KC/backend/bff/bff-core/internal/application/dto/wire.go:191-202` declara `id, codeCompany, name, legalName, cnpj, status, mfaChannels[{id,channel,required,enabled}], createdAt, updatedAt, tenantId`. `createdAt`/`updatedAt` usam `CustomTime` (`wire.go:13-35`), que aceita ISO com ou sem timezone. **Se a data vier em formato não-ISO, o unmarshal inteiro falha.**
- Campos efetivamente lidos:
  - `ID`: middleware (`internal/adapters/inbound/http/middleware/company_resolve_middleware.go:38,42`), `scope/company.go:30,33`, `user/get_me.go:28,37`, register_init/resend/confirm.
  - `Name`: `register_init_usecase.go:78`, `register_resend_usecase.go:69`, `register_confirm_usecase.go:310` (vira o `appName` nos templates de e-mail).
  - `MfaChannels` (`channel`, `enabled`, `required`): `register_init_usecase.go:88-101`. Só canais com enabled **e** required entram; lista vazia significa EMAIL.
  - `CodeCompany`: `register_confirm_usecase.go:227`, enviado ao ms-auth como `company_code` na criação do AuthUser.
- Middleware: ignora `/health`, `/swagger` e `/billing/webhooks/` (`company_resolve_middleware.go:20-26`).
- Decorators (`KC/backend/bff/bff-core/cmd/bff-core/main.go:199-220`): metrics → Redis → retry (2) → CB → logging.
- Redis: `.../outbound/http/decorator/company/redis_cache_decorator.go:84-108`. Lê **só** `company_cache:tenantId:<lower>` (:86-88) e também não grava. Retry em 429/408/500/502/503 (`retry_decorator.go:53-65`).
- Config: `BFF_CORE_SERVICES_COMPANY_BASE_URL` (`helm/templates/deployment.yaml:34-35`). Health check em `ms-company:8083/actuator/health/liveness` (`application-prod.yml:156`).

### 1.4 bff-invest (investbot, Go)
- `investbot/backend/bff/bff-invest/internal/adapters/out/http/company/client.go:32-67`. Faz `GET .../x-tenant-id/{tenantID}` (:40) com `X-Correlation-ID` opcional (:44-46). Timeout de 10s (:22), sem retry e sem cache.
- Lê **só** `id` (DTO `id, tenantId`, :27-30). Status diferente de 200 vira erro genérico `ms-company status N` (:56-58).
- Config: `COMPANY_BASE_URL`/`MS_COMPANY_URL` (`internal/infrastructure/config/config.go:204`), `http://ms-company:8083` (`helm/values.yaml:21`, confirmado ao vivo).

### 1.5 ms-auth (Java, Feign)
- `KC/backend/ms/ms-auth/src/main/java/com/keepguard/ms_auth/adapters/out/feign/CompanyClient.java:9-21`. Usa `COMPANY_SERVICE_URL` (ao vivo: `http://ms-company:8083`) e devolve `Map<String,Object>`, então é tolerante a campos novos. Não envia header nenhum: `UserClientConfig.java` não tem interceptor.
  - `GET /api/v1/companies/{id}` em `AuthCommandService.java:294`, chamado no login (:179). Lê `mfaChannels[].enabled` e `mfaChannels[].channel` (case-insensitive) (:296-311). **Não olha `required`.** Em qualquer erro, cai para EMAIL (:314-317).
  - `GET .../x-tenant-id/{x}` em `DeviceSessionCommandService.java:364` (chamado em :259 e :322). Passa **companyId como se fosse tenantId** e procura `apiBaseUrl`/`frontendBaseUrl`, campos que o ms-company **não devolve**. Resultado: sempre cai no default (:366-378). Na prática é código morto, mas a chamada HTTP acontece (e provavelmente devolve 404).
  - `CompanyResolverAdapter.java:20-40` implementa `CompanyResolverPort`, mas **ninguém injeta essa porta**. O grep só acha a interface e o adapter, e `DeviceSessionCommandService.java:1041` tem um `resolveCompanyId` privado próprio. É código morto.

### 1.6 Quem NÃO consome
- ms-user: só tem referência lógica ao `companyId`, sem client (`KC/backend/ms/ms-user/docs/architecture/system-design.md:35`, `CompanyProfile.java:16`).
- ms-billing, ms-knowledge (só o nome no `ServiceCatalog.java:34,50`), ms-communication, srv-*, achadinhos, front: nenhuma chamada.

### 1.7 Comportamento do servidor que os consumidores dependem (preservar no Go)
- `GET x-tenant-id/{tenantId}` (`CompanyQueryService.java:176-220`):
  - procura primeiro no Redis (`company_cache:tenantId:`); se não achar, vai ao banco;
  - **company não-ACTIVE devolve 404** (`COMPANY_NOT_ACTIVE`, :195-199);
  - grava no cache com TTL de 30 dias (`CompanyCacheService.java:145-153`, TTL em `application.yml:56-60`).
  - Pegadinha: o objeto que já está em cache é devolvido sem rechecar o status. Uma company suspensa por SQL direto continua "ativa" para os BFFs por até 30 dias.
- `GET /{id}` (:33-70): cache em `company_cache:id:`, **sem** checagem de status.
- Formato do corpo 200: `CompanyResponseDTO` (`MSC/.../company/dto/response/CompanyResponseDTO.java:25-43`), com `id, codeCompany, tenantId, name, legalName, cnpj, stateRegistration, municipalRegistration, address, contacts, representatives, bankAccount, taxRegime, cnaes, mfaChannels[{id,channel,required,enabled}], ein, status, createdAt, updatedAt`.
  - Enums saem como nome: `ACTIVE`, `EMAIL`, `SIMPLES_NACIONAL`… (sem `@JsonValue`).
  - `LocalDateTime` sai como ISO sem timezone.
  - `INDENT_OUTPUT: true` (`application.yml:48-50`).
- Erros: `ProblemDetail` (`type,title,status,detail,instance` + `timestamp,path,errorCode` no nível raiz) (`GlobalExceptionHandler.java:114-131`). 404 sai com `errorCode=ENTITY_NOT_FOUND`: o código específico `COMPANY_NOT_FOUND`/`COMPANY_NOT_ACTIVE` **não é exposto**.
- UUID inválido no path cai no handler genérico e vira **500** (`GlobalExceptionHandler.java:412-441`; não há handler para `MethodArgumentTypeMismatchException`). O bff-core reexecuta 500 (retry). No Go, vale devolver 400/404, porque os consumidores tratam qualquer status diferente de 200 como erro.
- Formato do JSON gravado no Redis: `CompanyViewDTO` (`application/dto/company/CompanyViewDTO.java:15-35`). Os nomes de campo são os mesmos do response DTO, mas os subobjetos são `*ViewDTO`. A versão simples é `CompanySimpleViewDTO.java:9-23`.
- Chaves Redis gravadas (`CompanyCacheService.java:286-327`): `company_cache:{id|cnpj|code|tenantId|simple:id|simple:tenantId}:<valor>`. Também existem `address_cache`, `bank_account_cache`, `contact_cache` e `representative_cache` (`application.yml:65-70`). Invalidação nas escritas: `clearAllCompanyCache` (:249-267).
- Header de correlação: o servidor lê `X-Correlation-ID` e o devolve na resposta (`infrastructure/filter/CorrelationIdFilter.java:46,61`), e lê `X-Tenant-Id` como "applicationName" (:55).
- **Não há autenticação** no ms-company: `pom.xml` não tem lib-security e não existe SecurityConfig. `JWT_SECRET` é injetado mas não é usado para proteger rotas.

---

## 2. ms-company → ms-auth (provisionamento de roles)

**Lado cliente** (ms-company):
- `MSC/src/main/java/com/keepguard/ms_company/adapters/out/feign/AuthProvisionClient.java:9-18`: `POST {AUTH_SERVICE_URL:http://localhost:8081}/api/v1/companies/{companyId}/roles/provision`, sem body e sem headers. Logger BASIC (`AuthClientConfig.java:10-13`).
- Chamado em `CompanyCommandService.java:65`, depois do `save` (:63). O `@Transactional` em método `private` (:74-77) não tem efeito. Se a chamada falhar, **a company fica gravada e a API devolve 500**.
- **Em prod a env `AUTH_SERVICE_URL` não existe no Deployment ao vivo**, então o Feign usa `localhost:8081` e o provisionamento falharia. O Helm do repo define a env (`MSC/helm/templates/deployment.yaml:75-76`), mas esse Helm não é o que está aplicado. Hoje ninguém chama POST `/companies`, então o problema está latente.

**Lado servidor** (ms-auth):
- `KC/backend/ms/ms-auth/src/main/java/com/keepguard/ms_auth/adapters/in/rest/role/CompanyRoleProvisionController.java:22-39`: `POST /api/v1/companies/{companyId}/roles/provision`, com `companyId` UUID no path e sem body.
- Resposta: **201** quando cria e **200** quando já estava provisionado (:37). Corpo `ProvisionCompanyRolesViewDTO {companyId: UUID, alreadyProvisioned: bool, roleNames: string[]}` (`application/dto/role/ProvisionCompanyRolesViewDTO.java:6-10`).
- Lógica (`application/service/role/CompanyRoleProvisionService.java:39-100`), `@Transactional`:
  - é idempotente via `existsByCompanyId` (:53-59);
  - clona os templates globais `SystemRoleNames.PROVISIONED` (ROLE_ADMIN/MANAGER/USER) com as authorities default, grava em `roles` + `companies_roles` (`CompanyRoleJpaEntity.java:13`), e ROLE_USER fica como default (:86-92);
  - template ou authority ausente devolve 404 (NotFoundException, :65-71);
  - publica auditoria `CREATE COMPANY_ROLE` (:41-47).
- Segurança: a rota cai em `.requestMatchers("/api/v1/**").permitAll()` (`infrastructure/config/security/SecurityConfig.java:67`), então **não exige JWT**.

---

## 3. Infra em produção

### 3.1 Deployment / Service (ao vivo, `kubectl get` em 2026-10-03)
| Item | Valor ao vivo | Repo (`MSC/helm`) |
|---|---|---|
| Deployment / container | `ms-company` / `ms-company`, 1 réplica, label e selector `app=ms-company` | igual (`helm/templates/deployment.yaml:4-22`) |
| Imagem | `ghcr.io/keepguard/ms-company:1.0.20-1.0.0` | `values.yaml:2-3` tag `1.0.9-1.0.0` |
| Service | `ms-company` ClusterIP, 8083/TCP, selector `app=ms-company` | `helm/templates/service.yaml:1-12` |
| Porta do container | 8083 | 8083 |
| Resources | requests 384Mi / limits 1Gi, sem CPU | `values.yaml:9-13` 256Mi/512Mi |
| JVM | sem `JAVA_OPTS` no pod, então vale o do Dockerfile: `-Xms512m -Xmx1024m` (`MSC/Dockerfile:29`). **Heap máximo igual ao limit do pod (1Gi), risco de OOMKill** | idem |
| Probes | **nenhuma** (liveness/readiness/startup vazias) | liveness `/actuator/health/liveness`, readiness `/actuator/health/readiness` (`deployment.yaml:81-96`) |
| Helm release | nenhuma (annotation `meta.helm.sh/release-name` vazia): o manifest foi aplicado à mão | — |
| Pod atual | 23d, 3 restarts (último em 2026-09-11, exitCode 255, reason Unknown) | — |
| Uso | 3m CPU, **772Mi** RAM | — |

- Deploy (`MSC/script-deploy-github-ms-company.sh:118-163`): `mvn clean package` local, `docker build --platform linux/amd64`, push de `ghcr.io/keepguard/ms-company:{SHA,latest,main-latest}` e por fim `kubectl set image deployment/ms-company ms-company=<SHA>`. `MSC/script-deploy-k8s-prod.sh:29-81` faz o mesmo. **No Go, manter o nome do Deployment e o nome do container `ms-company`**, porque o script depende dos dois.
- `KC/scripts/deploy-all-prod.sh:68` inclui o ms-company.
- Nenhum script do repo roda `helm install/upgrade`.

### 3.2 Env vars e secrets ao vivo (só nomes)
- Literais:
  - `SPRING_CLOUD_COMPATIBILITY_VERIFIER_ENABLED`, `SERVER_PORT=8083`
  - `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/keepguard_api_db`
  - `SPRING_DATA_REDIS_HOST=redis`, `SPRING_DATA_REDIS_PORT=6379`
  - `KEEPGUARD_VALIDATION_MODERATION_ENABLED=false`
  - `SPRING_RABBITMQ_HOST/PORT`, `RABBITMQ_HOST/PORT` (`rabbitmq-service:5672`)
  - `KEEPGUARD_AUDIT_EXCHANGE=srv-audit-exchange-prod`
  - `SPRING_RABBITMQ_USERNAME/PASSWORD`: **credencial em texto literal no spec, não vem de secret**.
- ConfigMap `keepguard-config`: `SPRING_PROFILES_ACTIVE` (valor `local`), `POSTGRES_USER`.
- Secret `keepguard-secret`: `JWT_SECRET`, `POSTGRES_PASSWORD`.
- **Ausentes ao vivo:** `AUTH_SERVICE_URL`, `USER_SERVICE_URL` e probes.

### 3.3 Profile e configuração efetiva
- O profile `local` está ativo em prod, então valem `application.yml` + `application-local.yml`, com env sobrescrevendo.
- Efeitos:
  - `ddl-auto: update` (`application-local.yml:27-29`);
  - Redis standalone (o cluster de 6 nós de `application-prod.yml:19-21` **não é usado**);
  - porta 8583 do local, sobrescrita por `SERVER_PORT=8083`.
- `application-prod.yml` nunca é carregado. Ele tem `ddl-auto: validate` (:30-32) e um bloco `logging:` com indentação inválida (:40-43), que provavelmente nem parseia.
- Hikari: pool de 10 (`application.yml:19-25`). Virtual threads ligadas (`application.yml:4-6`).

### 3.4 Banco
- Postgres único `postgres:5432`, database **`keepguard_api_db` compartilhado** por todos os ms Java. Cada um tem seu schema (`ms_auth, ms_user, ms_company, ms_communication, ms_user_consents, ms_knowledge, ms_ai_guardian, srv_audit, srv_data_collector`, conforme `KC/scripts/sync-apps-data-to-prod.sh:35-45`).
- O ms-company usa o schema `ms_company` (`hibernate.default_schema`, `application.yml:47`), com o mesmo usuário de banco dos outros serviços (configmap `POSTGRES_USER`).
- Tabelas, segundo as entidades JPA em `MSC/src/main/java/com/keepguard/ms_company/infrastructure/persistence/entity/`: `companies`, `company_addresses`, `company_bank_accounts`, `company_cnaes`, `company_contacts`, `company_policies`, `company_mfa_channels` (colunas `is_required`, `is_enabled`, conforme `scripts/seed_company_mfa_email_only.sql:9-11`) e `company_representatives`.
- **Quem cria o schema e as tabelas:**
  - Hibernate `ddl-auto: update`, porque o profile `local` está ativo.
  - Não há Flyway/Liquibase no `pom.xml` e não existe `CREATE TABLE`/`CREATE SCHEMA` de company no repo. O único `CREATE TABLE ... company_*` é do ms-billing (`GatewaySchemaAligner.java:66`).
  - Os dados (e o DDL, via dump) foram copiados do Docker local por `sync-apps-data-to-prod.sh`: `pg_dump -n <schema>` (:400-413), seguido de `DROP SCHEMA ... CASCADE` + `pg_restore` (:468-484).
  - O backup de prod confere com `SELECT COUNT(*) FROM ms_company.companies` (`KC/k8s/backup/postgres-backup.yaml:139`, `KC/helm/infra/templates/infra-postgres-backup.yaml:146`).
  - Consequência para o Go: é preciso escolher uma migration explícita (`CREATE TABLE IF NOT EXISTS`, como fazem os srv-*, por exemplo `KC/backend/srv/srv-audit/db/srv_audit_schema.sql:6`) gerada a partir do **schema real de prod**. Esse schema foi criado pelo Hibernate e pode ter sobras de colunas antigas. Não consegui listar as tabelas reais de prod (o acesso ao psql de prod foi negado pelo classificador); isso fica para o usuário rodar.

### 3.5 Redis
- Instância `redis:6379`, db 0, compartilhada. Os BFFs apontam para o mesmo host/db (`bff-core/application-prod.yml:83-87`, `bff-auth/application-prod.yml:52-56`, env `BFF_*_REDIS_HOST=redis` ao vivo).
- Pool lettuce de 8 e TTL de 30 dias.
- Keyspaces do ms-company: `company_cache:*`, `address_cache:*`, `bank_account_cache:*`, `contact_cache:*`, `representative_cache:*`.

### 3.6 RabbitMQ
- O único uso é a auditoria, via lib-common: `RabbitAuditConfiguration.java:17-44`, ativa por padrão (`keepguard.audit.enabled`, `application.yml:143-148`).
- As escritas com `@LogOperation(audit=true)` (o default em `LogOperation.java:51`) publicam por `LoggingService.logAudit` (`lib-common/.../LoggingService.java:190-222`). Exemplos: `CompanyCommandService.java:38-44`, `ContactCommandService.java:37-39`.
- O ms-company não tem consumer (`@RabbitListener`) nenhum.

### 3.7 Ingress / rotas
- Não é exposto. O IngressRoute Traefik só manda tráfego para `bff-auth-service` e `bff-core-service` (`KC/helm/infra/templates/ingress.yaml:47-93`).
- Acesso local é por túnel: `KC/scripts/tunnel-prod-services-all.sh:28` (porta local 18083).

### 3.8 Observabilidade
- Prometheus scrape: `ms-company:8083/actuator/prometheus`, com label `application=ms-company` (`KC/k8s/observability/prometheus-configmap.yaml:37-43`).
- O dashboard `KC/k8s/observability/dashboards/ms-company.json`, gerado por `generate-dashboards.py:310-311`, usa métricas Spring/JVM: `process_start_time_seconds`, `jvm_threads_live_threads`, `jvm_memory_used_bytes`, `http_server_requests_seconds_count{uri=~"/api/v1/companies.*"}` (:67, :128, :196, :485).
- O Go precisa servir `/actuator/prometheus` (ou o scrape precisa mudar) e emitir métricas compatíveis, ou então o dashboard precisa ser regenerado.
- Nenhuma regra de alerta referencia o ms-company (`alerting/rules.yaml`, `grafana-alerting.yaml`).
- O bff-core faz health check em `/actuator/health/liveness` (`bff-core/application-prod.yml:156`).

---

## 4. PROGRESS.md

- `PROGRESS.md` (raiz) e `keepguard-core/PROGRESS.md`: **nada sobre ms-company, migração Java→Go, consumo de memória da JVM ou libs.**
- O que tem relevância indireta:
  - `keepguard-core/docker/` e `keepguard-core/k8s/observability/` **não estão versionados em Git** (`PROGRESS.md:55-59`; `keepguard-core/PROGRESS.md:77, 84-88`). Ajuste de scrape ou dashboard para o novo ms-company só fica nesta máquina.
  - O Prometheus usa `static_configs` fixos, e as annotations `prometheus.io/scrape` do Helm são decorativas (`keepguard-core/PROGRESS.md:95-99`).
  - O ConfigMap canônico é `prometheus-configmap.yaml`. Editar `prometheus.yaml` já causou uma regressão (:89-94).
- O repo `ms-company` está limpo (`git status` vazio), na branch `develop`. Último commit: `485f610`.

---

## 5. Dimensionamento do programa: ms-* Java do keepguard-core

LOC por `find src/main -name '*.java' | xargs wc -l`. Todos são Spring Boot 3.5.3 (Java 25). Recursos e uso **ao vivo**.

| Serviço | LOC main (arquivos) | LOC test | Imagem ao vivo | requests / limits (mem) | JVM efetiva | Profile | RSS (`kubectl top`) |
|---|---|---|---|---|---|---|---|
| ms-auth | 23.466 (357) | 19.510 | `ms-auth:5db3de7` | 384Mi / 1Gi | Dockerfile `-Xms512m -Xmx1024m` | configmap (`local`) | 817Mi |
| ms-company | 15.714 (199) | 13.151 | `ms-company:1.0.20-1.0.0` | 384Mi / 1Gi | Dockerfile `-Xms512m -Xmx1024m` | `local` | 772Mi |
| ms-user | 15.104 (208) | 13.879 | `ms-user:d050363` | 384Mi / 1Gi | Dockerfile | configmap | 716Mi |
| ms-ai-guardian | 10.984 (207) | 922 | `ms-ai-guardian:159d6c5` | 512Mi / 1Gi, CPU 250m/1 | `MaxRAMPercentage=70` | prod | 601Mi |
| ms-knowledge | 10.548 (168) | 4.794 | `ms-knowledge:63a25a6` | 512Mi / 1Gi, CPU 250m/1 | `MaxRAMPercentage=70` | k8s | 464Mi |
| ms-communication | 10.009 (146) | 14.836 | `ms-communication:latest` | 384Mi / 1Gi | Dockerfile | configmap | 745Mi |
| ms-billing | 9.364 (210) | 2.395 | `ms-billing:1d6c2d2` | 384Mi / 1Gi | `-Xms256m -Xmx512m` | prod | 628Mi |
| ms-user-consents | 5.791 (78) | 2.949 | `ms-user-consents:f84fae9` | 384Mi / 1Gi | Dockerfile | configmap | 705Mi |
| **Total** | **≈101k** | **≈72k** | | | | | **≈5,45Gi** |
| libs: lib-common 2.798, lib-security 1.213, lib-validation 1.271 | | | | | | | |

- Referência Go no mesmo cluster: `ms-analyst-finance` 11Mi e `ms-achadinhos` 12Mi.
- O cluster tem nó único `srv1375392`, com 454m de CPU (11%) e **11.663Mi de memória (72%)**.
- Os valores dos Helm do repo divergem do que está aplicado em quase todos (ex.: ms-auth, ms-user, ms-communication, ms-user-consents e ms-company declaram 256Mi/512Mi no repo, contra 384Mi/1Gi ao vivo).
- `-Xmx1024m` com limit de 1Gi aparece em ms-auth/ms-company/ms-user/ms-communication/ms-user-consents. É risco de OOMKill e é um ganho rápido mesmo antes de migrar (passar para `MaxRAMPercentage`, como já fazem ms-ai-guardian e ms-knowledge).

---

## 6. Checklist de compatibilidade para o ms-company em Go (derivado do acima)

1. `GET /api/v1/companies/x-tenant-id/{tenantId}`: devolve 200 com o mesmo JSON (no mínimo `id, codeCompany, tenantId, name, legalName, cnpj, status, mfaChannels[{id,channel,required,enabled}], createdAt, updatedAt` em ISO). Devolve 404 quando não existe **ou** não está ACTIVE.
2. `GET /api/v1/companies/{id}`: 200 com o mesmo JSON (o ms-auth lê `mfaChannels[].channel/enabled`).
3. Redis: gravar `company_cache:tenantId:<lower(tenantId)>` (e `company_cache:id:<lower(id)>`) com JSON compatível, TTL de 30 dias, db 0 em `redis:6379`. Invalidar nas escritas. Considerar também gravar `simple:tenantId` ou deixar de lado, já que o bff-auth trata a ausência dessa chave como miss.
4. Preservar `POST .../roles/provision` no ms-auth com `AUTH_SERVICE_URL=http://ms-auth:8081`. O Deployment novo precisa dessa env.
5. Expor `/actuator/health/liveness`, `/actuator/health/readiness` e `/actuator/prometheus` na porta 8083, ou ajustar o bff-core (`application-prod.yml:156`), o scrape e o dashboard.
6. Manter Deployment/container/Service `ms-company`, label `app=ms-company` e porta 8083.
7. Schema: migration idempotente sobre `ms_company.*` no `keepguard_api_db`, validada contra o schema real de prod (que foi gerado por Hibernate).
8. (Opcional) Publicar auditoria em `srv-audit-exchange-prod` / `audit.event` nas escritas, se as escritas forem mantidas.
9. Escopo mínimo possível: as ~88 rotas sem consumidor podem ficar de fora da fase 1. Isso é decisão do usuário: hoje a gestão de company é feita por SQL direto.
