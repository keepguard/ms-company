# Levantamento dos padrões Go do monorepo — molde para migrar ms-company (Java → Go)

Data: 2026-10-03. Tudo abaixo cita arquivo real. Paths relativos à raiz
`/Users/rafaelnogueirasoares/Projetos/keepguard/`.

Fontes normativas lidas:
- `.claude/skills/new-app-go/SKILL.md` (normativo, mais novo — ganha em conflito)
- `keepguard-core/backend/bff/bff-auth/docs/architecture/go-standard.md` (histórico; partes
  desatualizadas: ainda fala em `<entidade>_document.go` e "fatia vertical a partir de 3",
  a skill corrigiu para `_model.go` e "2+ contextos")

Inventário: **16 módulos Go** (a skill ainda diz 13 — entraram `srv-mt5-analytics`,
`srv-mt5-market-data`, `srv-quote-simulator`). 14 em `go 1.27.1`; **`bff-core` e
`srv-llm-gateway` ainda em `go 1.24`** (desvio da regra §8).

---

## 1. ms-analyst-finance — árvore real e camadas

`investbot/backend/ms/ms-analyst-finance/internal/` (até 4 níveis):

```
internal/
├── adapters/
│   ├── in/
│   │   ├── http/
│   │   │   ├── server.go
│   │   │   ├── analysis/   {dto/, mapper/, handler.go, analysis_handlers.go}
│   │   │   ├── catalog/    {dto/, handler.go, catalog_handlers.go}
│   │   │   ├── digest/     {handler.go}
│   │   │   ├── market/     {handler.go, market_handlers.go, proactive_job.go}
│   │   │   ├── mt5account/ {dto/, handler.go}
│   │   │   ├── portfolio/  {dto/, mapper/, handler.go}
│   │   │   ├── watchlist/  {dto/, mapper/, handler.go, watchlist_handlers.go}
│   │   │   └── httperr/    errors.go          (ParseCompany, MapError)
│   │   └── scheduler/      cron.go            (robfig/cron)
│   └── out/
│       ├── http/
│       │   ├── authclient/ {dto/, oauth_client.go}      ← cliente do ms-auth
│       │   ├── knowledge/  {dto/, mapper/, knowledge_client.go}
│       │   └── llm/        {dto/, llm_client.go, llm_guard.go}
│       ├── messaging/audit/ audit_publisher.go          (amqp091)
│       ├── mongo/runstore/  ~20 arquivos (run_document.go, run_mapper.go, analysis_run_repository.go, watchlist.go, ...)
│       └── redis/           catalog_cache.go
├── application/
│   ├── analyst/   compare_assets_usecase.go
│   ├── analyze/   21 arquivos *_usecase.go (+ template.go)  — 6.734 linhas
│   ├── dto/       actor.go, commands.go, errors.go
│   └── port/
│       ├── in/    analysis_port.go, catalog_port.go, compare_port.go, digest_port.go,
│       │          market_port.go, mt5_account_port.go, portfolio_port.go, watchlist_port.go
│       └── out/   analysis_store.go, clock_audit.go, digest_store.go, market_data.go, user_store.go
├── domain/
│   ├── analysis/  (entidades + erros sentinela)   ├── persona/
│   ├── reasoning/ loop.go                         └── valuation/ (~45 arquivos de regra pura)
└── infrastructure/
    ├── config/config.go        (viper)
    ├── metrics/metrics.go      (promauto + middleware echo)
    ├── oauthsecret/crypto.go
    └── resilience/ circuit.go, errors.go, http_client.go, retry.go   (implementação própria)
```

Fora de `internal/`: `cmd/ms-analyst-finance/main.go`, `db/migrations/*.js` (scripts Mongo),
`application{,-dev,-local,-prod}.yml`, `helm/`, `deploy/`, `script-deploy-github-ms-analyst-finance.sh`.
69 arquivos `_test.go`.

### Atenção: ms-analyst-finance é referência de FORMA de `in/http`, não de tudo

Desvios que existem nele hoje (não copiar para ms-company):
- `application/analyze/analyze_usecase.go`: **um único `type UseCase struct` exportado com ~23
  portas de saída** e todos os métodos de todos os contextos (contraria §7 da skill: interface +
  impl privada, sem struct-Deus). Dependências opcionais por type-assertion
  (`if w, ok := store.(outport.WatchlistStore)`).
- `server.go` recebe `*analyze.UseCase` concreto (`NewServer(cfg, uc *analyze.UseCase, ...)`).
- `out/mongo/runstore/`: arquivos sem sufixo `_repository.go` (`watchlist.go`, `digests.go`) e
  struct de documento inline (`type watchlistDoc struct` dentro de `watchlist.go`).
- `watchlist/watchlist_handlers.go` declara `toWatchlistDTO` no próprio handler (deveria estar no mapper).
- **Sem middleware de JWT**: confia em `X-Company-Id`/`X-User-Id` vindos do BFF.

**Para um CRUD com Postgres como o ms-company, o molde mais fiel à skill é
`investbot/backend/srv/srv-mt5-market-data`** (1 use case por arquivo, impl privada,
`postgres/<ctx>/{model,mapper,repository}`, `pool.go`, `rowscanner.go`). Exemplos abaixo
misturam os dois, indicando a origem.

### Exemplos por camada

**Handler de contexto** — `ms-analyst-finance/internal/adapters/in/http/watchlist/handler.go`
```go
type Handler struct {
	uc inport.WatchlistUseCase   // depende da PORTA
}
func NewHandler(uc inport.WatchlistUseCase) *Handler { return &Handler{uc: uc} }
```
`watchlist_handlers.go`:
```go
func (h *Handler) PutWatchlist(c echo.Context) error {
	correlationID := c.Request().Header.Get("X-Correlation-ID")
	companyID, err := httperr.ParseCompany(c)          // X-Company-Id (fallback X-Tenant-Id)
	if err != nil { return c.JSON(http.StatusBadRequest, analysisdto.ErrorBody{Error: "INVALID_COMPANY", ...}) }
	var body dto.WatchlistWriteDTO
	if err := c.Bind(&body); err != nil { return c.JSON(http.StatusBadRequest, ...) }
	list, err := h.uc.PutWatchlist(appdto.WithActorCodeUser(c.Request().Context(), c.Request().Header.Get("X-User-Id")),
		companyID, appdto.WatchlistWrite{Tickers: body.Tickers, Enabled: body.Enabled})
	if err != nil { status, code, msg := httperr.MapError(err); return c.JSON(status, ...) }
	return c.JSON(http.StatusOK, toWatchlistDTO(list))
}
```

**DTO HTTP** — `.../in/http/watchlist/dto/watchlist_dto.go` (dado puro, só tag json)
```go
type WatchlistWriteDTO struct {
	Tickers []string `json:"tickers"`
	Enabled *bool    `json:"enabled,omitempty"`
}
```

**Mapper HTTP** — `.../in/http/watchlist/mapper/watchlist_mapper.go` (único que conhece dto + domain)
```go
func ToFavoritesDTO(list analysis.UserFavorites) catalogdto.FavoritesDTO { ... }
```
Versão mais limpa: `srv-mt5-market-data/internal/adapters/in/http/asset/mapper/asset_mapper.go`
(`ToCreateCommand(req)`, `ToAssetResponse(result)`).

**Erros HTTP compartilhados** — `.../in/http/httperr/errors.go`
```go
func MapError(err error) (int, string, string) {
	switch {
	case errors.Is(err, analysis.ErrAssetNotFound):
		return http.StatusNotFound, "ASSET_NOT_FOUND", "Ativo não encontrado no catálogo"
	case errors.Is(err, appdto.ErrStoreUnavailable): ...
```

**Port in** — `application/port/in/watchlist_port.go` (package `inport`)
```go
type WatchlistUseCase interface {
	GetWatchlist(ctx context.Context, companyID uuid.UUID) (analysis.Watchlist, error)
	PutWatchlist(ctx context.Context, companyID uuid.UUID, write appdto.WatchlistWrite) (analysis.Watchlist, error)
	...
}
```

**Port out** — `application/port/out/analysis_store.go` (package `outport`, um arquivo por tema)
```go
type MarketAssetStore interface {
	ListMarketAssets(ctx context.Context, filter MarketAssetFilter) ([]valuation.MarketAsset, error)
	GetMarketAsset(ctx context.Context, ticker string) (valuation.MarketAsset, error)
	UpsertMarketAsset(ctx context.Context, asset valuation.MarketAsset) error
	...
}
```

**application/dto** — `application/dto/errors.go` + `commands.go` (pacote neutro, evita ciclo)
```go
var ErrStoreUnavailable = errors.New("armazenamento de análises indisponível")
type WatchlistWrite struct { Tickers []string; Enabled *bool }
```

**Use case** (forma CERTA, do srv-mt5-market-data) —
`srv-mt5-market-data/internal/application/asset/create_asset_usecase.go`
```go
type assetUseCaseImpl struct { repo outport.AssetRepository }

func NewAssetUseCase(repo outport.AssetRepository) inport.AssetUseCase {   // devolve a interface
	return &assetUseCaseImpl{repo: repo}
}
func (uc *assetUseCaseImpl) Create(ctx context.Context, cmd dto.AssetCommand) (*dto.AssetResult, error) {
	a, err := asset.New(cmd.Codigo, nome, tipo, observacoes, cmd.Habilitado)  // regra no domínio
	if err != nil { return nil, err }
	created, err := uc.repo.Create(ctx, a)
	...
	return toResult(created), nil
}
```
(Get/List/Update/Delete em `get_asset_usecase.go`, `list_assets_usecase.go`, ... — um arquivo por ação,
mesma struct.)

**Repositório + model + mapper (Postgres)** — `srv-mt5-market-data/internal/adapters/out/postgres/assets/`
```go
// model/asset_model.go
type AssetModel struct { Codigo string; Nome *string; Tipo string; Habilitado bool; ... }

// mapper/asset_mapper.go
func ToDomainAsset(m model.AssetModel) *asset.Asset { ... }
func NullableString(s string) *string { ... }

// repository/asset_repository.go
func NewAssetRepository(pool *pgxpool.Pool) outport.AssetRepository { return &assetRepository{pool: pool} }
func (r *assetRepository) Create(ctx context.Context, a *asset.Asset) (*asset.Asset, error) {
	row := r.pool.QueryRow(ctx, `INSERT INTO srv_mt5_collector.ativos (...) VALUES ($1,...) RETURNING `+assetColumns, ...)
	m, err := scanAssetRow(row)
	if err != nil {
		var pgErr *pgconn.PgError
		if errors.As(err, &pgErr) && pgErr.Code == "23505" { return nil, dto.ErrAssetAlreadyExists }
		return nil, err
	}
	return mapper.ToDomainAsset(m), nil
}
```
Equivalente Mongo no analista: `out/mongo/runstore/run_document.go` (`type runDoc struct` com tags
`bson`) + `run_mapper.go` (`toDoc`, `uuidBin`) + `analysis_run_repository.go`.

**main.go (composition root)** — `ms-analyst-finance/cmd/ms-analyst-finance/main.go`
```go
cfg, err := config.Load()
logger := newLogger(cfg.Log.Level, cfg.Log.Format)              // zap
retry := resilience.PolicyFromConfig(cfg.HTTPRetry)
breaker := resilience.NewManager(resilience.BreakerFromConfig(cfg.CircuitBreaker))
coreHTTP := resilience.NewClient(cfg.Knowledge.Timeout(), retry, breaker)
store, err := runstore.Connect(ctx, cfg.Mongo.URI, cfg.Mongo.Database, cfg.Mongo.Timeout(), logger)
redisClient := redis.NewClient(&redis.Options{Addr: cfg.Redis.Addr(), ...})   // Ping falho = só Warn (fail-open)
auth := authclient.New(cfg.Auth, logger).WithHTTP(coreHTTP)
uc := analyze.NewUseCase(market, valuation.NewEngine(), reason, nil, auditPub, store, logger).WithCatalogCache(...)
server := httpinbound.NewServer(cfg, uc, store, logger)
go server.Start()
signal.Notify(quit, os.Interrupt, syscall.SIGTERM); <-quit
server.Stop(shutdownCtx)   // graceful 10s
```

**server.go** — `ms-analyst-finance/internal/adapters/in/http/server.go`
```go
e := echo.New()
e.Use(middleware.Recover()); e.Use(middleware.RequestID()); e.Use(metrics.Middleware())
e.GET("/health", ...)                    // liveness: {"status":"UP","service","version"}
e.GET("/ready", ... store.Ping(ctx) ...) // readiness com ping do banco
e.GET("/metrics", metrics.Handler())
hWatchlist := watchlist.NewHandler(uc)   // um Handler por contexto
api := e.Group("/api/v1/analyst")
api.GET("/watchlist", hWatchlist.GetWatchlist)
api.PUT("/watchlist", hWatchlist.PutWatchlist)
```

**Teste de adapter** — `srv-mt5-market-data/.../assets/repository/asset_repository_test.go`
```go
var _ outport.AssetRepository = NewAssetRepository(nil)
```

---

## 2. Itens (a)–(m): melhor referência por tema

| # | Tema | Melhor referência | Lib (versão go.mod) | Arquivo exemplar |
|---|---|---|---|---|
| a | CQRS commands/queries | `srv-audit` (parcial) | — | `keepguard-core/backend/srv/srv-audit/internal/application/audit/{persist_audit_usecase.go,query_audit_usecase.go}` + `application/dto/audit_query.go` |
| b | Postgres + migrations | `srv-mt5-market-data` (estrutura), `ms-achadinhos` (migrations embutidas) | `jackc/pgx/v5 v5.7.2` (pgxpool); migrations **caseiras** com `embed` | `investbot/backend/srv/srv-mt5-market-data/internal/adapters/out/postgres/{pool.go,assets/...}`; `achadinhos/backend/ms/ms-achadinhos/internal/adapters/out/postgres/migrations.go` + `db/migrations/embed.go` |
| c | Redis cache | `bff-auth` (decorator de cliente) e `ms-analyst-finance` (cache fail-open) | `redis/go-redis/v9 v9.22.0` | `keepguard-core/backend/bff/bff-auth/internal/adapters/out/http/decorator/company/redis_cache_decorator.go`; `investbot/.../ms-analyst-finance/internal/adapters/out/redis/catalog_cache.go` |
| d | HTTP de saída resiliente | `ms-analyst-finance` (stdlib própria) ou `bff-auth` (resty+gobreaker) | próprio sobre `net/http`; ou `go-resty/resty/v2 v2.11.0` + `sony/gobreaker v1.0.0` | `.../ms-analyst-finance/internal/infrastructure/resilience/{http_client.go,circuit.go,retry.go}`; `keepguard-core/backend/bff/bff-auth/internal/infrastructure/resilience/circuit_breaker.go` + `adapters/out/http/decorator/company/{retry,circuit_breaker,logging,metrics,redis_cache}_decorator.go` |
| e | JWT / X-Company-Id / correlation | `bff-auth` | `golang-jwt/jwt/v5 v5.3.0` | `keepguard-core/backend/bff/bff-auth/internal/adapters/in/http/middleware/{jwt_middleware.go,company_resolve_middleware.go,middleware.go}`; no ms: `ms-analyst-finance/.../in/http/httperr/errors.go` (`ParseCompany`) |
| f | Validação de request | `bff-auth` | `go-playground/validator/v10 v10.27.0` | `keepguard-core/backend/bff/bff-auth/internal/infrastructure/validation/validator.go` (+ `middleware/validation_middleware.go`) |
| g | Paginação | `srv-audit` (page/size/totalElements estilo Spring) | — | `keepguard-core/backend/srv/srv-audit/internal/application/port/out/audit_repository.go` (`ListFilter.Normalize`, `PageResult`) + `adapters/out/postgres/audit_repository.go` (COUNT + LIMIT/OFFSET) |
| h | Prometheus | `ms-analyst-finance` | `prometheus/client_golang v1.20.5` | `.../ms-analyst-finance/internal/infrastructure/metrics/metrics.go` |
| i | Logger | todos | `go.uber.org/zap v1.27.0` (16/16; nenhum slog/zerolog) | `investbot/backend/srv/srv-mt5-market-data/internal/infrastructure/logger/logger.go` |
| j | Config | todos menos mock | `spf13/viper v1.19.0` + `application{,-dev,-local,-prod}.yml` | `.../ms-analyst-finance/internal/infrastructure/config/config.go` (`Load()`) |
| k | Swagger | `bff-auth`, `bff-core` | `swaggo/swag v1.16.2` + `swaggo/echo-swagger v1.4.1` | `keepguard-core/backend/bff/bff-auth/internal/infrastructure/swagger/config.go` |
| l | Testes | `bff-auth` (mocks), `srv-mt5-market-data` (fakes + `var _`) | `stretchr/testify` (v1.10.0; v1.8.4 nos BFFs do core), `testify/mock` à mão, `httptest` | `keepguard-core/backend/bff/bff-auth/internal/application/auth/login_usecase_test.go`; `.../srv-mt5-market-data/.../asset_repository_test.go` |
| m | Transação / UoW | `srv-llm-gateway` (tx dentro do repo) | `pgx/v5` `pool.Begin` | `keepguard-core/backend/srv/srv-llm-gateway/internal/adapters/outbound/postgres/repository.go` (`SetDefaultProvider`) |

### (a) CQRS
Não existe pasta `command/`/`query/` em nenhum serviço Go. O mais próximo:
- `srv-audit`: `persist_audit_usecase.go` (escrita, via RabbitMQ) separado de
  `query_audit_usecase.go` (leitura), com `AuditListQueryDTO` → `AuditPageViewDTO`:
```go
type AuditQueryPort interface {
	List(ctx context.Context, query dto.AuditListQueryDTO) (dto.AuditPageViewDTO, error)
	GetByID(ctx context.Context, query dto.AuditGetQueryDTO) (dto.AuditDetailViewDTO, error)
}
```
  (desvio: `QueryAuditUseCase` exportado, sem `port/in`.)
- `bff-auth/internal/application/dto/*_command.go` (`login_command.go`, ...) e
  `bff-core/internal/application/dto/{register_command.go,register_view.go}` — só nomenclatura.
- `ms-analyst-finance/internal/application/analyze/catalog_query_usecase.go` — só nome de arquivo.

### (b) Postgres e migrations
- Driver: **pgx/v5 + pgxpool** em 6 serviços; `lib/pq` só no `mock-sms-gateway`. Sem sqlx, sem ORM.
- `pool.go` com retry exponencial (10 tentativas, 2s→30s): `srv-mt5-market-data/.../postgres/pool.go`
  (cópia declarada de `keepguard-core/backend/srv/srv-data-collector/internal/adapters/out/postgres/pool.go`).
- Migrations: **nenhum serviço usa golang-migrate nem goose.** Padrões existentes:
  - `ms-achadinhos`: `db/migrations/NNN_*.up.sql` embutidos (`//go:embed *.up.sql`) e aplicados
    no boot por `ApplyMigrations` — todos, em ordem, toda vez; idempotência pelo SQL
    (`IF NOT EXISTS`), **sem tabela de versão**:
```go
for _, nome := range arquivos {
	conteudo, _ := migrations.Files.ReadFile(nome)
	if _, err := pool.Exec(ctx, string(conteudo)); err != nil { return fmt.Errorf("aplicando %s: %w", nome, err) }
}
```
  - `srv-llm-gateway`: `internal/adapters/outbound/postgres/schema.go` (DDL embutido).
  - `srv-data-collector`: `db/migrations/collector_1NN_*.sql` aplicados à mão (memória: "migrations em prod via prompt para outra IA").
  - `srv-audit`: `db/srv_audit_schema.sql` solto.

### (c) Redis
- 5 serviços: `bff-invest`, `ms-analyst-finance`, `bff-auth`, `srv-data-collector` (v9.7.3) e
  `bff-core` (importa `go-redis/v9` em `infrastructure/cache/redis.go`, mas o go.mod marca como
  `// indirect` — go.mod não-tidy).
- Padrão fail-open do analista: erro de Redis vira `Warn` e retorna `nil, nil` → cai no banco.
- **Contrato que o ms-company Go tem que preservar**: o `bff-auth` (e `bff-core`) lêem a chave
  `company_cache:<tenantId>` **escrita pelo ms-company Java** — ver
  `bff-auth/.../decorator/company/redis_cache_decorator.go` ("Não grava cache: a escrita continua no ms-company").
  Formato JSON do valor tem que ser idêntico.

### (d) HTTP de saída
Dois estilos convivem:
1. `ms-analyst-finance`: `resilience.Client` implementa `HTTPDoer` (`Do(req)`), retry com backoff
   por classificação de status (transiente / rate-limited) + breaker próprio por host, métricas do estado.
2. `bff-auth`/`bff-core`: `resty` (retry embutido `SetRetryCount(3)`) + `gobreaker` via
   **decorators** empilhados (retry → circuit breaker → logging → metrics → redis cache) sobre a porta
   `CompanyClient`. `DefaultIsSuccessful` não conta 4xx como falha do breaker.
- Cliente do ms-auth (client-credentials OAuth com cache de token por company):
  `ms-analyst-finance/internal/adapters/out/http/authclient/oauth_client.go` (`GetToken(ctx, companyID)`, renovação 10 min antes).

### (e) Auth / tenant / correlation
- JWT só nos BFFs: `bff-auth/.../middleware/jwt_middleware.go`, `bff-core/.../middleware/jwt_middleware.go`,
  `bff-invest/.../middleware/jwt.go`. Os `ms-*` Go **não validam JWT** — recebem `X-Company-Id`,
  `X-User-Id`, `X-Correlation-ID` do BFF (`httperr.ParseCompany`).
- Correlation-id: `bff-auth/.../middleware/middleware.go` (`CorrelationIDMiddleware` → `EnsureCorrelationID`
  gera UUID se ausente); no analista, só é lido do header e devolvido no `ErrorBody`.
- Tenant→company: `company_resolve_middleware.go` chama `GET {ms-company}/api/v1/companies/x-tenant-id/{tenantId}`.

### (f) Validação
`go-playground/validator/v10 v10.27.0` em 4 serviços (`bff-auth`, `bff-core`, `ms-achadinhos`,
`bff-achadinhos`), com tags customizadas e mensagens PT (`getValidationMessage`). Os demais validam à
mão no handler (`if req.Codigo == "" { return httperr.BadRequest(...) }` — srv-mt5-market-data) ou no
construtor de domínio (`asset.New(...)` retorna erro).

### (g) Paginação
Única com página estilo Spring Data é o `srv-audit` (útil para manter contrato do Java):
```go
type AuditPageViewDTO struct {
	Content []AuditEventViewDTO `json:"content"`; Page int `json:"page"`; Size int `json:"size"`
	TotalElements int64 `json:"totalElements"`; TotalPages int `json:"totalPages"`
}
// Normalize: page<0→0, size<=0→20, size>100→100, sort whitelist
```
SQL: `SELECT COUNT(*) ... ` + `LIMIT $n OFFSET $m`. Outros usam só `limit` (analista: `strconv.Atoi(c.QueryParam("limit"))`).

### (h) Prometheus
13/16 serviços. Padrão: `promauto` com variáveis de pacote + `metrics.Middleware()` no echo + rota
`/metrics` via `promhttp`. Os BFFs do core usam struct `*metrics.Metrics` injetada.

### (i) Logger
`zap v1.27.0` em 16/16. `infrastructure/logger/` só onde agrega algo (`bff-auth` tem `tcp_logger.go`,
envio TCP). Regra da fase 6 (go-standard.md §9): **zap direto**, não copiar o wrapper.

### (j) Config
viper em 15/16. `application.yml` base + `MergeInConfig` de `application-$APP_ENV.yml` (erro ignorado),
`AutomaticEnv` com `.`→`_`, `BindEnv` explícitos com aliases, defaults em `setDefaults()`. Busca em `.` e `/app`.

### (k) Swagger
Só `bff-auth` e `bff-core` (`swaggo`). `bff-auth` versiona `docs/docs.go` gerado (divergência
conhecida); padrão desejado é o do `bff-core` (placeholder `docs/swagger.go`).

### (l) Testes
- testify em 11 serviços; `testify/mock` com mocks **escritos à mão** em 3 (`bff-auth`, `bff-core`, `srv-email-sender`).
- `httptest.NewServer` em 10 (fake de API externa).
- **Zero**: testcontainers, mockery, gomock, sqlmock, pgxmock, miniredis. Repositório Postgres não
  tem teste de integração — só `var _ outport.X = NewY(nil)`.
- Cobertura: `go test -coverpkg=./internal/... -coverprofile=cover.out ./...` (go-standard.md §3).

### (m) Transação
Sem unit of work em lugar nenhum. Transação vive **dentro de um método do repositório**:
```go
tx, err := r.pool.Begin(ctx)
defer tx.Rollback(ctx)
tx.Exec(ctx, `UPDATE providers SET is_default = true ... WHERE id = $1 AND enabled`, id)
tx.Exec(ctx, `UPDATE providers SET is_default = false ... WHERE id <> $1 AND is_default`, id)
...
return p, tx.Commit(ctx)
```
(`srv-llm-gateway/.../outbound/postgres/repository.go:132`; idem `mock-sms-gateway/.../sms_repository.go:89` com `database/sql`.)

---

## 3. Bibliotecas recorrentes (dependências diretas nos 16 go.mod)

| Lib | Versão predominante | Serviços |
|---|---|---|
| `labstack/echo/v4` | v4.13.3 (bff-auth/bff-core v4.11.4; srv-sms-sender v4.12.0) | 16 |
| `go.uber.org/zap` | v1.27.0 | 16 |
| `spf13/viper` | v1.19.0 (BFFs do core v1.18.2) | 15 |
| `prometheus/client_golang` | v1.20.5 (BFFs do core v1.19.0) | 13 |
| `stretchr/testify` | v1.10.0 | 11 |
| `google/uuid` | v1.6.0 | 7 |
| `rabbitmq/amqp091-go` | v1.10.0 | 7 (+2 BFFs via `wagslane/go-rabbitmq v0.12.4`) |
| `jackc/pgx/v5` | v5.7.2 | 6 |
| `redis/go-redis/v9` | v9.22.0 (data-collector v9.7.3) | 5 (bff-core marcado indirect) |
| `robfig/cron/v3` | v3.0.1 | 4 |
| `go-playground/validator/v10` | v10.27.0 | 4 |
| `golang-jwt/jwt/v5` | v5.3.0 | 3 (bff-auth, bff-core, bff-invest) |
| `go-resty/resty/v2` + `sony/gobreaker` | v2.11.0 / v1.0.0 | 2 (bff-auth, bff-core) |
| `swaggo/swag` + `echo-swagger` | v1.16.2 / v1.4.1 | 2 |
| `mongo-driver` | v1.17.3 | 2 |
| `lib/pq` | v1.12.3 | 1 (mock) |

---

## 4. Config por perfil, Dockerfile, helm, healthcheck, deploy (exemplares keepguard-core)

**Perfil**: `keepguard-core/backend/srv/srv-audit/application{,-dev,-local,-prod}.yml`. O `-prod` só
sobrepõe o que muda (host `postgres`, filas `.prod`); segredos vêm de env (secret K8s). `APP_ENV`
escolhe o perfil (Dockerfile do srv-audit fixa `ENV APP_ENV=dev`; o deployment sobrescreve).

**Dockerfile** — duas variantes válidas:
- Distroless + binário de healthcheck: `keepguard-core/backend/bff/bff-auth/deploy/Dockerfile`
  (`golang:1.27` → `gcr.io/distroless/base-debian12`, `USER 65532:65532`, `COPY application*.yml /app/`,
  `ENV HEALTHCHECK_URL=http://localhost:8381/health`).
- Alpine + curl: `keepguard-core/backend/srv/srv-audit/deploy/Dockerfile`
  (`golang:1.27-alpine` com `--mount=type=cache`, `-ldflags="-s -w"` → `alpine:3.20`,
  `HEALTHCHECK ... curl -fsS http://localhost:8620/health`).
- Healthcheck binário: `keepguard-core/backend/srv/srv-audit/deploy/healthcheck/main.go` (GET em `HEALTHCHECK_URL`, exit 1 se != 200).

**Helm**: `keepguard-core/backend/bff/bff-auth/helm/{Chart.yaml,values.yaml,templates/deployment.yaml,service.yaml}`
— `livenessProbe`/`readinessProbe` em `/health`, portas `http` + `metrics`, `resources` de `values.yaml`,
`imagePullSecrets: ghcr-secret`.

**Deploy**: `keepguard-core/backend/srv/srv-audit/script-deploy-github-srv-audit.sh` (commit/merge/build
opt-in, build via `git archive` do commit, `linux/amd64`, push GHCR) → chama `script-deploy-k8s-prod.sh`,
que faz **`kubectl set image deployment/<svc>`** + `rollout status` no namespace `keepguard` usando
`keepguard-core/docker/keepguard-kubeconfig.yaml`. **Ele não roda `helm upgrade`** — por isso os
`values.yaml` divergem do que está no cluster (ver §5).

---

## 5. Memória/CPU: Go vs Java no keepguard-core

Duas fontes: `helm/values.yaml` de cada serviço (declarado) e o cluster de produção
(`kubectl get deploy` / `kubectl top pods`, namespace `keepguard`, lido em 2026-10-03). Os valores
**não batem** com o helm porque o deploy só faz `set image` — o cluster é a verdade.

| Serviço | Ling. | Helm req/limit mem | **Prod req/limit mem** | CPU prod | **Uso real (top)** |
|---|---|---|---|---|---|
| bff-auth | Go | 128Mi / 256Mi | 128Mi / 512Mi | — | 10Mi, 1m |
| bff-core | Go | 128Mi / 256Mi | 128Mi / 512Mi | — | 10Mi, 1m |
| srv-audit | Go | 64Mi / 256Mi | 64Mi / 256Mi | — | 9Mi, 1m |
| srv-llm-gateway | Go | 64Mi / 256Mi | 64Mi / 256Mi | — | 9Mi, 1m |
| srv-email-sender | Go | 64Mi / 128Mi | 64Mi / 128Mi | — | 9Mi, 1m |
| srv-sms-sender | Go | 64Mi / 128Mi | 64Mi / 256Mi | — | 7Mi, 1m |
| srv-data-collector | Go | 256Mi / 2Gi | 256Mi / 2Gi | — | 15Mi, 2m |
| mock-sms-gateway | Go | 64Mi / 128Mi | 64Mi / 256Mi | — | 1Mi, 1m |
| **ms-company** | Java | 256Mi / 512Mi | **384Mi / 1Gi** | — | **774Mi, 2m** |
| ms-auth | Java | 256Mi / 512Mi | 384Mi / 1Gi | — | 817Mi, 2m |
| ms-user | Java | 256Mi / 512Mi | 384Mi / 1Gi | — | 716Mi, 2m |
| ms-user-consents | Java | 256Mi / 512Mi | 384Mi / 1Gi | — | 705Mi, 2m |
| ms-communication | Java | 256Mi / 512Mi | 384Mi / 1Gi | — | 745Mi, 2m |
| ms-billing | Java | 384Mi / 1Gi (`-Xmx512m`) | 384Mi / 1Gi | — | 628Mi, 3m |
| ms-knowledge | Java | 512Mi / 1Gi | 512Mi / 1Gi | 250m / 1 | 464Mi, 3m |
| ms-ai-guardian | Java | 512Mi / 1Gi | 512Mi / 1Gi | 250m / 1 | 601Mi, 5m |

Referência de ms Go com banco: `ms-analyst-finance` (Mongo+Redis+RabbitMQ) prod 64Mi/384Mi, uso 13Mi;
`ms-achadinhos` (Postgres) 64Mi/256Mi, uso 12Mi.

Leitura: Java ocupa ~460–820Mi parado, Go ~7–15Mi (≈50–80×). Somando os 7 Java "padrão" a ~5 GiB
residentes. Um ms-company Go pode nascer com `requests 64Mi / limits 256Mi` (padrão `srv-audit`);
o ganho estimado só com ele é ~750Mi de RAM no nó. Nenhum serviço Go define CPU; Java também não,
exceto knowledge/ai-guardian.

---

## 6. Lacunas — o que ms-company precisa e NÃO tem exemplar Go no monorepo

Estado do Java (`keepguard-core/backend/ms/ms-company/src/main/java/com/keepguard/ms_company/`):
7 contextos (`company`, `address`, `bankaccount`, `cnae`, `companypolicy`, `contact`, `representative`),
8 entidades JPA (`infrastructure/persistence/entity/*JpaEntity.java`, incl. `CompanyMfaChannelJpaEntity`),
relacionamentos JPA em 7 arquivos, `@Transactional` em 14, `CompanyCommandService`/`CompanyQueryService`,
cliente Feign do ms-auth (`adapters/out/feign/AuthProvisionClient.java`, `AuthRoleProvisionAdapter.java`),
`port/out/{auth,cache,metrics,persistence}`, Redis em `infrastructure/redis`.

1. **CQRS explícito (command/query como portas separadas)** — não existe. Precisa decidir a forma
   (sugestão compatível com a skill: `port/in/<ctx>_command_port.go` + `<ctx>_query_port.go`, use cases
   `create_company_usecase.go` / `get_company_query_usecase.go`, `application/dto/*_command.go` e
   `*_view.go` como no `srv-audit`/`bff-core`). Exige registrar na skill, que é normativa.
2. **Postgres com relacionamentos (agregado com filhos 1:N)** — todo repositório Go é tabela única.
   Não há exemplo de carregar Company + endereços/contatos/representantes (JOIN ou N queries), nem de
   upsert de coleção filha. Também não há `out/postgres/<ctx>/` com 7 contextos (o máximo é 2 no
   `srv-mt5-market-data`).
3. **Unit of Work / transação atravessando repositórios** — só existe tx dentro de um método de
   repositório (`srv-llm-gateway`). Operação "criar company + provisionar no ms-auth + gravar filhos"
   não tem molde (nem para compensação/outbox se o ms-auth falhar depois do commit).
4. **Migrations versionadas** — nenhum golang-migrate/goose; o padrão do `ms-achadinhos` reaplica tudo
   sem tabela de versão. Agravante: o Java usa `ddl-auto: update` (dev/local) e `validate` (prod) —
   **não há DDL versionado do ms-company**; será preciso extrair o schema baseline do Postgres de prod.
5. **JWT/roles dentro de um `ms-*` Go** — nenhum ms Go valida token; se o Java valida (lib-security),
   o exemplar é só dos BFFs (`bff-auth/.../middleware/jwt_middleware.go`, `roles_middleware.go`).
6. **Escrita do `company_cache:<tenantId>` no Redis** — só há leitores Go; o escritor é o Java.
   Formato/TTL precisam ser copiados do Java para não quebrar `bff-auth` e `bff-core`.
7. **Paginação estilo Spring com sort multi-campo** — só o `srv-audit` (sort por whitelist, 1 campo).
8. **Teste de integração de repositório Postgres** — inexistente (sem testcontainers/pgxmock).
9. **Validação de bean complexa (CNPJ, CPF de representante, conta bancária)** — validator existe, mas
   sem validadores de documento brasileiro registrados.
10. **Problem Details (RFC 7807)** — o `bff-auth` lê o erro do Java como `MSAuthErrorResponse`
    (`detail`, `title`, `properties.errorCode`) em `adapters/out/http/client/company_client.go`; o Go novo
    precisa devolver o mesmo formato. Nos Go, os structs com `detail` (`bff-auth/internal/application/dto/error_dto.go`,
    `bff-auth/internal/pkg/errors.go`, `bff-core/internal/application/dto/http_error.go`) servem sobretudo
    para LER erro do Java; não há um `ms-*` Go que emita Problem Details como contrato.
