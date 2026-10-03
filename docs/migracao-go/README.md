# Migração Java → Go: libs + ms-company — levantamento

Data: 2026-10-03. Status: **fases 1 e 2 implementadas localmente** (`lib-go-common` e `ms-company-go`, ao lado do Java em `keepguard-core/backend/ms/`). Nada em produção ainda — ver "Implementação" no fim.

## Por que migrar

| | Uso real de memória em prod (`kubectl top`, 2026-10-03) |
|---|---|
| ms-company (Java) | **772Mi** (limit 1Gi, `-Xmx1024m` = risco de OOMKill) |
| 8 ms Java do keepguard-core somados | **≈5,45Gi** |
| ms Go equivalentes (ms-achadinhos com Postgres, ms-analyst-finance) | **11–13Mi** cada |
| Nó único do cluster | 72% da memória ocupada |

O ms-company tem ~15,7k linhas Java em 199 arquivos. Os 8 ms Java somam ~101k linhas.

## Documentos

| Arquivo | Conteúdo |
|---|---|
| `01-ms-company-especificacao-atual.md` | **Especificação de referência.** Os ~75 endpoints com campos, validações e mensagens exatas; contrato de erro; domínio e transições de status; cada use case passo a passo; schema; chaves Redis; auditoria; métricas; testes; 24 bugs/comportamentos estranhos. Tudo com `arquivo:linha`. |
| `02-consumidores-e-infra-prod.md` | Quem chama o ms-company e quais campos lê; contrato Redis com os BFFs; Deployment real de prod (diverge do Helm do repo); banco; dimensionamento dos outros ms Java. |
| `03-libs-java.md` | lib-common, lib-security, lib-validation classe a classe; quem usa o quê; código morto; o que vira o quê em Go. |
| `04-padrao-go-referencia.md` | Padrões Go já usados no monorepo (libs, versões, exemplares por tema) e lacunas. |
| `05-arquitetura-alvo-go.md` | Mapeamento Java → Go camada a camada, árvore-alvo com CQRS, desenho da lib Go compartilhada e checklist de compatibilidade. |

## O que o levantamento mostrou (o que muda a forma do plano)

1. **Só 2 dos ~75 endpoints têm consumidor**: `GET /companies/x-tenant-id/{tenantId}` (bff-auth,
   bff-core, bff-invest, ms-auth) e `GET /companies/{id}` (ms-auth). Nenhuma escrita é chamada; hoje
   a gestão de company em prod é feita por SQL direto.
2. **Existe um contrato fora do HTTP**: bff-auth e bff-core leem a chave Redis
   `company_cache:tenantId:<id>` gravada pelo Java. O Go tem que gravar a mesma chave com o mesmo JSON.
3. **A auditoria é invisível no código**: `@LogOperation` (lib-common) publica evento no RabbitMQ em
   todo método de escrita. Em Go isso vira chamada explícita, senão some.
4. **Não existe DDL versionado**: o schema `ms_company` foi criado pelo Hibernate (`ddl-auto: update`,
   porque prod roda com profile `local`). A migration Go precisa nascer do schema real de prod.
5. **Das 3 libs, o ms-company só usa a lib-common** — e dela, só validadores BR, exceções, auditoria e
   métricas. lib-security é usada por 3 ms; lib-validation só pelo ms-user.
6. **Não há código Go compartilhado no monorepo**: 16 módulos isolados, cada serviço no seu repo Git,
   build via `git archive`. Uma lib Go exige decidir como ela entra no build (D2).
7. **Não há serviço Go com CQRS explícito, agregado com filhos nem unit of work.** O ms-company será o
   molde; o padrão tem que entrar na skill `new-app-go`.

## Decisões que são suas (antes do plano)

| # | Decisão | Opções | Recomendação |
|---|---|---|---|
| D1 | Escopo do ms-company Go | (a) paridade total, ~75 endpoints; (b) só o que tem consumidor + escritas de company | **(a)** — é o cadastro-mestre de empresa e o backoffice vai precisar das escritas; a ordem de implementação pode começar pelas leituras. |
| D2 | Como a lib Go entra no build | (a) repo próprio `lib-go-common` com tag + `go mod vendor` commitado em cada serviço; (b) repo + `GOPRIVATE` + token GitHub no `docker build`; (c) `go.work`/`replace` local | **(a)** — build continua hermético a partir do `git archive`, sem segredo no build. (c) quebra o modelo atual de repos. |
| D3 | 3 libs Go (espelho do Java) ou 1 módulo com pacotes | (a) 1 módulo, pacotes `brdoc`, `apperr`, `correlation`, `httpmetrics`, `audit`, `auth`; (b) 3 módulos | **(a)** — a divisão em 3 é coisa do classpath do Spring; em Go pacote não usado não entra no binário. |
| D4 | lib-security (`auth`) e lib-validation na fase 1? | ms-company não usa nenhuma das duas | `auth` como **fase 1b opcional** (consolida 4 middlewares JWT divergentes dos BFFs); moderação **não** vira lib — vai com o ms-user. |
| D5 | Criar company + provisionar roles no ms-auth | (a) igual hoje: grava, provisiona, falha = company órfã + 500; (b) provisiona dentro da transação, falha = rollback (ms-auth é idempotente) | **(b)** — mesmo resultado no caminho feliz, sem company órfã no erro. |
| D6 | Bugs do Java (`01` §12) | (a) paridade estrita, inclusive bugs; (b) paridade de contrato + corrigir os que perdem dado ou devolvem 500 indevido | **(b)**, com lista fechada: PUT de company apagando MFA; JSON/UUID/rota inválidos dando 500; erros de CNAE dando 500; cache de filhos nunca invalidado; busca por cidade/UF quebrada. O resto fica como está e documentado. |
| D7 | Métricas de negócio com `entity_id`/`company_id` como tag | (a) manter; (b) remover essas tags (cardinalidade ilimitada) | **(b)** — nenhum alerta usa, e o dashboard atual (métricas de JVM) vai ser refeito de qualquer forma. |
| D8 | Onde nasce o código Go | **Decidido (2026-10-03): diretório novo, lado a lado com o Java**, em `keepguard-core/backend/ms/`. Nome sugerido: `ms-company-go` e `lib-go-common` (nome final pendente). | No corte, o Service K8s `ms-company` continua com esse nome e passa a apontar para os pods Go — consumidores não mudam. |

## Esboço de fases (o plano detalhado vem depois das decisões)

- **Fase 0 — pré-requisitos**: schema real de prod (comando abaixo); decisões D1–D8; registrar o
  padrão CQRS na skill `new-app-go`.
- **Fase 1 — lib-go-common**: `brdoc`, `apperr`, `correlation`, `httpmetrics`, `audit`, com testes
  portados do Java. *Fica de fora:* `auth` (1b), moderação, enums de comunicação.
- **Fase 1b (opcional) — `auth`**: port do lib-security. *Fica de fora:* trocar os BFFs para usá-lo.
- **Fase 2 — ms-company Go**: domínio + application + Postgres → HTTP + Redis + ms-auth + auditoria →
  testes de contrato (HTTP contra a spec do `01`, JSON do Redis contra os structs dos BFFs) → corte em
  prod com rollback por `kubectl set image` de volta para a imagem Java.
  *Fica de fora:* migração dos outros ms Java e troca dos BFFs para a lib.

## Pendências

- **Schema real de prod** — o classificador bloqueou o acesso ao psql de prod para os agentes.
  Rodar a partir da raiz do monorepo (`~/Projetos/keepguard`) e salvar nesta pasta:

  ```bash
  kubectl --kubeconfig keepguard-core/docker/keepguard-kubeconfig.yaml -n keepguard exec \
    $(kubectl --kubeconfig keepguard-core/docker/keepguard-kubeconfig.yaml -n keepguard get pod -l app=postgres -o name | head -1) -- \
    pg_dump -s -n ms_company -U keepguard_api_user keepguard_api_db > keepguard-core/backend/ms/ms-company/docs/migracao-go/ms_company_schema_prod.sql
  ```

- **Achados de prod independentes da migração** (não mexi em nada):
  ms-company roda com profile `local` em prod; `-Xmx1024m` com limit 1Gi (o mesmo em ms-auth, ms-user,
  ms-communication e ms-user-consents); `AUTH_SERVICE_URL` ausente no Deployment (criar company pela
  API falharia no provisionamento); usuário/senha do RabbitMQ em texto literal no spec do Deployment;
  secret JWT com valor padrão versionado na lib-security e nos `application.yml`.

## Implementação (2026-10-03)

Decisões D1–D7 aplicadas conforme as recomendações da tabela; D8: diretório novo lado a lado.

| Projeto | Onde | Estado |
|---|---|---|
| `lib-go-common` | `keepguard-core/backend/ms/lib-go-common` | 5 pacotes (`brdoc`, `apperr`, `correlation`, `httpmetrics`, `audit`), ~1,1k linhas, testes ok. `auth` (lib-security) ficou para a fase 1b. |
| `ms-company-go` | `keepguard-core/backend/ms/ms-company-go` | 7 contextos, 74 rotas (as mesmas do Java), 240 arquivos de código + 49 de teste, ~21k linhas. `go build`, `go vet` e 42 pacotes de teste ok; build isolado com `-mod=vendor` (sem a lib ao lado) ok. |

Forma: `05-arquitetura-alvo-go.md`, com um ajuste — os use cases ficam em
`application/<ctx>/commands/` e `application/<ctx>/queries/` (plural, para não colidir com os pacotes
`dto/command` e `dto/query`).

### Correções D6 aplicadas (lista fechada)
- Erro de framework (JSON/UUID/query param inválido, param obrigatório ausente, rota/método inexistente) → 400/404/405 em vez de 500.
- Erros que viravam 500 por exceção genérica passam ao status semântico, com a mesma mensagem: CNAE não encontrado (404), CNAE duplicado (409), último CNAE ativo / CNAE principal (400), e-mail inválido pela lib (400), e-mail de contato duplicado em outra caixa ou violação de UNIQUE (409), mesmo CPF/e-mail de representante em 2 empresas (devolve o mais antigo).
- `PUT /companies/{id}` não apaga mais os canais de MFA.
- Criação de empresa + provisionamento no ms-auth na mesma transação (D5).
- Cache: escrita de endereço/conta/contato/representante invalida a lista da empresa; delete de empresa limpa cnpj/code/tenant.
- Endereço/conta "ativo exclusivo" em transação, sem perder o `company_id`.
- Busca de empresa por `city`/`state` e de conta por `accountType` funcionam.
- `GET /companies/cnpj/{cnpj}` com máscara consulta o banco só com dígitos.

### Divergências menores (não mudam contrato)
- Listagens sem ORDER BY no Java agora têm ordem estável (`created_at, id`).
- `companyId` vem na mesma query (fim do N+1 de listAll/search); o 404 "Empresa não encontrada para o endereço/contato/..." deixou de ser possível (FK NOT NULL).
- Campo de ordenação desconhecido é ignorado (no Java, 500); `%`/`_` da busca são escapados.
- Update de endereço/conta: campo em branco é recusado depois do lookup — id inexistente + campo em branco responde 404 em vez de 400.
- Update de contato preserva `created_at` (o merge do Java gravava null).
- Auditoria dos creates leva o id criado (no Java ia null) e `{name}`/`{code}` resolvidos.
- `@Size(max=150)` sem mensagem no e-mail do representante usa a padrão do Hibernate ("size must be between 0 and 150").
- Não portados por não terem endpoint/uso: `getSimpleByTenantId`, `HelperController` (`/api/v1/helper/*`), `listAll` de CNAE, domainEvents de política, `MfaPolicyEnum`.

### Pendências para ir a produção (cada uma precisa da sua confirmação)
1. **Schema de prod:** rodar o `pg_dump -s` acima e conferir contra `ms-company-go/db/migrations/001_ms_company_baseline.up.sql`.
2. **Repositórios GitHub:** criar `keepguard/ms-company-go` (e `keepguard/lib-go-common`) com os secrets `GHCR_PAT` e `KUBECONFIG_B64` (os mesmos do bff-invest); `git init` + remote + branch `develop` locais.
3. **Primeiro deploy (paralelo ao Java):** criar Deployment/Service `ms-company-go` a partir de `ms-company-go/helm/` — o script só faz `kubectl set image`, não cria o Deployment.
4. **Validação lado a lado:** comparar respostas Go × Java nos 2 GETs consumidos e conferir o JSON no Redis.
5. **Corte:** trocar o selector do Service `ms-company` para `app=ms-company-go`; rollback = voltar o selector. Depois, desligar o Deployment Java e regenerar o dashboard Grafana (hoje usa métricas de JVM).
