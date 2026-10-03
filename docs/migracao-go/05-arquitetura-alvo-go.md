# ms-company em Go — arquitetura-alvo

Data: 2026-10-03. Base normativa: skill `new-app-go` (`.claude/skills/new-app-go/SKILL.md`).
Este documento descreve **a forma** do serviço Go e da lib Go. As regras de negócio que ele
implementa estão em `01-ms-company-especificacao-atual.md`. Nada aqui cria regra nova.

Molde de código:
- forma de `in/http` (fatia vertical por contexto): `investbot/backend/ms/ms-analyst-finance`;
- use case com 1 arquivo por ação + implementação privada + `postgres/<ctx>/{model,mapper,repository}`:
  `investbot/backend/srv/srv-mt5-market-data`;
- **não** copiar do ms-analyst-finance: `UseCase` único com ~23 portas, `server.go` recebendo struct
  concreto, repositório sem sufixo (ver `04-padrao-go-referencia.md` §1).

---

## 1. Mapeamento Java → Go (camada por camada)

| Java (hoje) | Go (alvo) | Observação |
|---|---|---|
| `domain/entity/*` | `internal/domain/<agregado>/<entidade>.go` | Entidade com campos privados, construtor `New...` que valida e devolve `error`, reidratação `Restore...` sem validação. |
| `domain/enums/*` | `internal/domain/<agregado>/<enum>.go` | `type CompanyStatus string` + constantes + `Description()` + `Parse...`. Enum sem uso (`MfaPolicyEnum`) não migra. |
| `domain/exception/*` + exceções da lib-common | `internal/domain/<agregado>/<agregado>_errors.go` | Erro tipado (`*InvalidStatusError{Entity,Current,Expected}`) e sentinelas (`ErrInvalidStatusForOperation`). |
| `application/port/in/*Port` | `internal/application/port/in/<ctx>_command_port.go` + `<ctx>_query_port.go` | CQRS explícito: **duas** portas por contexto (ISP). |
| `application/service/*/*UseCaseService` | **não existe em Go** | Em Java só delega. Em Go o handler recebe as duas portas direto; a fachada some. |
| `application/service/*/*CommandService` | `internal/application/<ctx>/command/<ação>_usecase.go` | Um arquivo por ação. |
| `application/service/*/*QueryService` | `internal/application/<ctx>/query/<ação>_usecase.go` | Um arquivo por consulta. |
| `application/dto/*` (Command, Criteria, View) | `internal/application/dto/{command,query,view}/<ctx>_*.go` | Pacote neutro (evita ciclo `port/in → usecase → port/in`). |
| `application/service/exception/*` | `internal/application/dto/apperr/app_errors.go` | `ErrNotFound`, `ErrAlreadyExists` com mensagem; o handler mapeia para HTTP. |
| `application/mapper/*ApplicationMapper` | `internal/application/<ctx>/<ctx>_view_mapper.go` | Domínio → View e merge "null = manter" do update. |
| `application/port/out/{persistence,cache,metrics,auth}` | `internal/application/port/out/<tema>.go` | Um arquivo por tema + `transaction.go` (unit of work) + `audit_publisher.go`. |
| `adapters/in/rest/<ctx>/` | `internal/adapters/in/http/<ctx>/{dto,mapper,handler.go,<ctx>_*_handlers.go}` | Fatia vertical, um `Handler` por contexto recebendo só as portas que usa. |
| `infrastructure/rest/GlobalExceptionHandler` | `internal/adapters/in/http/httperr/problem_details.go` | ProblemDetail RFC 7807 com `timestamp`, `path`, `errorCode` na raiz (contrato lido pelo bff-auth). |
| `infrastructure/filter/CorrelationIdFilter` | middleware da lib Go (`correlation`) | Mesmo header `X-Correlation-ID`, gera UUID, ecoa na resposta. |
| `infrastructure/persistence/{entity,mapper,spring,*Adapter}` | `internal/adapters/out/postgres/<ctx>/{model,mapper,repository}/` | pgx/v5, SQL explícito. `pool.go`, `tx_manager.go`, `rowscanner.go` na raiz de `postgres/`. |
| `infrastructure/redis/*CacheService` | `internal/adapters/out/redis/<ctx>/<ctx>_cache.go` (+ `model/` com o JSON do cache) | Chaves e JSON idênticos ao Java (contrato com bff-auth/bff-core). |
| `adapters/out/feign/AuthProvisionClient` | `internal/adapters/out/http/msauth/{dto,auth_role_provision_client.go}` | Implementa `outport.RoleProvisioner`. |
| `@LogOperation(audit=true)` (AOP, invisível) | chamada explícita a `outport.AuditPublisher` no use case | **Não pode sumir.** Mesmo envelope `AuditEvent`, exchange e routing key. |
| `@MetricsEndpoint` (AOP) | middleware Prometheus (lib Go) com rótulo `endpoint` por rota | Mantém `api_requests_total` / `api_requests_latency_seconds`. |
| `MetricsPort` (`company_*_total`) | `outport.BusinessMetrics` + adapter Prometheus | Ver §6 sobre as tags de alta cardinalidade. |
| `infrastructure/config/*`, `application*.yml` | `internal/infrastructure/config/config.go` (viper) + `application{,-dev,-local,-prod}.yml` | Padrão do monorepo. |
| `Resilience4j` (`redisCache`, `databaseOperation`) | Redis fail-open (erro vira miss), retry só no cliente HTTP do ms-auth | Bulkhead/retry de DB do Java tende a não estar ativo (`01` §8.5); não replicar retry dentro de transação. |
| `HelperController`, `HealthController` | `/health`, `/ready` + aliases `/actuator/health/liveness`, `/actuator/health/readiness` inline no `server.go` | bff-core consulta `/actuator/health/liveness`. |

---

## 2. Árvore-alvo

```
ms-company/
├── cmd/ms-company/main.go                    # composition root: monta tudo, único entrypoint
├── db/migrations/
│   ├── embed.go
│   └── 001_ms_company_baseline.up.sql        # extraído de prod (pg_dump -s), idempotente
├── deploy/{Dockerfile, healthcheck/main.go}
├── docs/architecture/system-design.md
├── helm/
├── application{,-dev,-local,-prod}.yml
├── script-deploy-github-ms-company.sh
└── internal/
    ├── domain/
    │   ├── company/
    │   │   ├── company.go                    # agregado raiz: transições, validateStatusForOperations, aprovação
    │   │   ├── company_status.go             # ACTIVE, INACTIVE, PENDING_APPROVAL, SUSPENDED, BLOCKED
    │   │   ├── tax_regime.go
    │   │   ├── mfa_channel.go                # entidade CompanyMfaChannel + enum de canal
    │   │   ├── approval_requirements.go      # o que precisa estar ativo para aprovar (snapshot dos filhos)
    │   │   └── company_errors.go
    │   ├── address/          address.go
    │   ├── bankaccount/      bank_account.go, account_type.go
    │   ├── cnae/             cnae.go, cnae_errors.go
    │   ├── contact/          contact.go
    │   ├── representative/   representative.go
    │   └── policy/           company_policy.go, policy_status.go
    │
    ├── application/
    │   ├── port/
    │   │   ├── in/                           # package inport
    │   │   │   ├── company_command_port.go   company_query_port.go
    │   │   │   ├── address_command_port.go   address_query_port.go
    │   │   │   ├── bank_account_command_port.go  bank_account_query_port.go
    │   │   │   ├── cnae_command_port.go      cnae_query_port.go
    │   │   │   ├── contact_command_port.go   contact_query_port.go
    │   │   │   ├── representative_command_port.go  representative_query_port.go
    │   │   │   └── policy_command_port.go    policy_query_port.go
    │   │   └── out/                          # package outport
    │   │       ├── company_repository.go     address_repository.go  ...  policy_repository.go
    │   │       ├── company_cache.go          child_list_cache.go
    │   │       ├── role_provisioner.go       # ms-auth
    │   │       ├── audit_publisher.go
    │   │       ├── business_metrics.go
    │   │       └── transaction.go            # TxManager.WithinTx(ctx, fn)
    │   ├── dto/
    │   │   ├── command/   company_commands.go, address_commands.go, ...
    │   │   ├── query/     company_queries.go (SearchCompaniesQuery, critérios), page_query.go
    │   │   ├── view/      company_view.go, page_view.go, ...
    │   │   └── apperr/    app_errors.go
    │   ├── company/
    │   │   ├── command/
    │   │   │   ├── create_company_usecase.go
    │   │   │   ├── update_company_usecase.go
    │   │   │   ├── change_company_status_usecase.go   # approve/reject/activate/deactivate/suspend/block
    │   │   │   ├── update_mfa_channels_usecase.go
    │   │   │   └── delete_company_usecase.go
    │   │   ├── query/
    │   │   │   ├── get_company_usecase.go             # por id, code, cnpj, tenant
    │   │   │   └── search_companies_usecase.go
    │   │   └── company_view_mapper.go
    │   ├── address/{command,query}/ ...   (idem para bankaccount, cnae, contact, representative, policy)
    │
    ├── adapters/
    │   ├── in/http/
    │   │   ├── server.go                     # rotas, middlewares, health inline, um Handler por contexto
    │   │   ├── company/
    │   │   │   ├── dto/  company_request_dto.go, company_response_dto.go
    │   │   │   ├── mapper/ company_mapper.go           # request → command, view → response
    │   │   │   ├── handler.go                          # Handler{commands inport.CompanyCommands; queries inport.CompanyQueries}
    │   │   │   ├── company_command_handlers.go
    │   │   │   └── company_query_handlers.go
    │   │   ├── address/  bankaccount/  cnae/  contact/  representative/  policy/   (mesma forma)
    │   │   ├── httperr/  problem_details.go, error_mapper.go, request_parsing.go
    │   │   └── middleware/ endpoint_label_middleware.go   # rótulo de métrica por rota
    │   └── out/
    │       ├── postgres/
    │       │   ├── pool.go  tx_manager.go  rowscanner.go  migrations.go
    │       │   ├── company/        {model,mapper,repository}/   # companies + company_mfa_channels
    │       │   ├── address/        {model,mapper,repository}/
    │       │   ├── bankaccount/    cnae/  contact/  representative/  policy/
    │       ├── redis/
    │       │   ├── company/        {model/company_cache_model.go, company_cache.go}
    │       │   └── childlist/      child_list_cache.go          # address/bank/contact/representative
    │       ├── http/msauth/        {dto/, auth_role_provision_client.go}
    │       ├── messaging/audit/    audit_publisher.go           # fino: delega à lib Go
    │       └── metrics/            business_metrics.go          # implementa outport.BusinessMetrics
    │
    └── infrastructure/ config/  logger/  metrics/
```

### Por que `request` e `response` são arquivos, não subpastas
A skill fixa `<ctx>/dto/` como um pacote. Separar `dto/request/` e `dto/response/` cria dois pacotes
por contexto sem ganho de isolamento (os dois são dado puro, sem import de domínio). O nome do arquivo
já entrega o conteúdo (`company_request_dto.go`). Se você preferir subpastas, é uma mudança na skill,
não só aqui.

### Por que `command/` e `query/` são subpastas
Aqui há ganho real: os use cases de leitura dependem de cache e não de transação/auditoria; os de
escrita dependem de `TxManager`, `AuditPublisher` e invalidação de cache. Pacotes separados tornam
isso visível no import. Não existe hoje nenhum serviço Go com CQRS explícito — este será o primeiro,
e o padrão precisa entrar na skill `new-app-go` junto com a implementação.

---

## 3. Regras de dependência (as mesmas da skill, aplicadas aqui)

```
in/http/<ctx>  →  port/in  →  application/<ctx>/{command,query}  →  port/out  ←  out/<tecnologia>
                                        ↓
                                     domain/
```

- `domain/` não importa nada de fora (nem a lib Go, exceto `brdoc`, que é função pura — decisão D3).
- Handler, dto e mapper de `in/` nunca importam `out/`. O único lugar que conhece os dois é `main.go`.
- Handler recebe interface de `port/in`; use case recebe interfaces de `port/out`; construtores
  `New...` devolvem interface.
- Struct com tag (`json`, `db`) só existe em adapter. O que a aplicação enxerga é `view.*` / domínio.

---

## 4. Pontos de desenho que não têm molde no monorepo

### 4.1 Agregado com filhos (Company → endereços, contas, CNAEs, contatos, representantes, MFA)
Java carrega a Company com 6 coleções lazy, mas **só usa os filhos para a regra de aprovação**; a
resposta HTTP sai com os filhos `null` (só `mfaChannels` vem preenchido).

Go: o repositório de Company carrega `companies` + `company_mfa_channels` (2 queries). A aprovação
recebe um `ApprovalRequirements` (tem endereço ativo? conta ativa? CNAE principal ativo? contato
ativo? representante ativo?) montado por **uma** query de `EXISTS` no use case de aprovação. A regra
continua no domínio (`company.Approve(reqs)`), na mesma ordem e com as mesmas mensagens. Resultado
igual, sem carregar 6 coleções.

### 4.2 Transação (unit of work)
`outport.TxManager.WithinTx(ctx, func(ctx) error)`; o adapter guarda o `pgx.Tx` no `context` e os
repositórios usam a tx se houver, o pool se não houver. Usado onde o Java tem `@Transactional` real:
criação de endereço/conta (desativa o anterior + insere), CNAE principal, política ativa única.

### 4.3 Criação de company + provisionamento no ms-auth
Hoje: grava a company, depois chama o ms-auth; se o ms-auth falhar, a company fica gravada e a API
responde 500 (sem rollback). Opções na decisão D5.

### 4.4 Auditoria
Hoje é automática (AOP) em todo método de escrita, com `outcome` SUCCESS/FAILURE. Em Go: o use case
de escrita publica explicitamente via `outport.AuditPublisher` (assíncrono, buffer, descarta se cheio —
mesmo comportamento do Java). Para não repetir 40 vezes o mesmo bloco, um *decorator* por porta de
comando (`audited_company_commands.go`) envolve a implementação e publica SUCCESS/FAILURE — é o
equivalente explícito do aspect, montado no `main.go`.

### 4.5 Contrato Redis com os BFFs
bff-auth e bff-core leem `company_cache:tenantId:<lower>` (e o bff-auth também
`company_cache:simple:tenantId:<lower>`) direto do Redis. O Go grava exatamente essas chaves, com o
JSON do `CompanyViewDTO` (mesmos nomes de campo, datas ISO sem fuso), TTL 30 dias, db 0. Teste de
contrato: um JSON gravado pelo Go tem que ser desserializado pelos structs dos dois BFFs.

---

## 5. Lib Go compartilhada (fase 1)

### 5.1 O que as 3 libs Java viram

| Lib Java | Usada por | Em Go |
|---|---|---|
| lib-common | 7 ms (ms-company incluso) | ~70% é Spring/AOP e desaparece. Portam: validadores BR (`brdoc`), envelope e publisher de auditoria (`audit`), middleware de correlation-id, middleware de métricas HTTP, tipos de erro + ProblemDetail, enums de comunicação (só quando ms-communication migrar). |
| lib-security | ms-billing, ms-knowledge, ms-user (**ms-company não usa**) | Vira pacote `auth` (validação JWT HS256 + middleware + claims no `context`). Hoje há 4 implementações Go divergentes nos BFFs; consolidar resolve isso também. |
| lib-validation | só ms-user (5 campos com moderação) | Não vira lib. Vai para dentro do ms-user quando ele migrar (corrigindo cache sem TTL e log de conteúdo do usuário). |

### 5.2 Forma: um módulo, vários pacotes
As 3 libs Java existem separadas por causa do classpath/autoconfig do Spring. Em Go, um pacote não
usado não entra no binário, então **um módulo com pacotes independentes** dá o mesmo isolamento com
um terço da manutenção (uma versão, um repo, um pipeline):

```
lib-go-common/          (módulo github.com/keepguard/lib-go-common)
├── brdoc/          cnpj.go, cpf.go, cep.go, phone.go, state.go, bank_code.go, cnae.go, email.go
├── apperr/         problem_details.go           # RFC 7807 com timestamp/path/errorCode na raiz
├── correlation/    correlation_middleware.go, correlation_context.go
├── httpmetrics/    http_metrics_middleware.go   # api_requests_total, api_requests_latency_seconds
├── audit/          audit_event.go, rabbit_audit_publisher.go, noop_audit_publisher.go
└── auth/           jwt_parser.go, jwt_middleware.go, claims_context.go   (fase 1b)
```

Regras: sem `echo` no núcleo dos pacotes (middleware em subpacote `echoadapter/` se precisar),
mensagens de erro idênticas às do Java, tabela de testes portada de `BrazilianValidationUtilsTest`.

### 5.3 Distribuição — o ponto que muda o build
Cada serviço é um repositório Git próprio e o build sai de `git archive` do commit do serviço.
Um `replace ../lib-go-common` **quebra** esse build. Opções na decisão D2.

---

## 6. Compatibilidade que o Go precisa manter (checklist de corte)

1. `GET /api/v1/companies/x-tenant-id/{tenantId}` e `GET /api/v1/companies/{id}`: mesmo JSON, 404 para
   não-ACTIVE no lookup por tenant.
2. Redis: chaves, JSON e TTL do §4.5.
3. Erros em ProblemDetail (`application/problem+json`).
4. Deployment, container, Service e label `ms-company`, porta 8083.
5. `/actuator/health/liveness` e `/readiness` respondendo (bff-core e probes).
6. Métricas: `/actuator/prometheus` ou ajuste do scrape; o dashboard atual usa métricas de JVM e
   precisa ser regenerado de qualquer forma.
7. Auditoria no mesmo exchange (`srv-audit-exchange-prod`, `audit.event`, `sourceService=ms-company`).
8. Schema `ms_company` no `keepguard_api_db` — a migration baseline sai do `pg_dump -s` de prod.
9. Env `AUTH_SERVICE_URL=http://ms-auth:8081` no Deployment (hoje ausente em prod).
