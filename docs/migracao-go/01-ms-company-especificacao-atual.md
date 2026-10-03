# ms-company — Levantamento para migração Java → Go

Fonte: `keepguard-core/backend/ms/ms-company` (Spring Boot 3.5.3, Java 25, `pom.xml:5-13`, versão `1.0.20-1.0.0`).
Todas as referências são relativas a `src/main/java/com/keepguard/ms_company/` salvo indicação. Abreviações: `ctrl/` = `adapters/in/rest/`, `svc/` = `application/service/`, `dom/` = `domain/`, `infra/` = `infrastructure/`, `lib/` = `keepguard-core/backend/ms/lib-common/src/main/java/com/keepguard/lib_common/` (fonte 1.0.38-SNAPSHOT; o ms usa o jar 1.0.37-SNAPSHOT, `pom.xml:106-110` — o jar 1.0.37 em `~/.m2` contém as mesmas classes de audit/logging/metrics).

Legenda de confiança: **[código]** = lido diretamente; **[framework]** = comportamento padrão do Spring/Jackson/Hibernate inferido (não está no código do ms, validar com chamada real antes de congelar o contrato Go).

---

## 0. Resumo executivo (o que mais importa para a reescrita)

1. **Sem autenticação**: não há Spring Security nem filtro de auth; todos os endpoints são públicos. Nenhum header é obrigatório. `X-Correlation-ID` é lido/ecoado; `X-Tenant-Id` só é usado em log/métrica/auditoria.
2. **Consumidores reais conhecidos**: `GET /api/v1/companies/x-tenant-id/{tenantId}` (bff-auth `internal/adapters/out/http/client/company_client.go:48`, bff-core `internal/adapters/outbound/http/client/company_client.go:53`, ms-auth `CompanyClient.java:16`) e `GET /api/v1/companies/{id}` (ms-auth `CompanyClient.java:19`). O resto da superfície não tem consumidor encontrado em `keepguard-core/backend`.
3. **Erros**: todo erro sai como `ProblemDetail` (RFC 7807) com campos extras `timestamp`, `path`, `errorCode`. O handler genérico `@ExceptionHandler(Exception.class)` com `HIGHEST_PRECEDENCE` transforma em **500** até erros que o Spring normalmente daria 400/404/405 (JSON inválido, UUID inválido no path, rota inexistente, método não suportado, query param obrigatório ausente) — [framework].
4. **Schema** gerido por Hibernate `ddl-auto` (sem Flyway/Liquibase). Schema `ms_company`, 8 tabelas.
5. **Cache Redis** com TTL de 30 dias e invalidação incompleta: listas de endereço/conta/contato/representante **nunca** são invalidadas pelas próprias operações de escrita (só por mutações da Company). Listas vazias também são cacheadas.
6. **Resposta de Company nunca traz** `address`, `contacts`, `representatives`, `bankAccount`, `cnaes` (sempre `null`); só `mfaChannels` vem preenchido.
7. **Bug atual relevante**: `PUT /api/v1/companies/{id}` apaga todos os canais de MFA da empresa (seção 12).
8. **Profile em produção**: o Helm define `SPRING_PROFILES_ACTIVE=local` (`helm/values.yaml:16`) e `application-prod.yml` tem YAML com indentação inválida (`application-prod.yml:40-43`) — forte indício de que prod roda com profile `local`. Confirmar com `kubectl get deploy ms-company -n keepguard -o yaml` antes de decidir config.

---

## 1. Inventário de endpoints

### 1.0 Regras gerais (valem para todos os controllers)

- **Porta**: `server.port: 8083` (`resources/application.yml:2`, `application-dev.yml:2`, `application-prod.yml:2`); profile `local` usa `8583` (`application-local.yml:2`), mas o Helm injeta `SERVER_PORT=8083` (`helm/templates/deployment.yaml:30-31`). **Sem context-path** (nenhum `server.servlet.context-path`). Os paths abaixo são completos.
- **Headers**: nenhum obrigatório. `X-Correlation-ID` opcional (gerado UUID v4 se ausente/vazio e devolvido no header de resposta — `infra/filter/CorrelationIdFilter.java:46-61`). `X-Tenant-Id` opcional (só log/métrica/auditoria). `Authorization` ignorado. **Não existe uso de `X-Company-Id` nos controllers** (só é lido pelo coletor de auditoria da lib, `lib/audit/AuditContextCollector.java:35-49`).
- **Serialização (Jackson padrão Spring Boot + `spring.jackson.serialization.INDENT_OUTPUT: true`, `application.yml:48-50`)**:
  - Respostas **pretty-printed** (indentação) [código+framework].
  - Nomes JSON = nomes dos campos Java (camelCase). Não há nenhum `@JsonProperty`/`@JsonNaming` no ms.
  - `null` **é serializado** como `null` (não há `NON_NULL` configurado) [framework].
  - Enums serializados pelo `name()` (ex.: `"PENDING_APPROVAL"`). Na entrada, o valor tem que ser exatamente o `name()` (case-sensitive); valor inválido → `HttpMessageNotReadableException` → **500** (ver seção 2).
  - `LocalDateTime` → string ISO local sem fuso, ex. `"2026-10-03T14:05:07.123456"` (frações variáveis) [framework]. `LocalDate` → `"1990-01-15"`. `UUID` → string minúscula com hífens.
  - Booleanos de DTOs Lombok com campo `boolean active` saem como `"active"`.
  - Campos desconhecidos no request são ignorados (Spring Boot desliga `FAIL_ON_UNKNOWN_PROPERTIES`) [framework].
- **Validação de request**: `@Valid` em `@RequestBody` (Jakarta Bean Validation). Falha → 400 (seção 2). Além do Bean Validation, os records de *Command* (`application/dto/**`) validam no construtor e lançam `IllegalArgumentException` → 400; e o domínio valida de novo (seção 3).
- **Paginação** (`application/dto/common/PageResultDTO.java:5-27`): record `PageResultDTO<T>(List<T> items, long total, int page, int size)` com getters extras `getTotalPages()` e `isEmpty()`. JSON esperado [framework — Jackson serializa getters de record]:
  ```json
  {"items":[...],"total":123,"page":0,"size":20,"totalPages":7,"empty":false}
  ```
  `hasNext()`/`hasPrevious()` **não** saem (não são getters). `totalPages = ceil(total/size)` (0 se size ≤ 0). Validar com chamada real.
  - Query params: `page` (default `0`), `size` (default `20`), `sortFields` (lista; aceita `?sortFields=a&sortFields=b` ou `a,b`), `sortDirection` (default `"ASC"`; `"DESC"` case-insensitive vira DESC, qualquer outro vira ASC).
  - `page < 0` → 400 "Página deve ser maior ou igual a 0"; `size <= 0` → 400 "Tamanho da página deve ser maior que 0"; `size > 100` → 400 "Tamanho da página não pode ser maior que 100" (records `AddressSearchCriteriaDTO.java:18-28`, `BankAccountSearchCriteriaDTO.java:17-27`, `ContactSearchCriteriaDTO.java:19-29`). **Company search não valida page/size** (`CompanySearchCriteriaDTO.java:7-29` não tem construtor compacto) — `size=0` → `PageRequest.of` lança IAE "Page size must not be less than one" → 400 via handler de IAE [framework]; size > 100 é aceito.
  - `sortFields` com nome de propriedade inexistente → `PropertyReferenceException` → 500 [framework]. Nomes válidos = nomes dos campos das JPA entities (seção 5).
- **Métricas por endpoint**: cada método de controller tem `@MetricsEndpoint(endpoint=...)` da lib (seção 8.2).
- **Port/in**: todos os controllers chamam o `*Port` (interface) implementado por `*UseCaseService`, que só delega para `*CommandService` ou `*QueryService` (seção 4).

### 1.1 CompanyController — `ctrl/company/CompanyController.java` (base `/api/v1/companies`, linha 31)

| # | Método | Path | Params | Request body | Sucesso | Port chamado | Linhas |
|---|---|---|---|---|---|---|---|
| 1 | POST | `/api/v1/companies` | — | `CompanyCreateRequestDTO` | **201** `CompanyResponseDTO` | `CompanyPort.create` | 40-54 |
| 2 | PUT | `/api/v1/companies/{id}` | `id` UUID | `CompanyUpdateRequestDTO` | 200 `CompanyResponseDTO` | `update(id, cmd)` | 56-72 |
| 3 | PATCH | `/api/v1/companies/{id}/approve` | `id` | — | 200 `CompanyResponseDTO` | `approve(id)` | 74-87 |
| 4 | PATCH | `/api/v1/companies/{id}/reject` | `id` | — | 200 | `reject(id)` | 89-102 |
| 5 | PATCH | `/api/v1/companies/{id}/activate` | `id` | — | 200 | `activate(id)` | 104-117 |
| 6 | PATCH | `/api/v1/companies/{id}/deactivate` | `id` | — | 200 | `deactivate(id)` | 119-132 |
| 7 | PATCH | `/api/v1/companies/{id}/suspend` | `id` | — | 200 | `suspend(id)` | 134-147 |
| 8 | PATCH | `/api/v1/companies/{id}/block` | `id` | — | 200 | `block(id)` | 149-162 |
| 9 | GET | `/api/v1/companies/{id}` | `id` | — | 200 `CompanyResponseDTO` | `getById(id)` | 164-177 |
| 10 | GET | `/api/v1/companies/x-tenant-id/{tenantId}` | `tenantId` UUID | — | 200 `CompanyResponseDTO` (só se ACTIVE) | `getByTenantId` | 179-192 |
| 11 | GET | `/api/v1/companies/code/{codeCompany}` | `codeCompany` UUID | — | 200 | `getByCodeCompany` | 194-207 |
| 12 | GET | `/api/v1/companies/cnpj/{cnpj}` | `cnpj` String (sem normalização no DB) | — | 200 | `getByCnpj` | 209-222 |
| 13 | GET | `/api/v1/companies/search` | query: `name`, `legalName`, `cnpj`, `city`, `state`, `status`, `page`=0, `size`=20, `sortFields`, `sortDirection`=ASC | — | 200 `PageResultDTO<CompanyResponseDTO>` | `search(criteria)` | 224-257 |
| 14 | PUT | `/api/v1/companies/{id}/mfa-channels` | `id` | **array** `[CompanyMfaChannelRequestDTO]` | 200 `CompanyResponseDTO` | `updateMfaChannels` | 260-274 |
| 15 | DELETE | `/api/v1/companies/{id}` | `id` | — | **204** sem corpo | `delete(id)` | 276-288 |

Detalhes:
- **Search**: `status` é convertido com `CompanyStatusEnum.valueOf(status.toUpperCase())` (`:244`) → valor inválido gera `IllegalArgumentException("No enum constant com.keepguard.ms_company.domain.enums.CompanyStatusEnum.XYZ")` → **400**. Os filtros `city`/`state` estão **quebrados** (seção 5.3) → 500.
- **Port não exposto**: `CompanyPort.getSimpleByTenantId` (`application/port/in/CompanyPort.java:48`) não tem endpoint. `CompanySimpleResponseDTO` e `CompanyAdapterMapper.toSimpleResponseDTO` (`ctrl/company/mapper/CompanyAdapterMapper.java:117-142`) não são usados por nenhum endpoint.
- **DTOs mortos** (não referenciados em `src/main`): `CompanySearchRequestDTO`, `CompanyStatusRequestDTO`, `CompanyDetailsResponseDTO`, `CompanySearchResponseDTO`, e os records `ctrl/company/dto/response/{Address,BankAccount,Cnae,Contact,Representative}ResponseDTO`. Não migrar.

**`CompanyCreateRequestDTO`** (`ctrl/company/dto/request/CompanyCreateRequestDTO.java:17-42`):

| campo | tipo | validação (mensagem) |
|---|---|---|
| `name` | string | `@NotBlank` "Nome fantasia é obrigatório"; `@Size(max=150)` "Nome fantasia deve ter no máximo 150 caracteres" |
| `legalName` | string | `@NotBlank` "Razão social é obrigatória"; `@Size(max=200)` "Razão social deve ter no máximo 200 caracteres" |
| `cnpj` | string | `@NotBlank` "CNPJ é obrigatório"; `@Pattern("^\d{14}$")` "CNPJ deve conter 14 dígitos" (+ dígito verificador no domínio) |
| `stateRegistration` | string? | `@Size(max=20)` "Inscrição estadual deve ter no máximo 20 caracteres" |
| `municipalRegistration` | string? | `@Size(max=20)` "Inscrição municipal deve ter no máximo 20 caracteres" |
| `taxRegime` | enum `TaxRegimeEnum` | `@NotNull` "Regime tributário é obrigatório" |
| `ein` | string? | `@Size(max=20)` "EIN deve ter no máximo 20 caracteres" |

**`CompanyUpdateRequestDTO`** (`.../CompanyUpdateRequestDTO.java:14-32`): todos opcionais — `name` (max 150), `legalName` (max 200), `stateRegistration` (max 20), `municipalRegistration` (max 20), `taxRegime` (enum), `ein` (max 20). Mesmas mensagens de `@Size`. **CNPJ não é atualizável** (nem existe no DTO). Semântica de PATCH: `null` = manter valor atual (`application/mapper/CompanyApplicationMapper.java:47-61`); string vazia/branca em `name`/`legalName` → 400 "Nome fantasia não pode ser vazio" / "Razão social não pode ser vazia" (`application/dto/company/CompanyUpdateCommandDTO.java:14-22`). Não é possível limpar `stateRegistration`/`municipalRegistration`/`ein` (null = manter).

**`CompanyMfaChannelRequestDTO`** (record, `.../CompanyMfaChannelRequestDTO.java:6-17`): `channel` (`MfaChannelEnum`, `@NotNull` "Canal de MFA é obrigatório"), `required` (boolean primitivo), `enabled` (boolean primitivo). Ausência de `required`/`enabled` no JSON → `false` [framework: Jackson usa o construtor canônico; o construtor de conveniência `(channel)` que põe `true,true` não é usado pelo Jackson]. A validação `@Valid` sobre `List<...>` (`CompanyController.java:269`) não é um `@RequestBody` de bean simples; elemento com `channel: null` termina em 500 (ou por `HandlerMethodValidationException` ou por NPE "Canal de MFA não pode ser nulo" em `dom/entity/CompanyMfaChannel.java:20`) — [framework].

**`CompanyResponseDTO`** (`ctrl/company/dto/response/CompanyResponseDTO.java:23-44`) — ordem dos campos no JSON:
```
id (UUID), codeCompany (UUID), tenantId (UUID), name, legalName, cnpj (14 dígitos),
stateRegistration, municipalRegistration,
address (AddressDTO|null), contacts ([ContactDTO]|null), representatives ([RepresentativeDTO]|null),
bankAccount (BankAccountDTO|null), taxRegime (enum), cnaes ([CnaeResponseDTO]|null),
mfaChannels ([{id, channel, required, enabled}]), ein, status (enum),
createdAt (LocalDateTime), updatedAt (LocalDateTime)
```
- Na prática `address`, `contacts`, `representatives`, `bankAccount`, `cnaes` **saem sempre `null`**: `CompanyApplicationMapper.toViewDTO` passa `null` (`application/mapper/CompanyApplicationMapper.java:83-88`) e nenhum service preenche depois.
- `mfaChannels` sempre lista (possivelmente `[]`), itens `CompanyMfaChannelResponseDTO {id, channel, required, enabled}` (`.../CompanyMfaChannelResponseDTO.java:15-21`). Ordem = ordem de carga do `@OneToMany` (não definida).
- Os tipos aninhados (para o caso hipotético de virem preenchidos) estão em `ctrl/company/dto/AddressDTO.java`, `BankAccountDTO.java`, `ContactDTO.java` (só `email, phone, website`), `RepresentativeDTO.java`; `cnaes` usa `ctrl/cnae/dto/response/CnaeResponseDTO`.

### 1.2 AddressController — `ctrl/address/AddressController.java` (base `/api/v1/addresses`, linha 30)

| Método | Path | Params | Body | Sucesso | Port | Linhas |
|---|---|---|---|---|---|---|
| POST | `/api/v1/addresses/company/{companyId}` | `companyId` | `AddressCreateRequestDTO` | **201** `AddressResponseDTO` | `AddressPort.create(companyId, cmd)` | 39-55 |
| PUT | `/api/v1/addresses/{id}` | `id` | `AddressUpdateRequestDTO` | 200 | `update` | 57-73 |
| PATCH | `/api/v1/addresses/{id}/activate` | `id` | — | 200 | `activate` | 75-88 |
| PATCH | `/api/v1/addresses/{id}/deactivate` | `id` | — | 200 | `deactivate` | 90-103 |
| GET | `/api/v1/addresses/{id}` | `id` | — | 200 | `getById` | 105-118 |
| GET | `/api/v1/addresses/company/{companyId}` | `companyId` | — | 200 `[AddressResponseDTO]` (cache) | `listByCompanyId` | 120-134 |
| GET | `/api/v1/addresses/company/{companyId}/active` | `companyId` | — | 200 `AddressResponseDTO` (cache) | `getActiveByCompanyId` | 136-149 |
| GET | `/api/v1/addresses` | `page`=0, `size`=20 | — | 200 `PageResultDTO<AddressResponseDTO>` | `search(criteria sem filtros, sort null, "ASC")` | 151-174 |
| GET | `/api/v1/addresses/search` | `companyId` UUID?, `city`, `state`, `zipCode`, `active` Boolean?, `page`, `size`, `sortFields`, `sortDirection` | — | 200 Page | `search` | 176-206 |
| GET | `/api/v1/addresses/all` | — | — | 200 `[AddressResponseDTO]` (sem paginação) | `listAll` | 208-221 |
| DELETE | `/api/v1/addresses/{id}` | `id` | — | **204** | `delete` | 223-235 |

`AddressCreateRequestDTO` (`ctrl/address/dto/request/AddressCreateRequestDTO.java:14-46`):
- `street` `@NotBlank` "Logradouro é obrigatório", `@Size(max=150)` "Logradouro deve ter no máximo 150 caracteres"
- `number` `@NotBlank` "Número é obrigatório", `@Size(max=20)` "Número deve ter no máximo 20 caracteres"
- `complement` `@Size(max=100)` "Complemento deve ter no máximo 100 caracteres"
- `district` `@NotBlank` "Bairro é obrigatório", `@Size(max=100)` "Bairro deve ter no máximo 100 caracteres"
- `city` `@NotBlank` "Cidade é obrigatória", `@Size(max=100)` "Cidade deve ter no máximo 100 caracteres"
- `state` `@NotBlank` "Estado é obrigatório", `@Size(min=2,max=2)` "Estado deve ter exatamente 2 caracteres" (minúsculo aceito aqui; domínio converte para maiúsculo e valida UF)
- `country` `@NotBlank` "País é obrigatório", `@Size(max=100)` "País deve ter no máximo 100 caracteres"
- `zipCode` `@NotBlank` "CEP é obrigatório", `@Size(min=8,max=8)` "CEP deve ter exatamente 8 dígitos" (domínio remove não-dígitos e exige 8 dígitos)

`AddressUpdateRequestDTO` (`.../AddressUpdateRequestDTO.java:13-38`): mesmos campos, todos opcionais, mesmas regras de `@Size`. `null` = manter; string só com espaços → 400 "<Campo> não pode ser vazio/vazia" (`application/dto/address/AddressUpdateCommandDTO.java:14-36`).

`AddressResponseDTO` (`ctrl/address/dto/response/AddressResponseDTO.java:14-27`): `id, companyId, street, number, complement, district, city, state, country, zipCode, active(boolean)`.

### 1.3 BankAccountController — `ctrl/bankaccount/BankAccountController.java` (base `/api/v1/bank-accounts`, linha 30)

| Método | Path | Params | Body | Sucesso | Port | Linhas |
|---|---|---|---|---|---|---|
| POST | `/api/v1/bank-accounts/company/{companyId}` | `companyId` | `BankAccountCreateRequestDTO` | **201** `BankAccountResponseDTO` | `create` | 39-55 |
| PUT | `/api/v1/bank-accounts/{id}` | `id` | `BankAccountUpdateRequestDTO` | 200 | `update` | 57-75 |
| PATCH | `/api/v1/bank-accounts/{id}/activate` | `id` | — | 200 | `activate` | 77-92 |
| PATCH | `/api/v1/bank-accounts/{id}/deactivate` | `id` | — | 200 | `deactivate` | 94-109 |
| GET | `/api/v1/bank-accounts/{id}` | `id` | — | 200 | `getById` | 111-126 |
| GET | `/api/v1/bank-accounts/company/{companyId}` | `companyId` | — | 200 lista (cache) | `listByCompanyId` | 128-144 |
| GET | `/api/v1/bank-accounts/company/{companyId}/active` | `companyId` | — | 200 (cache) | `getActiveByCompanyId` | 146-161 |
| GET | `/api/v1/bank-accounts` | `page`, `size` | — | 200 Page | `search` (sem filtros) | 163-189 |
| GET | `/api/v1/bank-accounts/search` | `companyId`?, `bankCode`, `accountType` (String), `active`?, `page`, `size`, `sortFields`, `sortDirection` | — | 200 Page | `search` | 191-223 |
| GET | `/api/v1/bank-accounts/all` | — | — | 200 lista | `listAll` | 225-240 |
| DELETE | `/api/v1/bank-accounts/{id}` | `id` | — | **204** | `delete` | 242-256 |

`BankAccountCreateRequestDTO` (`.../BankAccountCreateRequestDTO.java:17-40`):
- `code` `@NotBlank` "Código do banco é obrigatório", `@Pattern("^\d{3}$")` "Código do banco deve conter 3 dígitos" (+ whitelist no domínio)
- `agency` `@NotBlank` "Agência é obrigatória", `@Size(max=10)` "Agência deve ter no máximo 10 caracteres"
- `agencyDigit` `@Size(max=1)` "Dígito da agência deve ter 1 caractere"
- `accountNumber` `@NotBlank` "Número da conta é obrigatório", `@Size(max=20)` "Número da conta deve ter no máximo 20 caracteres"
- `accountDigit` `@NotBlank` "Dígito da conta é obrigatório", `@Size(max=1)` "Dígito da conta deve ter 1 caractere"
- `accountType` `AccountTypeEnum` `@NotNull` "Tipo da conta é obrigatório"

`BankAccountUpdateRequestDTO` (`.../BankAccountUpdateRequestDTO.java:15-33`): todos opcionais; `code` `@Pattern("^\d{3}$")`; demais `@Size` iguais. Branco → 400 "... não pode ser vazio/vazia" (`application/dto/bankaccount/BankAccountUpdateCommandDTO.java:14-30`, inclusive `agencyDigit` "Dígito da agência não pode ser vazio").

`BankAccountResponseDTO` (`.../response/BankAccountResponseDTO.java:15-26`): `id, companyId, code, agency, agencyDigit, accountNumber, accountDigit, accountType(enum), active`.

### 1.4 CnaeController — `ctrl/cnae/CnaeController.java` (base `/api/v1/companies/{companyId}/cnaes`, linha 28)

O `companyId` do path **só é usado no POST e nas listagens**; em update/activate/deactivate/set-principal/get/delete ele é ignorado (não há checagem de pertença).

| Método | Path | Body | Sucesso | Port | Linhas |
|---|---|---|---|---|---|
| POST | `/api/v1/companies/{companyId}/cnaes` | `CnaeCreateRequestDTO` | **201** `CnaeResponseDTO` | `CnaePort.create(companyId, cmd)` | 37-53 |
| PUT | `/api/v1/companies/{companyId}/cnaes/{id}` | `CnaeUpdateRequestDTO` | 200 | `update(id, cmd)` | 55-74 |
| PATCH | `.../cnaes/{id}/activate` | — | 200 | `activate(id)` | 76-92 |
| PATCH | `.../cnaes/{id}/deactivate` | — | 200 | `deactivate(id)` | 94-110 |
| PATCH | `.../cnaes/{id}/set-principal` | — | 200 | `setAsPrincipal(id)` | 112-128 |
| GET | `.../cnaes/{id}` | — | 200 | `getById(id)` | 130-146 |
| GET | `/api/v1/companies/{companyId}/cnaes` | — | 200 `[CnaeResponseDTO]` | `listByCompanyId` | 148-164 |
| GET | `.../cnaes/principal` | — | 200 | `getPrincipalByCompanyId` | 166-181 |
| GET | `.../cnaes/active` | — | 200 lista | `listActiveByCompanyId` | 183-199 |
| DELETE | `.../cnaes/{id}` | — | **204** | `delete(id)` | 201-216 |

`CnaeCreateRequestDTO` (`ctrl/cnae/dto/request/CnaeCreateRequestDTO.java:15-42`):
- `code` `@NotBlank` "Código CNAE é obrigatório", `@Pattern("^\d{7}$")` "Código CNAE deve conter 7 dígitos"
- `description` `@NotBlank` "Descrição do CNAE é obrigatória", `@Size(max=500)` "Descrição do CNAE deve ter no máximo 500 caracteres"
- `section` `@Size(max=1)` "Seção deve ter no máximo 1 caractere"; `division` max 2 "Divisão deve ter no máximo 2 caracteres"; `groupCode` max 3 "Código do grupo deve ter no máximo 3 caracteres"; `classCode` max 4 "Código da classe deve ter no máximo 4 caracteres"; `subclassCode` max 5 "Código da subclasse deve ter no máximo 5 caracteres"
- `principal` boolean (default `false`)

`CnaeUpdateRequestDTO` (`.../CnaeUpdateRequestDTO.java:15-39`): **`code` e `description` obrigatórios** (mesmas anotações), demais opcionais; **só `code` e `description` são efetivamente gravados** (seção 4.4).

`CnaeResponseDTO` (`ctrl/cnae/dto/response/CnaeResponseDTO.java:15-30`): `id, companyId, code, description, section, division, groupCode, classCode, subclassCode, active, principal, createdAt, updatedAt`.

### 1.5 CompanyPolicyController — `ctrl/companypolicy/CompanyPolicyController.java` (base `/api/v1/companies/{companyId}/policies`, linha 26)

| Método | Path | Params | Body | Sucesso | Port | Linhas |
|---|---|---|---|---|---|---|
| POST | `/api/v1/companies/{companyId}/policies` | `companyId` | `CreateCompanyPolicyRequestDTO` | **201** `CompanyPolicyResponseDTO` | `CompanyPolicyPort.create` | 35-53 |
| PUT | `.../policies/{policyId}` | `policyId` (companyId ignorado) | `UpdateCompanyPolicyRequestDTO` | 200 | `update` | 55-74 |
| DELETE | `.../policies/{policyId}` | `policyId`, **query obrigatório `updatedBy`** | — | **200 com corpo** `CompanyPolicyResponseDTO` (não 204) | `deactivate` | 76-94 |
| GET | `/api/v1/companies/{companyId}/policies` | — | — | 200 lista | `getPolicies` | 96-111 |
| GET | `.../policies/active` | — | — | 200 lista | `getActivePolicies` | 113-128 |

`CreateCompanyPolicyRequestDTO` (`.../CreateCompanyPolicyRequestDTO.java:18-39`):
- `code` `@NotBlank` "Código da política é obrigatório", `@Size(max=64)` "Código da política deve ter no máximo 64 caracteres"
- `description` `@NotBlank` "Descrição da política é obrigatória", `@Size(max=255)` "Descrição da política deve ter no máximo 255 caracteres"
- `status` `PolicyStatusEnum` `@NotNull` "Status da política é obrigatório"
- `effectiveFrom` `LocalDateTime` `@NotNull` "Data de início de vigência é obrigatória"
- `effectiveTo` `LocalDateTime` opcional
- `createdBy` `@NotBlank` "Usuário criador é obrigatório", `@Size(max=64)` "Usuário criador deve ter no máximo 64 caracteres"

`UpdateCompanyPolicyRequestDTO` (`.../UpdateCompanyPolicyRequestDTO.java:18-32`): `description` (NotBlank, max 255), `status` (NotNull), `effectiveTo` (opcional), `updatedBy` (`@NotBlank` "Usuário atualizador é obrigatório", `@Size(max=64)` "Usuário atualizador deve ter no máximo 64 caracteres").

`CompanyPolicyResponseDTO` (`.../response/CompanyPolicyResponseDTO.java:16-30`): `id, companyId, code, description, status, version (Integer), effectiveFrom, effectiveTo, createdAt, updatedAt, createdBy, updatedBy`.

Formato de `LocalDateTime` na entrada: ISO local (`"2026-01-01T00:00:00"`). Com `Z`/offset → erro de parse → 500 [framework].

### 1.6 ContactController — `ctrl/contact/ContactController.java` (base `/api/v1/contacts`, linha 30)

| Método | Path | Params | Body | Sucesso | Port | Linhas |
|---|---|---|---|---|---|---|
| POST | `/api/v1/contacts/company/{companyId}` | `companyId` | `ContactCreateRequestDTO` | **201** `ContactResponseDTO` | `create` | 39-56 |
| PUT | `/api/v1/contacts/{id}` | `id` | `ContactUpdateRequestDTO` | 200 | `update` | 58-75 |
| **PUT** | `/api/v1/contacts/{id}/activate` | `id` | — | 200 | `activate` | 77-90 |
| **PUT** | `/api/v1/contacts/{id}/deactivate` | `id` | — | 200 | `deactivate` | 92-105 |
| DELETE | `/api/v1/contacts/{id}` | `id` | — | **204** | `delete` | 107-119 |
| GET | `/api/v1/contacts/{id}` | `id` | — | 200 | `getById` | 121-134 |
| GET | `/api/v1/contacts/company/{companyId}` | — | — | 200 lista (cache) | `listByCompanyId` | 136-150 |
| GET | `/api/v1/contacts/company/{companyId}/active` | — | — | 200 lista (cache) | `listActiveByCompanyId` | 152-166 |
| GET | `/api/v1/contacts` | — | — | 200 lista **sem paginação** | `listAll` | 168-181 |
| GET | `/api/v1/contacts/search` | `companyId`?, `name`, `email`, `position`, `department`, `active`?, `page`, `size`, `sortFields`, `sortDirection` | — | 200 Page | `search` | 183-215 |

Atenção: activate/deactivate de **contato usam PUT**; nos outros recursos usam PATCH. Não há `/contacts/all`.

`ContactCreateRequestDTO` (`.../ContactCreateRequestDTO.java:16-40`):
- `name` `@NotBlank` "Nome é obrigatório", `@Size(max=100)` "Nome deve ter no máximo 100 caracteres"
- `email` `@NotBlank` "Email é obrigatório", `@Email` "Email deve ter formato válido", `@Size(max=150)` "Email deve ter no máximo 150 caracteres"
- `phone` `@NotBlank` "Telefone é obrigatório", `@Pattern("^[0-9\s\-\(\)\+]+$")` "Telefone deve conter apenas números, espaços, hífens, parênteses e sinal de mais", `@Size(min=10,max=20)` "Telefone deve ter entre 10 e 20 caracteres" (+ regex do domínio, mais restritiva, seção 3)
- `website` `@Size(max=150)`; `position` `@Size(max=100)` "Cargo deve ter no máximo 100 caracteres"; `department` `@Size(max=100)` "Departamento deve ter no máximo 100 caracteres"

`ContactUpdateRequestDTO` (`.../ContactUpdateRequestDTO.java:15-36`): todos opcionais, mesmas regras exceto `@NotBlank`; branco em `name`/`email`/`phone` → 400 "... não pode ser vazio" (`application/dto/contact/ContactUpdateCommandDTO.java:12-22`).

`ContactResponseDTO` (`.../response/ContactResponseDTO.java:14-25`): `id, companyId, name, email, phone, website, position, department, active`.

### 1.7 RepresentativeController — `ctrl/representative/RepresentativeController.java` (base `/api/v1/representatives`, linha 28)

| Método | Path | Params | Body | Sucesso | Port | Linhas |
|---|---|---|---|---|---|---|
| POST | `/api/v1/representatives/company/{companyId}` | `companyId` | `RepresentativeCreateRequestDTO` | **201** | `create` | 37-54 |
| PUT | `/api/v1/representatives/{id}` | `id` | `RepresentativeUpdateRequestDTO` (substituição total) | 200 | `update` | 56-72 |
| PATCH | `/api/v1/representatives/{id}/activate` | `id` | — | 200 | `activate` | 74-87 |
| PATCH | `/api/v1/representatives/{id}/deactivate` | `id` | — | 200 | `deactivate` | 89-102 |
| DELETE | `/api/v1/representatives/{id}` | `id` | — | **204** | `delete` | 104-116 |
| GET | `/api/v1/representatives/{id}` | `id` | — | 200 | `findById` | 118-131 |
| GET | `/api/v1/representatives` | — | — | 200 lista (todos) | `findAll` | 133-146 |
| GET | `/api/v1/representatives/company/{companyId}` | — | — | 200 lista (cache) | `findByCompanyId` | 148-162 |
| GET | `/api/v1/representatives/company/{companyId}/active` | — | — | 200 um item (o primeiro ativo; cache) | `findActiveByCompanyId` | 164-177 |
| GET | `/api/v1/representatives/active` | — | — | 200 lista | `findAllActive` | 179-192 |
| GET | `/api/v1/representatives/search/cpf/{cpf}` | `cpf` | — | 200 | `findByCpf` | 194-207 |
| GET | `/api/v1/representatives/search/email/{email}` | `email` | — | 200 | `findByEmail` | 209-222 |

`RepresentativeCreateRequestDTO` / `RepresentativeUpdateRequestDTO` (idênticos; `.../RepresentativeCreateRequestDTO.java:18-53`, `.../RepresentativeUpdateRequestDTO.java:18-53`):
- `name` `@NotBlank` "Nome é obrigatório", `@Size(max=150)` "Nome deve ter no máximo 150 caracteres"
- `cpf` `@NotBlank` "CPF é obrigatório", `@Pattern("^\d{11}$")` "CPF deve conter 11 dígitos"
- `rg` `@Size(max=15)` "RG deve ter no máximo 15 caracteres"
- `birthDate` `LocalDate` `@NotNull` "Data de nascimento é obrigatória", `@JsonFormat(pattern="yyyy-MM-dd")`
- `email` `@NotBlank` "Email é obrigatório", `@Email` "Email deve ter formato válido", `@Size(max=150)`
- `phone` `@NotBlank` "Telefone é obrigatório", `@Pattern("^\d{10,15}$")` "Telefone deve ter entre 10 e 15 dígitos"
- `role` `@Size(max=100)` "Cargo deve ter no máximo 100 caracteres"

`RepresentativeResponseDTO` (`.../response/RepresentativeResponseDTO.java:19-56`): `id, name, cpf, rg, birthDate ("yyyy-MM-dd"), email, phone, role, active (Boolean), createdAt, updatedAt` (os dois últimos com `@JsonFormat("yyyy-MM-dd'T'HH:mm:ss")`, mas **sempre `null`** — `application/mapper/RepresentativeApplicationMapper.java:74-75`). **Não tem `companyId`.**

### 1.8 HealthController — `ctrl/health/HealthController.java` (`/api/v1/health`, linha 37)

`GET /api/v1/health` (`:55-88`): faz `dataSource.getConnection().isValid(2)`.
- 200: `{"service":"ms-company","status":"UP","timestamp":"<LocalDateTime.now().toString()>","components":{"database":"UP"}}` (ordem de chaves indefinida — `HashMap`).
- 503: mesmo corpo com `"status":"DOWN"` e `"database":"DOWN"`.
A classe também implementa `HealthIndicator` (`:94-109`) → aparece em `/actuator/health` como componente `healthController` com details `service`, `database`. Não afeta os grupos `liveness`/`readiness` [framework].

### 1.9 HelperController — `ctrl/helper/HelperController.java` (`/api/v1/helper`, linha 17)

Só ativo com profile `dev`, `local` ou `test` (`@Profile`, `:19`) — ou seja, **ativo em prod se o profile for `local`** (seção 0, item 8).
- `GET /api/v1/helper/health` (`:23-42`) → 200 `{"status":"UP","service":"ms-company","timestamp":<epoch ms, número>,"message":"MS Company Service está funcionando normalmente"}`
- `GET /api/v1/helper/info` (`:44-65`) → 200 `{"service":"ms-company","version":"1.0.0-SNAPSHOT","description":"Company Management Service","java_version":"<System java.version>","spring_version":"3.3.2","port":"8083"}` (valores fixos no código; `spring_version` está desatualizado).

### 1.10 Endpoints de infraestrutura

- Actuator (`application.yml:77-95`): `/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness` (probes habilitadas, `show-details: always`), `/actuator/info`, `/actuator/prometheus`.
- Springdoc 2.3.0 (`pom.xml:90-94`, `application.yml:137-141`): `/v3/api-docs`, `/swagger-ui.html` (redireciona para `/swagger-ui/index.html`) [framework]. Info em `infra/config/SwaggerConfig.java:16-35` (título "KeepGuard Company API", versão "1.0.0", server `http://localhost:8083`).

---

## 2. Contrato de erro — `infra/rest/GlobalExceptionHandler.java`

`@RestControllerAdvice` com `@Order(HIGHEST_PRECEDENCE)` (`:23-24`). Todas as respostas são `ResponseEntity<ProblemDetail>` (classe do Spring 6, **não** da lib-common). Formato [código + framework]:

```json
{
  "type": "about:blank",
  "title": "<título fixo por exceção>",
  "status": 400,
  "detail": "<mensagem>",
  "instance": "/api/v1/...",          // [framework] preenchido pelo Spring com o request URI quando não setado
  "timestamp": "2026-10-03T15:00:00.123456789Z",   // Instant (UTC, ISO)
  "path": "/api/v1/...",              // request.getDescription(false) sem o prefixo "uri=" (sem query string)
  "errorCode": "<código>"
  // + campos extras em alguns casos (abaixo)
}
```
Content-Type `application/problem+json` [framework]. Saída indentada (INDENT_OUTPUT). Propriedades extras (`timestamp`, `path`, `errorCode`, ...) ficam **no nível raiz** (não dentro de `properties`) [framework].

| Exceção | Status | `title` | `detail` | `errorCode` | Extras | Linhas |
|---|---|---|---|---|---|---|
| `MethodArgumentNotValidException` (Bean Validation do `@RequestBody`) | 400 | "Dados de entrada inválidos" | `"<campo>: <mensagem>"` do **primeiro** `FieldError` (ordem não determinística entre múltiplos erros) ou "Dados de entrada inválidos" | `VALIDATION_ERROR` | — | 89-112 |
| `application.service.exception.NotFoundException` | 404 | "Recurso não encontrado" | mensagem da exceção | `ENTITY_NOT_FOUND` (o `errorCode` interno da exceção, ex. `COMPANY_NOT_FOUND`, é ignorado) | — | 114-131 |
| `AlreadyExistsException` | 409 | "Recurso já existe" | mensagem | `ENTITY_ALREADY_EXISTS` | — | 133-150 |
| `domain.exception.InvalidStatusForOperationException` e `application.service.exception.InvalidStatusForOperationException` | **403** | "Operação não permitida" | mensagem | `INVALID_STATUS_FOR_OPERATION` | — | 152-172 |
| `lib_common.exception.ValidationException` (estende `IllegalArgumentException`, `lib/exception/ValidationException.java:6`) | 400 | "Dados inválidos" | mensagem | `VALIDATION_ERROR` | — | 174-193 |
| `IllegalArgumentException` (inclui `NumberFormatException`, IAEs dos records de Command, `valueOf` de enum) | 400 | "Dados inválidos" | mensagem | `VALIDATION_ERROR` | — | 195-214 |
| `lib_common.exception.InvalidStatusException` (`lib/exception/InvalidStatusException.java`) | 400 | "Status inválido para operação" | mensagem | `INVALID_STATUS_TRANSITION` | `entityType`, `currentStatus`, `expectedStatus` (cada um só se não-nulo) | 216-246 |
| `UndeclaredThrowableException` | 400 se a causa é `ValidationException` (title "Dados inválidos", `VALIDATION_ERROR`); senão 500 ("Erro interno do servidor"/`INTERNAL_SERVER_ERROR`). NPE no próprio handler se `cause == null` (`:301`) | | | | | 248-323 |
| `CommandOperationException` | 500 | "Falha na operação de comando" | mensagem | `ex.errorCode` | `operation`, `context` (map) | 325-358 — **nunca lançada** no código atual |
| `QueryOperationException` | 404 se `getCause()` é `NotFoundException` (title "Recurso não encontrado", detail = msg da causa, `errorCode: RESOURCE_NOT_FOUND`); senão 500 (title "Falha na operação de consulta", detail = msg, `errorCode` = `ex.errorCode`, ex. `ADDRESS_QUERY_ERROR`) | | | | `operation` (ex. `"getById"`), `context` (map, ex. `{"addressId":"..."}`, ou `{"criteria":{...record...}}`) | 360-410 |
| `Exception` (qualquer outra, inclusive `RuntimeException` crua, `IllegalStateException`, `NullPointerException`, `DataIntegrityViolationException`, `FeignException`, `InvalidEmailException`) | 500 | "Erro interno do servidor" | **"Erro interno do servidor"** (mensagem original escondida) | `INTERNAL_SERVER_ERROR` | — | 412-441 |

Consequências [framework] do handler genérico com precedência máxima — viram **500 "Erro interno do servidor"**:
- JSON malformado, enum inválido no body, data em formato errado (`HttpMessageNotReadableException`);
- UUID inválido no path ou query (`MethodArgumentTypeMismatchException`), `page=abc`, `active=talvez`;
- query param obrigatório ausente (`DELETE .../policies/{id}` sem `updatedBy` → `MissingServletRequestParameterException`);
- rota inexistente (`NoResourceFoundException`), método HTTP errado (`HttpRequestMethodNotSupportedException`), content-type errado.

Exceções da lib que **não** estendem `IllegalArgumentException`: `InvalidEmailException extends RuntimeException` (`lib/exception/InvalidEmailException.java:3`) → 500 genérico (seção 12).

Exceções próprias do ms (todas `RuntimeException`, logam `log.error` + MDC no construtor):
- `svc/exception/NotFoundException.java` (errorCode default `NOT_FOUND`, msg default "Recurso não encontrado.")
- `svc/exception/AlreadyExistsException.java` (default `ALREADY_EXISTS`)
- `svc/exception/InvalidStatusForOperationException.java` (default `INVALID_STATUS`; factory `blockedOrSuspended` — não usada)
- `svc/exception/QueryOperationException.java` (message, operation, errorCode, context, cause)
- `svc/exception/CommandOperationException.java` (não usada)
- `dom/exception/InvalidStatusForOperationException.java` (só message)

Consumidor que depende do formato: bff-auth lê `detail`/`title` e um `errorCode` (`bff-auth/.../company_client.go:64-71`).

---

## 3. Domínio (`dom/`)

Entidades sem dependência de framework (só lib-common). IDs `UUID`; quando nulos na construção, são gerados com `UUID.randomUUID()` (v4) **no domínio**.

### 3.1 `Company` (agregado raiz) — `dom/entity/Company.java`
Campos (`:18-36`): `id` (final), `codeCompany` (final), `tenantId` (final), `name`, `legalName`, `cnpj`, `stateRegistration`, `municipalRegistration`, listas `addresses`, `contacts`, `representatives`, `bankAccounts`, `cnaes`, `mfaChannels`, `taxRegime`, `ein`, `status`, `createdAt`, `updatedAt` (`LocalDateTime`).
- Construtor (`:38-54`): `id/codeCompany/tenantId` = random se null; `name`/`legalName`/`cnpj` validados; `taxRegime` default `SIMPLES_NACIONAL`; `status` default `PENDING_APPROVAL`.
- Factories: `create(...)` (`:56-61`) → status `PENDING_APPROVAL`, `createdAt=updatedAt=now()`; `of(...)` (`:63-68`) reidrata.
- Validações:
  - `name` (`:70-78`): null/branco → `IllegalArgumentException("Nome fantasia é obrigatório")`; `length > 150` (medido **antes** do trim) → IAE "Nome fantasia deve ter no máximo 150 caracteres"; retorna `trim()`.
  - `legalName` (`:80-88`): "Razão social é obrigatória" / "Razão social deve ter no máximo 200 caracteres"; trim.
  - `cnpj` (`:90-97`): null/branco → `ValidationException("CNPJ é obrigatório")`; remove não-dígitos; `BrazilianValidationUtils.validateCnpj` (seção 3.9); retorna só dígitos.
- Comportamento / transições de status (todas setam `updatedAt=now()`):

| Método | Origem permitida | Destino | Erro (exceção → HTTP) | Linhas |
|---|---|---|---|---|
| `approve()` | `PENDING_APPROVAL` | `ACTIVE` | outro status → `InvalidStatusException.invalidTransition("Empresa", status, "PENDING_APPROVAL")` → 400 "Empresa com status 'X' não pode realizar esta operação. Status esperado: 'PENDING_APPROVAL'" (com `expectedStatus`). Depois `validateRequiredDataForApproval()` → `ValidationException` 400 (abaixo) | 136-146 |
| `reject()` | `PENDING_APPROVAL` | `BLOCKED` | igual ao approve | 176-182 |
| `activate()` | `INACTIVE` | `ACTIVE` | `PENDING_APPROVAL` → `invalidOperation("Empresa", "PENDING_APPROVAL", "ativada - deve ser aprovada primeiro")` → 400 "Empresa com status 'PENDING_APPROVAL' não pode ser ativada - deve ser aprovada primeiro" (sem `expectedStatus`); outros → invalidTransition esperado `INACTIVE` | 184-193 |
| `deactivate()` | `ACTIVE` | `INACTIVE` | PENDING → "... não pode ser desativada - deve ser aprovada primeiro"; outros → esperado `ACTIVE` | 195-204 |
| `suspend()` | `ACTIVE` | `SUSPENDED` | PENDING → "... não pode ser suspensa - deve ser aprovada primeiro"; outros → esperado `ACTIVE` | 206-215 |
| `block()` | qualquer (inclusive BLOCKED) | `BLOCKED` | — | 217-220 |

  Consequência: `SUSPENDED` só sai via `block()`; `BLOCKED` é terminal.
- `validateRequiredDataForApproval()` (`:148-174`), na ordem, cada um `ValidationException`:
  1. sem endereço ativo → "Empresa deve ter pelo menos um endereço ativo para ser aprovada"
  2. sem conta ativa → "Empresa deve ter pelo menos uma conta bancária ativa para ser aprovada"
  3. sem CNAE principal **e** ativo → "Empresa deve ter pelo menos um CNAE ativo e principal para ser aprovada"
  4. sem contato ativo → "Empresa deve ter pelo menos um contato ativo para ser aprovada"
  5. sem representante ativo → "Empresa deve ter pelo menos um representante ativo para ser aprovada"
  (A exigência de política ativa fica no service, antes — seção 4.1.)
- `isActive()` (`:222-224`), `isBlockedOrSuspended()` (`:226-229`).
- `validateStatusForOperations()` (`:231-238`): se `BLOCKED` ou `SUSPENDED` → `dom.exception.InvalidStatusForOperationException` (**403**) com mensagem exata: `"Não é possível realizar operações na empresa com status '" + status.getDescription() + "'. Operações são permitidas apenas para empresas com status Ativa, Inativa ou Aguardando Aprovação."` (ex. `'Bloqueada'`, `'Suspensa'`).
- Gestão de filhos (em memória; só usados ao carregar do banco e na regra de aprovação):
  - `addAddress` (`:241-248`): se o novo é ativo, desativa todos os anteriores (endereço ativo exclusivo).
  - `addContact` / `addRepresentative`: múltiplos ativos permitidos.
  - `addBankAccount` (`:294-301`): conta ativa exclusiva.
  - `addCnae` (`:308-318`): se principal, `unsetAsPrincipal` nos outros principais.
  - `getActiveRepresentatives` ordena por `id` (`:282-291`; não usado em endpoint).
  - `setPrincipalCnae`, `removeCnae`, `getActiveCnaes`, `getSecondaryCnaes` (não usados pelos services).
- `addMfaChannel` (`:120-125`): substitui canal de mesmo tipo. `setMfaChannels` (`:127-133`): limpa e adiciona (sem dedupe).
- `updateBasicInfo`, `updateTaxRegime` (`:345-361`): não usados pelos services (update usa `Company.of`, seção 4.1).
- `equals/hashCode` por `id`.

### 3.2 `Address` — `dom/entity/Address.java`
Campos: `id, street, number, complement, district, city, state, country, zipCode, active`. `create(...)` → `active=true` (`:36-39`).
Validações (todas medem `length` **antes** do trim e retornam `trim()`):
- `street` (`:46-54`): obrigatório "Logradouro é obrigatório"; max 150 "Logradouro deve ter no máximo 150 caracteres" (IAE)
- `number` (`:56-64`): "Número é obrigatório"; max 20
- `complement` (`:66-78`): null ou branco → **null**; trim; max 100 (medido depois do trim)
- `district` (`:80-88`): "Bairro é obrigatório"; max 100
- `city` (`:90-98`): "Cidade é obrigatória"; max 100
- `state` (`:100-107`): branco → `ValidationException("Estado é obrigatório")`; `trim().toUpperCase()`; `validateState` (UF da lista de 27)
- `country` (`:109-117`): "País é obrigatório"; max 100
- `zipCode` (`:119-126`): branco → `ValidationException("CEP é obrigatório")`; remove não-dígitos; `validateCep`
`activate()/deactivate()` só trocam o flag.

### 3.3 `BankAccount` — `dom/entity/BankAccount.java`
Campos: `id, code, agency, agencyDigit, accountNumber, accountDigit, accountType, active`. `create` → ativo.
- `code` (`:43-50`): branco → `ValidationException("Código do banco é obrigatório")`; remove não-dígitos; `validateBankCode` (whitelist).
- `agency` (`:52-60`): "Agência é obrigatória"; max 10; trim.
- `agencyDigit` (`:62-70`): null/branco → **null**; `length > 1` → "Dígito da agência deve ter 1 caractere".
- `accountNumber` (`:72-80`): "Número da conta é obrigatório"; max 20.
- `accountDigit` (`:82-90`): "Dígito da conta é obrigatório"; length > 1 → "Dígito da conta deve ter 1 caractere".
- `accountType`: `Objects.requireNonNull(..., "Tipo da conta é obrigatório")` → NPE (500) se null (`:29`).

### 3.4 `Cnae` — `dom/entity/Cnae.java`
Campos: `id, code, description, section, division, groupCode, classCode, subclassCode, active, principal, companyId (obrigatório — NPE "ID da empresa é obrigatório"), createdAt, updatedAt`. `create` → `active=true`, timestamps now.
- `code` (`:58-65`): branco → `ValidationException("Código CNAE é obrigatório")`; remove não-dígitos; `validateCnae` (7 dígitos + estrutura, seção 3.9).
- `description` (`:67-75`): "Descrição do CNAE é obrigatória"; max 500 (IAE); trim.
- `deactivate()` (`:98-104`): se `principal` → **`IllegalStateException("Não é possível desativar o CNAE principal. Defina outro como principal primeiro.")`** → 500 (mensagem escondida).
- `setAsPrincipal()` (`:106-110`): `principal=true` **e** `active=true`.
- `unsetAsPrincipal()`, `activate()`, `updateDescription`, `updateCode`.

### 3.5 `Contact` — `dom/entity/Contact.java`
Campos: `id, name, email, phone, website, position, department, active`.
- `name` (`:43-51`): "Nome é obrigatório"; max 100.
- `email` (`:53-60`): branco → `ValidationException("Email é obrigatório")`; `trim().toLowerCase()`; `ValidationUtils.validateEmail` → lança **`InvalidEmailException`** (500) com "Formato de email inválido: x", "Domínio de email muito curto: d", "Domínio de email inválido: d", "Email muito longo (máximo 254 caracteres)", "Parte local do email muito longa (máximo 64 caracteres)" (`lib/utils/ValidationUtils.java:42-93`; regex `^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$`).
- `phone` (`:62-68`): branco → `ValidationException("Telefone é obrigatório")`; `validatePhone(phone)` (valor **sem** trim é testado contra regex após trim interno); retorna `trim()`.
- `website`/`position`/`department` (`:70-89`): max 150/100/100 (IAE "Website deve ter no máximo 150 caracteres", "Cargo deve ter no máximo 100 caracteres", "Departamento deve ter no máximo 100 caracteres"); trim; **string vazia continua `""`** (não vira null).

### 3.6 `Representative` — `dom/entity/Representative.java`
Campos: `id, name, cpf, rg, birthDate (LocalDate), email, phone, role, active`.
- `name` (`:46-54`): "Nome do representante é obrigatório"; max 150 "Nome deve ter no máximo 150 caracteres".
- `cpf` (`:56-63`): branco → `ValidationException("CPF é obrigatório")`; só dígitos; `validateCpf`.
- `rg` (`:65-73`): branco → null; max 15 "RG deve ter no máximo 15 caracteres".
- `birthDate` (`:75-86`): null → "Data de nascimento é obrigatória"; `> hoje` → "Data de nascimento não pode ser futura"; `< hoje - 120 anos` → "Data de nascimento inválida" (IAE; usa `LocalDate.now()` do fuso da JVM).
- `email` (`:88-95`): "Email do representante é obrigatório"; lower+trim; `validateEmail` (`InvalidEmailException` → 500).
- `phone` (`:97-103`): "Telefone do representante é obrigatório"; `validatePhone`.
- `role` (`:105-113`): branco → null; max 100 "Cargo deve ter no máximo 100 caracteres".

### 3.7 `CompanyPolicy` — `dom/entity/CompanyPolicy.java`
Campos: `id, companyId (obrigatório — NPE "Company ID não pode ser nulo"), code, description, status, version (Integer), effectiveFrom, effectiveTo, createdAt, updatedAt, createdBy, updatedBy` + lista interna `domainEvents` (nunca publicada).
- Construtor (`:31-47`): `status` default `INACTIVE`; `version` default 1; `effectiveFrom` default `now()`.
- `create(...)` (`:49-57`): `version=1`, `createdAt=updatedAt=now()`, `updatedBy=createdBy`; registra evento "CompanyPolicyCreated".
- `code` (`:113-121`): branco → `ValidationException("Código da política não pode ser nulo ou vazio")`; `length > 64` → "Código da política não pode ter mais de 64 caracteres"; trim.
- `description` (`:123-131`): "Descrição da política não pode ser nula ou vazia"; > 255 "Descrição da política não pode ter mais de 255 caracteres".
- `updateDescription`, `updateStatus` (NPE "Status não pode ser nulo"), `updateEffectiveTo`, `incrementVersion` (+1), `deactivate(updatedBy)` → `INACTIVE`. Todos atualizam `updatedBy`, `updatedAt`.
- `isActive()`, `isEffective()` (não usado).

### 3.8 `CompanyMfaChannel` — `dom/entity/CompanyMfaChannel.java`
`id, channel (obrigatório, NPE "Canal de MFA não pode ser nulo"), required, enabled, createdAt, updatedAt`. `create(channel, required, enabled)`; `update(required, enabled)` (não usado).

### 3.9 Validadores brasileiros (lib) — `lib/utils/BrazilianValidationUtils.java`
Todos lançam `ValidationException` (400):
- **CNPJ** (`:40-60`): branco "CNPJ não pode ser nulo ou vazio"; remove `\D`; `^\d{14}$` senão "CNPJ deve conter exatamente 14 dígitos"; todos iguais "CNPJ inválido: todos os dígitos são iguais"; DV com pesos `5,4,3,2,9,8,7,6,5,4,3,2` e `6,5,4,3,2,9,8,7,6,5,4,3,2`, dígito = `soma%11 < 2 ? 0 : 11 - soma%11`, senão "CNPJ inválido: dígitos verificadores incorretos" (`:222-240`).
- **CPF** (`:68-88`): mensagens análogas ("CPF não pode ser nulo ou vazio", "CPF deve conter exatamente 11 dígitos", "CPF inválido: todos os dígitos são iguais", "CPF inválido: dígitos verificadores incorretos"); pesos 10..2 e 11..2 (`:242-260`).
- **CEP** (`:96-111`): "CEP não pode ser nulo ou vazio", "CEP deve conter exatamente 8 dígitos", "CEP inválido: todos os dígitos são iguais".
- **Telefone** (`:119-129`): `trim()` e regex `^\(?[1-9][1-9]\)?\s?[1-9][0-9]{3,4}-?[0-9]{4}$`; senão "Formato de telefone inválido. Use: (XX) XXXXX-XXXX ou (XX) XXXX-XXXX". Não aceita `+55`, nem DDD com 0, nem número começando com 0.
- **UF** (`:137-147`): lista `AC AL AP AM BA CE DF ES GO MA MT MS MG PA PB PR PE PI RJ RN RS RO RR SC SP SE TO`; senão `"Estado inválido: " + state + ". Use uma das siglas válidas: " + Set.toString()` (ordem do `Set.of` **não determinística**).
- **Banco** (`:155-165`): whitelist `001, 033, 104, 237, 341, 356, 422, 748, 756`; senão `"Código bancário inválido: X. Códigos válidos: [..]"` (mesma questão de ordem).
- **CNAE** (`:173-188`, estrutura `:262-291`): remove `\D`; `^\d{7}$` senão "Código CNAE deve conter exatamente 7 dígitos"; 1º dígito 1..9, dígitos 2-3 em 01..99, dígitos 4-6 em 001..999, 7º 0..9; senão "Código CNAE inválido: estrutura hierárquica incorreta". **CNAEs reais começando com 0 (ex. 0111301) são rejeitados.**

### 3.10 Enums — `dom/enums/`
Todos têm `description` (não serializada; só aparece em mensagens). Persistidos como `STRING` (nome).
- `CompanyStatusEnum`: `ACTIVE("Ativa")`, `INACTIVE("Inativa")`, `PENDING_APPROVAL("Aguardando Aprovação")`, `SUSPENDED("Suspensa")`, `BLOCKED("Bloqueada")`.
- `TaxRegimeEnum`: `SIMPLES_NACIONAL("Simples Nacional")`, `LUCRO_PRESUMIDO("Lucro Presumido")`, `LUCRO_REAL("Lucro Real")`.
- `AccountTypeEnum`: `CORRENTE("Conta Corrente")`, `POUPANCA("Conta Poupança")`, `PJ("Conta Pessoa Jurídica")`.
- `PolicyStatusEnum`: `ACTIVE("Ativa")`, `INACTIVE("Inativa")`.
- `MfaChannelEnum`: `EMAIL("E-mail")`, `SMS("SMS")`, `WHATSAPP("WhatsApp")`, `AUTHENTICATOR_APP("App Autenticador (TOTP)")`.
- `MfaPolicyEnum`: `NONE`, `EMAIL_ONLY`, `SMS_ONLY`, `COMBINED_EMAIL_SMS` — **não usado**.

---

## 4. Application (CQRS)

### 4.0 Padrão
- `*Port` (`application/port/in/`) = interface de entrada usada pelos controllers.
- `*UseCaseService` (`svc/<ctx>/*UseCaseService.java`) implementa o Port e **só delega**: comandos → `*CommandService`, consultas → `*QueryService` (ex. `svc/company/CompanyUseCaseService.java:28-108`). Sem lógica, sem transação.
- `*CommandService`: escrita; anotado com `@LogOperation` da lib por método (log estruturado + métrica `operations_total` + evento de auditoria RabbitMQ — seção 8.3). Transação: `@Transactional` **na classe** em Address/BankAccount/Cnae/Contact/Representative; **por método** em CompanyPolicy; **ausente** em Company (o `@Transactional` em `saveCompany` privado, `svc/company/CompanyCommandService.java:74-77`, não tem efeito — proxy Spring não intercepta método privado/auto-invocação).
- `*QueryService`: leitura, `@Transactional(readOnly = true)` na classe (exceto CompanyPolicy, por método). Cache Redis em Company, Address, BankAccount, Contact, Representative (CNAE e Policy sem cache).
- Mappers de aplicação (`application/mapper/*ApplicationMapper`): Command→Domínio e Domínio→View; no update fazem *merge* "null = manter valor atual".
- Isolamento por tenant: **não existe**. Nenhum service filtra por tenant/empresa do chamador; qualquer id de qualquer empresa é acessível. O `companyId` do path só é usado para associar/listar.
- `open-in-view` não configurado → padrão `true` [framework]: a sessão Hibernate fica aberta durante o request, o que permite o lazy loading nos command services sem transação.

### 4.1 Company

`CompanyPort` (`application/port/in/CompanyPort.java:14-51`): `create(CompanyCreateCommandDTO)`, `update(UUID, CompanyUpdateCommandDTO)`, `approve`, `reject`, `activate`, `deactivate`, `suspend`, `block`, `updateMfaChannels(UUID, List<CompanyMfaChannelCommandDTO>)`, `delete(UUID)`, `getById`, `getByCodeCompany(UUID)`, `getByCnpj(String)`, `getByTenantId(UUID)`, `getSimpleByTenantId(UUID)`, `search(CompanySearchCriteriaDTO)` → `PageResultDTO<CompanyViewDTO>`.

DTOs: `CompanyCreateCommandDTO` (valida name/legalName/cnpj não brancos e taxRegime não nulo com IAE, `application/dto/company/CompanyCreateCommandDTO.java:15-28`), `CompanyUpdateCommandDTO`, `CompanyMfaChannelCommandDTO(channel, required, enabled)`, `CompanyViewDTO` (valida id, codeCompany, tenantId, name, legalName, cnpj, taxRegime, status não nulos — `CompanyViewDTO.java:37-64`), `CompanyMfaChannelViewDTO(id, channel, required, enabled)`, `CompanySimpleViewDTO`, `CompanySearchCriteriaDTO`.

**`CompanyCommandService`** (`svc/company/CompanyCommandService.java`):
- `create` (`:38-72`, `@LogOperation CREATE_COMPANY / audit CREATE / COMPANY`):
  1. `BrazilianValidationUtils.validateCnpj(command.cnpj())`; em falha incrementa `company_business_errors_total{error_code=INVALID_CNPJ, operation=create}` e relança (400).
  2. `existsByCnpj(command.cnpj())` (CNPJ cru do request) → se existe: `company_business_errors_total{error_code=CNPJ_ALREADY_EXISTS, operation=create}` + `AlreadyExistsException("CNPJ já cadastrado: <cnpj>")` → **409**.
  3. `Company.create(...)` (gera id, codeCompany, tenantId, status PENDING_APPROVAL; valida tudo) e `companyRepository.save` (sem transação englobando o resto).
  4. `authRoleProvisionPort.provisionCompanyRoles(savedCompany.getId())` (HTTP síncrono ao ms-auth, seção 7). Falha → exceção sobe (500), **empresa já gravada fica**.
  5. `company_created_total{entity_id=<id>}`.
  6. Retorna view (sem cache write/evict).
- `update` (`:79-113`): `findById` (404 "Empresa não encontrada: <id>" + `company_not_found_total{entity_id, operation=update}`); `validateStatusForOperations()` (403); `companyMapper.toDomain(command, existing)` cria **novo** `Company.of(...)` com merge null=manter, **sem filhos e sem mfaChannels**, `updatedAt=now()`; `save`; `clearAllCompanyCache(id, cnpj, codeCompany, tenantId)`; `company_updated_total{entity_id}`.
- `approve` (`:115-153`): `findById` (404 + métrica op=approve) → **`companyPolicyRepository.existsActivePolicyByCompanyId(id)`** falso → `company_business_errors_total{error_code=NO_ACTIVE_POLICY, operation=approve}` + `ValidationException("Empresa deve ter pelo menos uma política ativa para ser aprovada")` (400) → `company.approve()` (status + dados obrigatórios, seção 3.1) → `save` → `clearAllCompanyCache` → `company_approved_total`. Note a ordem: a checagem de política vem **antes** da checagem de status.
- `reject` / `activate` / `deactivate` / `suspend` / `block` (`:155-348`): `findById` (404 + `company_not_found_total{operation=<op>}`) → método de domínio → `save` → `clearAllCompanyCache` → `company_<rejected|activated|deactivated|suspended|blocked>_total{entity_id}`. **Não chamam** `validateStatusForOperations` (só as regras de transição).
- `updateMfaChannels` (`:254-282`, audit action UPDATE): `findById` (404 sem métrica) → converte cada item em `CompanyMfaChannel.create` → `company.setMfaChannels(list)` → `save` (o adapter faz o diff por canal, seção 5.4) → `clearAllCompanyCache`. **Sem** checagem de status e sem métrica. Lista vazia remove todos os canais.
- `delete` (`:350-372`): `findById` (404 + métrica) → `deleteById` (hard delete, cascade JPA dos filhos) → `clearAllCompanyCacheById(id)` (não limpa chaves por cnpj/code/tenant) → `company_deleted_total`.

**`CompanyQueryService`** (`svc/company/CompanyQueryService.java`, readOnly):
- `getById` (`:33-70`): cache `company_cache:id:<id>` → hit: `company_queries_total{query_type=GET_BY_ID,status=CACHE_HIT}`; miss: `findById` (404 "Empresa não encontrada: <id>" + `company_not_found_total{entity_id, operation=get_by_id}`), mapeia, grava cache, `company_queries_total{GET_BY_ID,SUCCESS}`. Qualquer outra exceção → `company_queries_total{...,ERROR}` + `RuntimeException("Erro interno ao buscar empresa por ID")` → 500.
- `getByCodeCompany` (`:72-109`): idem com chave `code`, `query_type=GET_BY_CODE_COMPANY`, `operation=get_by_code_company`.
- `getByCnpj` (`:111-148`): idem com chave `cnpj` (só dígitos) — mas a busca no banco usa o **valor cru do path** (`findByCnpj(cnpj)`), então CNPJ formatado (`11.222.333/0001-81`) só funciona se já estiver em cache.
- `getByTenantId` (`:176-220`): cache `tenantId` → hit retorna direto (sem checar status); miss: `findByTenantId` (404 "Empresa não encontrada: <tenantId>") → **se `status != ACTIVE`**: `company_invalid_status_total{entity_id, status, operation=get_by_tenant_id}` + `NotFoundException("Empresa não está ativa: <tenantId>")` → **404** → grava cache → `GET_BY_TENANT_ID SUCCESS`.
- `getSimpleByTenantId` (`:222-267`): igual, com cache `simple:tenantId` e `CompanySimpleViewDTO` (sem endpoint).
- `search` (`:150-173`): `companyRepository.search(criteria)` → map → `company_queries_total{SEARCH,SUCCESS}`; erro → `RuntimeException("Erro interno ao buscar empresas")` → 500. Sem cache.

`CompanyApplicationMapper` (`application/mapper/CompanyApplicationMapper.java`): `toDomain(create)` = `Company.create`; `toDomain(update, existing)` (`:41-66`) = merge (CNPJ mantido); `toViewDTO` (`:68-102`) → filhos `null`, `mfaChannels` mapeados; `toSimpleViewDTO`.

### 4.2 Address

`AddressPort` (`application/port/in/AddressPort.java:12-37`): `create(companyId, cmd)`, `update(id, cmd)`, `activate`, `deactivate`, `delete`, `getById`, `listByCompanyId`, `getActiveByCompanyId`, `listAll`, `search(AddressSearchCriteriaDTO)`.

`AddressCommandService` (`svc/address/AddressCommandService.java`, `@Transactional`):
- `create` (`:33-71`): `companyRepository.findById(companyId)` (404 "Empresa não encontrada: <companyId>" + `address_business_errors_total{error_code=COMPANY_NOT_FOUND, operation=create}`) → `validateStatusForOperations` (403) → se há endereço ativo: `deactivate()` + `addressRepository.save(activeAddress)` (overload **sem** companyId — ver risco seção 12) → `Address.create` (ativo) → `save(address, companyId)` → `company.addAddress(saved)` + `companyRepository.save(company)` (só atualiza escalares/MFA) → `address_created_total{entity_id, company_id}` → view com companyId. **Sem invalidação de cache.**
- `update` (`:73-104`): `findById` (404 "Endereço não encontrado: <id>" + `address_not_found_total{entity_id, operation=update}`) → `findCompanyIdByAddressId` (404 "Empresa não encontrada para o endereço: <id>") → `companyRepository.findById` (404) → `validateStatusForOperations` (403) → merge null=manter (`AddressApplicationMapper.java:38-61`, mantém `active`) → `save(updated, companyId)` → `address_updated_total`.
- `activate` (`:106-139`): `findById` (404 + métrica) → companyId → desativa o outro ativo (se id diferente) com save sem companyId → `activate()` → save → `address_activated_total`. **Sem checagem de status da empresa.**
- `deactivate` (`:141-165`): idem, `address_deactivated_total`. Sem checagem de status.
- `delete` (`:167-186`): `existsById` (404 + métrica) → `deleteById` → `address_deleted_total`. Sem checagem de status.

`AddressQueryService` (`svc/address/AddressQueryService.java`, readOnly):
- `getById` (`:33-59`): `findById` (404 "Endereço não encontrado: <id>"; errorCode interno `ADDRESS_NOT_FOUND`) → `address_queries_total{GET_BY_ID,SUCCESS}` → `findCompanyIdByAddressId` → view. Outros erros → `QueryOperationException("Falha ao buscar endereço","getById","ADDRESS_QUERY_ERROR",{addressId})` + `address_system_errors_total{error_type=GET_ADDRESS_BY_ID_ERROR, operation=get_by_id}`.
- `listByCompanyId` (`:61-92`): cache `address_cache:company:<id>` (hit → `address_queries_total{LIST_BY_COMPANY,CACHE_HIT,count}`); miss → `findByCompanyId` → views → grava cache (inclusive lista vazia) → `{...,SUCCESS,count}`. Não verifica se a empresa existe. Erro → `QueryOperationException(...,"listByCompanyId","ADDRESS_QUERY_ERROR",{companyId})`.
- `getActiveByCompanyId` (`:94-132`): cache `...:active`; miss → `findActiveByCompanyId` (404 "Endereço ativo não encontrado para a empresa: <id>" + `address_not_found_total{company_id, operation=get_active_by_company}`) → grava cache.
- `listAll` (`:134-156`): `findAll` + 1 query por item para companyId (N+1). Se algum endereço não tiver empresa → NotFound embrulhado em QueryOperationException → 404 `RESOURCE_NOT_FOUND`.
- `search` (`:158-185`): repositório + companyId por item; métrica `address_queries_total{SEARCH,SUCCESS,count=<total>}`.

### 4.3 BankAccount
Estrutura idêntica a Address (`svc/bankaccount/BankAccountCommandService.java`, `BankAccountQueryService.java`), com:
- Mensagens: "Dados bancários não encontrados: <id>", "Empresa não encontrada para os dados bancários: <id>", "Dados bancários ativos não encontrados para a empresa: <companyId>".
- Métricas: `bank_account_business_errors_total`, `bank_account_not_found_total`, `bank_account_created_total{entity_id,company_id}`, `bank_account_updated_total`, `bank_account_activated_total`, `bank_account_deactivated_total`, `bank_account_deleted_total`, `bank_account_queries_total`, `bank_account_system_errors_total`.
- `create` desativa a conta ativa anterior (conta ativa exclusiva) — `BankAccountCommandService.java:52-57`.
- `getById` NotFound sem errorCode próprio (`BankAccountQueryService.java:40`).
- `search` embrulha erro em `RuntimeException("Erro ao buscar dados bancários com critérios")` (`:184`) → **500 mesmo para NotFound** (diferente de Address/Contact).
- Merge de update: `BankAccountApplicationMapper.java:36-57`.

### 4.4 CNAE
`CnaePort` (`application/port/in/CnaePort.java:10-37`): `create(companyId, cmd)`, `update(id, cmd)`, `activate`, `deactivate`, `setAsPrincipal`, `delete`, `getById`, `listByCompanyId`, `listActiveByCompanyId`, `getPrincipalByCompanyId`, `listAll` (sem endpoint).
`CnaeCreateCommandDTO`/`CnaeUpdateCommandDTO` fazem `trim` de todos os campos (`application/dto/cnae/*.java`).

`CnaeCommandService` (`svc/cnae/CnaeCommandService.java`, `@Transactional`):
- `create` (`:34-91`): empresa existe (404 "Empresa não encontrada: <id>" + `cnae_business_errors_total{COMPANY_NOT_FOUND,create}`) → `validateStatusForOperations` (403) → `existsByCompanyIdAndCode(companyId, command.code())` → **`RuntimeException("CNAE já existe para esta empresa: <code>")` → 500** (+ métrica `CNAE_ALREADY_EXISTS`) → se `principal`: busca todos da empresa, `unsetAsPrincipal` + save em cada principal → `Cnae.create(..., active=true)` → save → `cnae_created_total{entity_id, company_id}`.
- `update` (`:93-130`): `findById` → não achou: **`RuntimeException("CNAE não encontrado: <id>")` → 500** (+ `cnae_business_errors_total{CNAE_NOT_FOUND,update}`) → empresa (404) + `validateStatusForOperations` (403) → se código mudou e já existe → `RuntimeException("Código CNAE já existe para esta empresa: <code>")` (500) → `updateCode` + `updateDescription` (**section/division/groupCode/classCode/subclassCode do request são ignorados**) → save → `cnae_updated_total`.
- `activate` (`:132-154`, audit action UPDATE): não achou → 500; `activate()`; sem checagem de status.
- `deactivate` (`:156-178`): não achou → 500; principal → `IllegalStateException` → 500.
- `setAsPrincipal` (`:180-212`): não achou → 500; desmarca todos os principais da empresa (ativos ou não) → `setAsPrincipal()` (também ativa) → `cnae_set_principal_total`.
- `delete` (`:214-241`): não achou → 500; `countActiveByCompanyId <= 1 && cnae.isActive()` → `RuntimeException("Não é possível remover o último CNAE ativo da empresa")` → 500 (+ métrica `CANNOT_DELETE_LAST_ACTIVE_CNAE`); senão `deleteById` → `cnae_deleted_total`.

`CnaeQueryService` (`svc/cnae/CnaeQueryService.java`, readOnly, **sem cache**): `getById` (não achou → `RuntimeException` → 500, `cnae_query_errors_total{CNAE_NOT_FOUND,get_by_id}`), `listByCompanyId`, `getPrincipalByCompanyId` (principal **e** ativo; não achou → 500 "CNAE principal não encontrado para empresa: <id>"), `listActiveByCompanyId`, `listAll`, mais métodos sem uso (`listAllActive`, `existsById`, `existsByCompanyIdAndCode`, `findByCompanyIdAndCode`, `countActiveByCompanyId`). Métrica `cnae_queried_total` com tags variáveis.

### 4.5 CompanyPolicy
`CompanyPolicyPort` (`application/port/in/CompanyPolicyPort.java:12-23`): `create(CreateCompanyPolicyCommandDTO)`, `update(UpdateCompanyPolicyCommandDTO)`, `deactivate(DeactivateCompanyPolicyCommandDTO)`, `getPolicies(GetCompanyPoliciesQueryDTO)`, `getActivePolicies(GetActiveCompanyPoliciesQueryDTO)`. Os records validam campos obrigatórios com IAE (400) — ex. `DeactivateCompanyPolicyCommandDTO.java:10-17` "Usuário atualizador é obrigatório".

`CompanyPolicyCommandService` (`svc/companypolicy/CompanyPolicyCommandService.java`):
- `create` (`:32-60`, `@Transactional`): `existsByCompanyIdAndCodeAndStatus(companyId, code cru, ACTIVE)` → `AlreadyExistsException("Já existe uma política ativa com o código: <code>")` → **409** (+ `company_policy_business_errors_total{POLICY_CODE_ALREADY_EXISTS,create}`) → se `status == ACTIVE`: desativa **todas** as políticas ativas da empresa (`updatedBy = createdBy`, sem incrementar versão) → `CompanyPolicy.create` → save → `company_policy_created_total{entity_id}`. **Não verifica se a empresa existe.**
- `update` (`:62-95`): `findById` (404 "Política não encontrada: <id>") → se ativando (status ACTIVE e atual não ativo) desativa as outras ativas da empresa → `updateDescription`, `updateStatus`, `updateEffectiveTo` **só se não-nulo** → `incrementVersion` (+1) → save → `company_policy_updated_total`.
- `deactivate` (`:97-116`): `findById` (404) → `deactivate(updatedBy)` → save → `company_policy_deactivated_total`. Versão não muda.
- Regra resultante: **no máximo uma política ACTIVE por empresa**.

`CompanyPolicyQueryService` (`svc/companypolicy/CompanyPolicyQueryService.java`): `getPolicies` (todas da empresa) e `getActivePolicies` (status ACTIVE), `@LogOperation(audit=false)`; métrica `company_policy_queried_total{operation, company_id}`. Sem cache. Ordem não definida.

### 4.6 Contact
`ContactPort` (`application/port/in/ContactPort.java:12-37`).
`ContactCommandService` (`svc/contact/ContactCommandService.java`, `@Transactional`):
- `create` (`:34-73`): empresa (404 + `contact_business_errors_total{COMPANY_NOT_FOUND,create}`) → status (403) → `findByEmail(command.email())` (valor **cru**, case-sensitive) → existe: `AlreadyExistsException("Já existe um contato com o email: <email>")` → **409** → `Contact.create` (email lower) → `save(contact, companyId)` → `company.addContact` + `companyRepository.save` → `contact_created_total{entity_id,company_id}`. Unicidade de email é **global** (todas as empresas) — também há `UNIQUE` na coluna.
- `update` (`:75-118`): `findById` (404 "Contato não encontrado: <id>") → companyId (404 "Empresa não encontrada para o contato: <id>") → empresa + status (403) → se `command.email() != null` e diferente do atual (comparação crua): `findByEmail` → se for outro id → 409 → merge null=manter (`ContactApplicationMapper.java:36-57`) → save.
- `activate`/`deactivate` (`:120-170`): sem checagem de status; `contact_activated_total` / `contact_deactivated_total`.
- `delete` (`:172-191`): `existsById` → 404 ou `deleteById`.

`ContactQueryService` (`svc/contact/ContactQueryService.java`): igual a Address; `listActiveByCompanyId` cacheia a **lista** de ativos em `contact_cache:company:<id>:active`. `search` usa `Specification` (seção 5.3). Erros → `QueryOperationException(..., "CONTACT_QUERY_ERROR", ...)`.

### 4.7 Representative
`RepresentativePort` (`application/port/in/RepresentativePort.java:10-39`).
`RepresentativeCommandService` (`svc/representative/RepresentativeCommandService.java`, `@Transactional`):
- `create` (`:33-79`): empresa (404 **"Empresa não encontrada"** — sem id) → status (403) → `existsByCompanyIdAndCpf(companyId, command.cpf())` (qualquer status) → **`IllegalArgumentException("Representante com este CPF já existe para esta empresa")` → 400** (Swagger diz 409) → `Representative.create` → `save(rep, companyId)` → métrica **`representative.created`** (Prometheus `representative_created_total`) `{entity_id}`. Não chama `company.addRepresentative`.
- `update` (`:81-128`): `findById` (404 "Representante não encontrado") → companyId (404) → empresa + status (403) → `Representative.of(id, <todos os campos do command>, active atual)` (substituição total, sem checar duplicidade de CPF) → `save(rep)` (adapter preserva `company`) → `representative.updated`.
- `activate`/`deactivate` (`:130-182`): 404 se não existe; sem checagem de status; `representative.activated` / `representative.deactivated`.
- `delete` (`:184-206`): `findById` (404) → `delete(rep)` → `representative.deleted`.

`RepresentativeQueryService` (`svc/representative/RepresentativeQueryService.java`, readOnly):
- `findById` (404 "Representante não encontrado"), `findAll`, `findAllActive`.
- `findByCompanyId` (cache lista), `findActiveByCompanyId` (cache; `findFirstByCompanyIdAndActiveTrue`; 404 "Representante ativo não encontrado para esta empresa").
- `findByCpf(cpf)` (global, valor cru; 404 "Representante não encontrado com este CPF"; se o CPF existir em 2 empresas → `IncorrectResultSizeDataAccessException` → 500 [framework]).
- `findByEmail(email)` (valor cru, case-sensitive contra valor gravado em minúsculas; 404 "Representante não encontrado com este email").
- Métricas: `representative.found.by_id`, `.all`, `.by_company{company_id,count,status}`, `.active_by_company{company_id,entity_id,status}`, `.all_active`, `.by_cpf`, `.by_email`.
- Erros **não** são embrulhados em QueryOperationException aqui.

---

## 5. Persistência

### 5.1 Configuração
- **PostgreSQL**, driver `org.postgresql.Driver`. URL `jdbc:postgresql://postgres:5432/keepguard_api_db` (`application.yml:15`; Helm sobrescreve via `SPRING_DATASOURCE_URL`, `deployment.yaml:37-38`), usuário `keepguard_api_user` (Helm: configMap `keepguard-config/POSTGRES_USER`), senha (Helm: secret `keepguard-secret/POSTGRES_PASSWORD`).
- Hikari base/local/dev: `connection-timeout 30000`, `maximum-pool-size 10`, `minimum-idle 5`, `idle-timeout 600000`, `max-lifetime 1800000`, `connection-test-query SELECT 1` (`application.yml:19-25`). Prod: timeout 20000, pool 20, min 10, `leak-detection-threshold 60000` (`application-prod.yml:10-17`).
- **Sem Flyway/Liquibase**. `spring.jpa.hibernate.ddl-auto: update` (base/local/dev), `validate` (prod) — `application.yml:42`, `application-prod.yml:32`. `hibernate.default_schema: ms_company`. Dialeto auto (PostgreSQL). `show-sql: false`.
- `@EnableJpaRepositories` em `MsCompanyApplication.java:11` (`infrastructure.persistence.spring`) e um segundo em `infra/config/DatabaseConfig.java:7` apontando para `com.keepguard.ms_company.domain` (sem repositórios lá — sem efeito).
- Hibernate 6.6.x (Boot 3.5.3). Timestamps `LocalDateTime` gravados em `timestamp(6)` sem fuso; valor = relógio da JVM (container sem `TZ`/`user.timezone` → UTC) [framework].
- Sem `@Version` (sem lock otimista), sem soft delete (todos os deletes são físicos), sem auditoria Spring Data (`created_at`/`updated_at` via `@PrePersist`/`@PreUpdate`).

### 5.2 Tabelas (schema `ms_company`) — DDL equivalente reconstituído das entities

> Gerado pelo Hibernate com `ddl-auto=update`; nomes de FK/unique gerados automaticamente (exceto `uk_company_channel`). Para enums `STRING` o Hibernate 6 costuma gerar `CHECK (col IN (...))` em tabelas novas [framework]. Antes de escrever migrations Go, **extrair o DDL real** de prod (`pg_dump -s -n ms_company`).

**`companies`** — `infra/persistence/entity/CompanyJpaEntity.java`
| coluna | tipo | null | outros |
|---|---|---|---|
| id | uuid | NOT NULL | PK, `@GeneratedValue(UUID)`, updatable=false (`:25-29`) |
| code_company | uuid | NOT NULL | UNIQUE, updatable=false (`:31-32`) |
| tenant_id | uuid | NOT NULL | UNIQUE, updatable=false (`:34-35`) |
| name | varchar(150) | NOT NULL | |
| legal_name | varchar(200) | NOT NULL | |
| cnpj | varchar(14) | NOT NULL | UNIQUE (`:43-44`) |
| state_registration | varchar(20) | null | |
| municipal_registration | varchar(20) | null | |
| tax_regime | varchar(255) | NOT NULL | enum STRING |
| ein | varchar(20) | null | |
| status | varchar(255) | NOT NULL | enum STRING, default Java `PENDING_APPROVAL` |
| created_at / updated_at | timestamp(6) | null | `@PrePersist` (também gera code_company/tenant_id se nulos) / `@PreUpdate` (`:100-115`) |
Relacionamentos `@OneToMany(mappedBy="company", cascade=ALL, orphanRemoval=true)` + `@BatchSize(10)`, fetch LAZY (padrão): `addresses`, `contacts`, `representatives`, `bankAccounts`, `cnaes`, `mfaChannels` (`:52-80`). **Não há relação com `company_policies`.**

**`company_addresses`** — `CompanyAddressJpaEntity.java`: `id uuid PK`, `company_id uuid NOT NULL FK→companies(id)` (`@ManyToOne LAZY`), `street varchar(150) NN`, `number varchar(20) NN`, `complement varchar(100)`, `district varchar(100) NN`, `city varchar(100) NN`, `state varchar(2) NN`, `country varchar(100) NN`, `zip_code varchar(8) NN`, `active boolean NN` (default Java true), `created_at`, `updated_at`.

**`company_bank_accounts`** — `CompanyBankAccountJpaEntity.java`: `id`, `company_id NN FK`, `bank_code varchar(3) NN` (campo Java `code`), `bank_agency varchar(10) NN` (`agency`), `bank_agency_digit varchar(1)` (`agencyDigit`), `bank_account_number varchar(20) NN` (`accountNumber`), `bank_account_digit varchar(1) NN` (`accountDigit`), `bank_account_type varchar(255) NN` (enum, `accountType`), `active NN`, timestamps. Atenção: para `sortFields` vale o nome Java (`code`, `agency`, ...), não o da coluna.

**`company_cnaes`** — `CompanyCnaeJpaEntity.java`: `id`, `company_id NN FK`, `code varchar(7) NN`, `description varchar(500) NN`, `section varchar(1)`, `division varchar(2)`, `group_code varchar(3)`, `class_code varchar(4)`, `subclass_code varchar(5)`, `active boolean NN` (default true), `principal boolean NN` (default false), timestamps. **UNIQUE (company_id, code)** (`:13-14`).

**`company_contacts`** — `CompanyContactJpaEntity.java`: `id`, `company_id NN FK`, `name varchar(100) NN`, `email varchar(150) NN` **UNIQUE global** (`:33-34`), `phone varchar(20) NN`, `website varchar(150)`, `position varchar(100)`, `department varchar(100)`, `active NN`, timestamps.

**`company_mfa_channels`** — `CompanyMfaChannelJpaEntity.java`: `id`, `company_id NN FK`, `channel varchar(30) NN` (enum), `is_required boolean NN` (default true), `is_enabled boolean NN` (default true), timestamps. **UNIQUE `uk_company_channel` (company_id, channel)** (`:14-16`).

**`company_policies`** — `CompanyPolicyJpaEntity.java`: `id`, `company_id uuid NN` (**coluna simples, sem FK**), `code varchar(64) NN`, `description varchar(255) NN`, `status varchar(255) NN` (default INACTIVE), `version integer NN` (default 1; **não é `@Version`**), `effective_from timestamp(6) NN`, `effective_to timestamp(6)`, `created_at`, `updated_at`, `created_by varchar(64)`, `updated_by varchar(64)`.

**`company_representatives`** — `CompanyRepresentativeJpaEntity.java`: `id`, `company_id NN FK`, `name varchar(150) NN`, `cpf varchar(11) NN` (sem unique), `rg varchar(15)`, `birth_date date NN`, `email varchar(150) NN` (sem unique), `phone varchar(20) NN`, `role varchar(100)`, `active NN`, timestamps.

Índices: apenas PK e UNIQUEs acima (nenhum `@Index`). Delete de `companies` via JPA remove os filhos por cascade (o Hibernate carrega e apaga um a um); políticas ficam órfãs. No banco não há `ON DELETE CASCADE` [framework].

Seed: `scripts/seed_company_mfa_email_only.sql:4-21` — em transação, apaga os canais MFA da company `f7fc7350-b9fc-4e54-9c58-ac9385b23ae4` e insere `EMAIL` com `is_required=true, is_enabled=true`, `gen_random_uuid()`, `NOW()`.

### 5.3 Repositórios Spring Data (consultas equivalentes)

`CompanySpringRepository` (`infra/persistence/spring/CompanySpringRepository.java`):
- `findByIdWithRelations(id)`: `SELECT c FROM CompanyJpaEntity c WHERE c.id = :id` (apesar do nome, **sem fetch join**; filhos carregados lazy em lotes de 10 pelo mapper).
- `findByCnpj`, `findByCodeCompany`, `findByTenantId`, `findAllByStatus`: igualdade simples.
- `existsByCnpj(cnpj)` (derivada) → `SELECT EXISTS(... WHERE cnpj = ?)`.
- `findByLegalNameContainingIgnoreCase`, `findByNameContainingIgnoreCase`, `findAllWithRelations`, `searchWithRelations`, `findByFilters`, `existsBytenantId` — **não usados** pelos services (findByFilters faria `LEFT JOIN addresses` com filtros opcionais).
- **Search real** (`infra/persistence/CompanyRepositoryAdapter.java:138-234`): `JpaSpecificationExecutor.findAll(spec, pageable)`:
  - `name` → `lower(name) LIKE '%'||lower(:name)||'%'`; `legalName` idem; `cnpj` → `cnpj LIKE '%:cnpj%'` (case-sensitive); `status` → igualdade.
  - `city` → `lower(root.get("address").get("city"))` e `state` → `upper(root.get("address").get("state")) = upper(:state)`: **a entity não tem atributo `address`** (é `addresses`) → `IllegalArgumentException`/`InvalidDataAccessApiUsageException` na montagem → embrulhado em RuntimeException → **500**.
  - Ordenação default `name ASC`; com `sortFields`, direção DESC se `sortDirection` = "DESC" (case-insensitive).
  - Count separado (`page.getTotalElements()`).
  - Wildcards `%`/`_` do usuário não são escapados.

`AddressSpringRepository` (`.../AddressSpringRepository.java`):
- `findByCompanyId`: `WHERE a.company.id = :companyId`.
- `findActiveByCompanyId`: `WHERE a.company.id = :companyId AND a.active = true` → `Optional` (mais de 1 ativo → `IncorrectResultSizeDataAccessException` → 500).
- `findAllActive`, `findByCityContainingIgnoreCase` (`a.city ILIKE :pattern`), `findByState`, `findByZipCodeContaining` (não usados pelos endpoints).
- `findByFilters` (`:40-55`): `WHERE (:companyId IS NULL OR a.company.id = :companyId) AND (:city IS NULL OR a.city ILIKE :cityPattern) AND (:state IS NULL OR a.state = :state) AND (:zipCode IS NULL OR a.zipCode LIKE :zipCodePattern) AND (:active IS NULL OR a.active = :active)`; patterns `%x%` montados no adapter (`AddressRepositoryAdapter.java:124-125`). `state` comparação exata (case-sensitive; gravado em maiúsculas). Ordenação default `city ASC` (`:151`).
- `findCompanyIdByAddressId`: `SELECT a.company.id ... WHERE a.id = :addressId`.

`BankAccountSpringRepository`: análogos (`findByCompanyId`, `findActiveByCompanyId`, `findAllActive`, `findByBankCode`, `findByAccountType`, `findCompanyIdByBankAccountId`); `findByFilters` (`:37-48`): `(:companyId IS NULL OR b.company.id = :companyId) AND (:bankCode IS NULL OR b.code = :bankCode) AND (:accountType IS NULL OR b.accountType = :accountType) AND (:active IS NULL OR b.active = :active)` — `accountType` é passado como **String** contra atributo enum (provável erro de tipo de parâmetro do Hibernate quando informado → 500) [framework]. Ordenação: **sem ordenação default** (`Sort.unsorted()`, `BankAccountRepositoryAdapter.java:111`).

`CnaeSpringRepository` (derivadas): `findByCompanyId` (`company.id`), `findPrincipalByCompanyId` (`principal = true AND active = true`, Optional), `findByCompanyIdAndActiveTrue`, `findByActiveTrue`, `existsByCompanyIdAndCode`, `findByCompanyIdAndCode`, `countByCompanyIdAndActiveTrue`, `findPrincipalsByCompanyId` (não usado), `findByCode` (não usado), `countByActiveTrue`.

`CompanyPolicySpringRepository`: `findByCompanyId`, `findByCompanyIdAndStatus`, `existsByCompanyIdAndCodeAndStatus` (`SELECT COUNT(p) > 0 ...`), `findByCompanyIdAndCodeAndStatus`, `existsActivePolicyByCompanyId` (`... AND p.status = 'ACTIVE'`).

`ContactSpringRepository`: `findByCompanyId`, `findActiveByCompanyId`, `findAllActive`, `findByEmail` (igualdade exata), `findBy*ContainingIgnoreCase` (não usados), `findCompanyIdByContactId`, `existsByEmail`, `findByCompanyId(pageable)` (não usados). **Search** (`ContactRepositoryAdapter.java:126-194`) via Specification: `companyId` igualdade; `name`/`email`/`position`/`department` → `lower(col) LIKE '%'||lower(x)||'%'` (ignorados se nulos/brancos); `active` igualdade. Default `name ASC`. Retorna `page`/`size` do critério.

`RepresentativeSpringRepository` (derivadas): `findByCompanyId`, `findFirstByCompanyIdAndActiveTrue` (LIMIT 1, sem ORDER BY explícito → ordem indefinida), `findByActiveTrue`, `existsByCompanyIdAndCpf`, `findByCompanyIdAndCpf`, `findByCpf` (Optional), `findByEmail` (Optional), `findByNameContainingIgnoreCase`, `findByRoleContainingIgnoreCase`, `countByCompanyIdAndActiveTrue`, `findCompanyIdByRepresentativeId`.

Nenhuma listagem não paginada tem `ORDER BY` → ordem do Postgres (indefinida).

### 5.4 Adapters e mappers JPA — comportamentos de gravação
- `CompanyRepositoryAdapter.save` (`:36-86`):
  - **Empresa existente** (`findById` acha): copia `name, legalName, cnpj, stateRegistration, municipalRegistration, taxRegime, ein, status`; faz **diff de MFA por canal**: remove da coleção os canais que não estão no alvo (orphanRemoval → DELETE), atualiza `required/enabled` dos existentes, adiciona novos (id gerado). **Filhos (endereços etc.) não são tocados.** `code_company`, `tenant_id`, `created_at` não mudam.
  - **Nova**: `CompanyJpaMapper.toEntity` (com o `id` já gerado no domínio) → `save` (= `merge`, pois id ≠ null).
- `CompanyJpaMapper.toDomain` (`infra/persistence/mapper/CompanyJpaMapper.java:110-187`): reidrata e chama `addMfaChannel`, `addAddress`, `addContact`, `addRepresentative`, `addBankAccount`, `addCnae` — aplicando as regras de exclusividade em memória (ex.: se houver 2 endereços ativos no banco, só o último carregado fica ativo no objeto; nada é gravado).
- `AddressRepositoryAdapter.save(address)` sem companyId (`:34-39`) monta entity **sem `company`** e faz merge → sobrescreve `company_id` com null (violação NOT NULL) — usado ao desativar o endereço/conta ativa anterior (risco, seção 12). `save(address, companyId)` cria um `CompanyJpaEntity` só com id como referência.
- `BankAccountRepositoryAdapter`: idem Address.
- `CnaeRepositoryAdapter.save(cnae)` usa `cnae.getCompanyId()` (`:29-33`). `CnaeJpaMapper.toDomain` usa `UUID.randomUUID()` como companyId se a entity não tiver company (`CnaeJpaMapper.java:29`).
- `ContactRepositoryAdapter.save(contact)` sem companyId → mesmo risco; services usam sempre o overload com companyId.
- `RepresentativeRepositoryAdapter.save(rep)` (`:28-53`): se existe, carrega e copia campos (preserva company); senão cria sem company. `delete` usa entity sem company (Spring Data faz `find` + `remove(merge)`).
- `CompanyPolicyRepositoryAdapter.save`: entity completa (inclui `createdAt` do domínio) → merge.
- Todos os adapters têm `@Retry(name="databaseOperation")` + `@Bulkhead(name="databaseOperation")` em nível de classe (seção 8.5).

---

## 6. Cache Redis

Cliente: `StringRedisTemplate` (strings UTF-8), Lettuce. Config: `local` → standalone `localhost:6379` (`application-local.yml:17-19`), Helm injeta `SPRING_DATA_REDIS_HOST=redis`, `SPRING_DATA_REDIS_PORT=6379`; `dev`/`prod` → cluster de 6 nós `redis-node-1:7000 ... redis-node-6:7005`, `max-redirects 3` (dev com refresh adaptativo 30s). `timeout 3000ms`, pool `max-active 8, max-idle 8, min-idle 2, max-wait -1ms`.

Serialização: `ObjectMapper` do Spring (o mesmo das respostas HTTP, **com INDENT_OUTPUT** → JSON indentado no Redis) dos records `*ViewDTO` da camada application (nomes de campos = componentes do record; `LocalDateTime` ISO). Leitura com `readValue(..., XViewDTO.class)` ou `List<XViewDTO>`.

TTL: `cache.redis.ttl.*` = **2592000 s (30 dias)** para company/address/bank-account/contact/representative (`application.yml:57-70`). Prefixos configuráveis (`cache.redis.prefix.*`), `:` finais removidos.

| Chave (formato exato) | Valor | Escrito em | Lido em | Invalidado em | Classe:linhas |
|---|---|---|---|---|---|
| `company_cache:id:<uuid lower>` | `CompanyViewDTO` | `getById` miss | `getById` | `clearAllCompanyCache` e `clearAllCompanyCacheById` | `infra/redis/CompanyCacheService.java:36-60,297-299` |
| `company_cache:cnpj:<só dígitos>` | `CompanyViewDTO` | `getByCnpj` miss | `getByCnpj` | só `clearAllCompanyCache` | `:62-86,301-303` |
| `company_cache:code:<uuid lower>` | `CompanyViewDTO` | `getByCodeCompany` miss | idem | só `clearAllCompanyCache` | `:88-112,305-307` |
| `company_cache:tenantId:<uuid lower>` | `CompanyViewDTO` | `getByTenantId` miss (só ACTIVE) | idem | só `clearAllCompanyCache` | `:144-167,309-311` |
| `company_cache:simple:id:<id>` | `CompanySimpleViewDTO` | nunca | nunca (sem uso) | ambos | `:178-212` |
| `company_cache:simple:tenantId:<id>` | `CompanySimpleViewDTO` | `getSimpleByTenantId` (sem endpoint) | idem | `clearAllCompanyCache` | `:214-246` |
| `address_cache:company:<companyId>` | `[AddressViewDTO]` (inclusive `[]`) | `listByCompanyId` miss | idem | **só** via `clearAllCompanyCache*` | `infra/redis/AddressCacheService.java:31-55,112-114` |
| `address_cache:company:<companyId>:active` | `AddressViewDTO` | `getActiveByCompanyId` miss | idem | idem | `:57-81,116-118` |
| `bank_account_cache:company:<id>` / `:active` | lista / item | análogo | análogo | idem | `BankAccountCacheService.java` |
| `contact_cache:company:<id>` / `:active` | lista / **lista** de ativos | análogo | análogo | idem | `ContactCacheService.java` |
| `representative_cache:company:<id>` / `:active` | lista / item | análogo | análogo | idem | `RepresentativeCacheService.java` |

`clearAllCompanyCache(id, cnpj, code, tenantId)` (`CompanyCacheService.java:248-267`): apaga as 6 chaves de company e as chaves `company:<id>` + `company:<id>:active` dos 4 filhos. É chamado por update, approve, reject, activate, deactivate, suspend, block, updateMfaChannels. `clearAllCompanyCacheById(id)` (`:269-284`): só `id`, `simple:id` e os filhos — chamado no delete (deixa `cnpj`/`code`/`tenantId` stale por até 30 dias). `create` de empresa não toca cache. **Nenhuma** escrita de Address/BankAccount/Contact/Representative invalida cache.

Normalização de chave: `trim().toLowerCase()` em ids; CNPJ só dígitos.

Falha do Redis:
- Leituras: `@CircuitBreaker(name="redisCache", fallbackMethod=...)` + `@Retry(name="redisCache")`; exceção vira `RuntimeException`; fallback (`getXFallback(String, Exception)`) loga "FALLBACK: Redis indisponivel" e retorna `null` → segue para o banco (`CompanyCacheService.java:329-337`).
- Escritas e remoções: `@CircuitBreaker` sem fallback, mas com `try/catch` interno que só loga `warn` → nunca derrubam a requisição.
- Chamadas internas de `clearAllCompanyCache` para os outros métodos são auto-invocação (sem aspecto); cada uma tem seu try/catch.
- Com timeout de 3 s do Lettuce e retry, uma leitura com Redis fora pode somar vários segundos de latência antes do fallback.

---

## 7. Integrações de saída

### 7.1 ms-auth — Feign `AuthProvisionClient` (`adapters/out/feign/AuthProvisionClient.java:9-18`)
- `@FeignClient(name="auth-service", url="${AUTH_SERVICE_URL:http://localhost:8081}", configuration=AuthClientConfig.class)`. Helm: `AUTH_SERVICE_URL=http://ms-auth:8081` (`helm/values.yaml:23`, `deployment.yaml:75-76`). `@EnableFeignClients` em `MsCompanyApplication.java:12`.
- Chamada: `POST /api/v1/companies/{companyId}/roles/provision` — sem corpo, **sem headers de autenticação**, **sem propagação de `X-Correlation-ID`** (não há `RequestInterceptor`). Retorno ignorado (`void`).
- Lado ms-auth (`ms-auth/.../CompanyRoleProvisionController.java:29-39`): idempotente; clona `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_USER` para a company; responde 201 (provisionou) ou 200 (`alreadyProvisioned`).
- Config: `AuthClientConfig` só define `Logger.Level.BASIC` (`AuthClientConfig.java:10-13`; sem efeito visível pois o logger do Feign não está em DEBUG).
- **Resiliência: nenhuma** (sem CB/Retry/TimeLimiter no Feign; `spring.cloud.openfeign.*` não configurado). Timeouts = padrão Spring Cloud OpenFeign: connect 10 s, read 60 s; sem retry (`Retryer.NEVER_RETRY`) [framework].
- Quando: somente em `CompanyCommandService.create`, **depois** do insert da empresa (`svc/company/CompanyCommandService.java:63-65`), via `AuthRoleProvisionAdapter` (`adapters/out/feign/AuthRoleProvisionAdapter.java:17-21`, loga "Provisionando roles no ms-auth para company {id}").
- Falha (rede/4xx/5xx): `FeignException` → 500 "Erro interno do servidor"; a empresa **permanece gravada**, sem rollback, sem compensação, e um novo POST com o mesmo CNPJ dará 409.

### 7.2 RabbitMQ — auditoria (via lib-common, sem código no ms)
- `spring-boot-starter-amqp` (`pom.xml:111-114`); host/porta/usuário/senha por env (`application.yml:26-30`, Helm `SPRING_RABBITMQ_*` e `RABBITMQ_*`).
- `keepguard.audit`: `enabled: true`, `exchange: ${KEEPGUARD_AUDIT_EXCHANGE:srv-audit-exchange-local}` (Helm: `srv-audit-exchange-prod`), `routing-key: audit.event`, `source-service: ms-company` (`application.yml:143-148`).
- Publicação detalhada na seção 8.3. Sem consumers (`@RabbitListener`) no ms.

### 7.3 Outras
- `USER_SERVICE_URL` é injetado pelo Helm (`deployment.yaml:73-74`) mas **não é usado** no código. `JWT_SECRET` e `KEEPGUARD_VALIDATION_MODERATION_ENABLED` também não.

---

## 8. Infra transversal

### 8.1 Correlation ID — `infra/filter/CorrelationIdFilter.java`, `infra/context/CorrelationContext.java`
- `OncePerRequestFilter` `@Component`. Ignora (sem MDC/log/header) os paths **exatos** `/actuator/prometheus`, `/actuator/health`, `/actuator/info`, `/actuator/metrics` (`:26-31,75-87`) — `/actuator/health/liveness` e `/readiness` **não** são ignorados.
- Header de entrada `X-Correlation-ID` (servlet: case-insensitive); ausente ou vazio → `UUID.randomUUID()` (`:46-49`). Grava em MDC `correlationId` (`CorrelationContext.java:23-29`; se vier string só com espaços, gera outro UUID para o MDC mas o header de resposta leva o valor original), seta header de resposta `X-Correlation-ID` (`:61`), loga `"Processing request: method={}, path={}, correlationId={}, application={}, userAgent={}, remoteAddr={}"` (application = header `X-Tenant-Id`), e limpa o MDC no `finally`.

### 8.2 Métricas
- Micrometer + `micrometer-registry-prometheus`; exposto em `/actuator/prometheus`; scrape em `k8s/observability/prometheus-configmap.yaml:37-43` (`ms-company:8083`, path `/actuator/prometheus`). Dashboard em `k8s/observability/dashboards/ms-company.json`.
- `MetricsPort` (`application/port/out/metrics/MetricsPort.java`) → `MetricsAdapter` (`infra/metrics/MetricsAdapter.java`) → `lib/metrics/service/MetricsService.java` (counter via `meterRegistry.counter(name, tags...)`).
- **Por endpoint** (`@MetricsEndpoint`, aspecto `lib/metrics/aspect/MetricsAspect.java:29-68`): counter `api_requests_total{endpoint, application, status=success|error}` e timer `api_requests_latency_seconds{endpoint, application, status}`. `application` = header `X-Tenant-Id` ou `"none"` (o `applicationParam` não é usado no ms). Loga "Recebida requisição para {operation}: {application}" / sucesso / erro. Lista de `endpoint` (valor da tag): `company_create, company_update, company_approve, company_reject, company_activate, company_deactivate, company_suspend, company_block, company_get_by_id, company_get_by_tenant_id, company_get_by_code, company_get_by_cnpj, company_search, company_update_mfa_channels, company_delete; address_create, address_update, address_activate, address_deactivate, address_get_by_id, address_list_by_company, address_get_active_by_company, address_list, address_search, address_list_all, address_delete; bank_account_create, bank_account_update, bank_account_activate, bank_account_deactivate, bank_account_get_by_id, bank_account_list_by_company, bank_account_get_active_by_company, bank_account_list, bank_account_search, bank_account_list_all, bank_account_delete; cnae_create, cnae_update, cnae_activate, cnae_deactivate, cnae_set_principal, cnae_get_by_id, cnae_list_by_company, cnae_get_principal_by_company, cnae_list_active_by_company, cnae_delete; company_policy_create, company_policy_update, company_policy_deactivate, company_policy_list, company_policy_list_active; contact_create, contact_update, contact_activate, contact_deactivate, contact_delete, contact_get_by_id, contact_list_by_company, contact_list_active_by_company, contact_list_all, contact_search; representative_create, representative_update, representative_activate, representative_deactivate, representative_delete, representative_get, representative_list, representative_list_by_company, representative_get_active_by_company, representative_list_active, representative_search_by_cpf, representative_search_by_email`. Health/Helper não têm.
- **Por operação** (`@LogOperation` → `lib/logging/service/LoggingService.java:49-90`): `operations_total{operation=<lowercase>, status=SUCCESS|ERROR}` e `business_errors_total{error_type=UNKNOWN_ERROR}` em erro. Operações: `create_company, update_company, approve_company, reject_company, activate_company, deactivate_company, update_mfa_channels, suspend_company, block_company, delete_company, create_address, ..., create_cnae, update_cnae, activate_cnae, deactivate_cnae, set_principal_cnae, delete_cnae, create_company_policy, update_company_policy, deactivate_company_policy, get_company_policies, get_active_company_policies, create_contact, ..., create_representative, ...`.
- **Erros tratados** (GlobalExceptionHandler → LoggingService): `validation_errors_total{field=request|BUSINESS_RULE}`, `business_errors_total{error_type=ENTITY_NOT_FOUND|ENTITY_ALREADY_EXISTS|INVALID_STATUS_FOR_OPERATION|INVALID_STATUS_TRANSITION|...}`, `operations_total{operation=generic_exception|query_operation_exception|..., status=ERROR}`.
- **De negócio** (chamadas explícitas nos services; nomes e tags na seção 4): `company_business_errors_total{error_code, operation}`, `company_created_total{entity_id}`, `company_updated_total`, `company_approved_total`, `company_rejected_total`, `company_activated_total`, `company_deactivated_total`, `company_suspended_total`, `company_blocked_total`, `company_deleted_total`, `company_not_found_total{entity_id, operation}`, `company_queries_total{query_type, status}`, `company_invalid_status_total{entity_id, status, operation}`; `address_*`, `bank_account_*`, `contact_*` (`*_business_errors_total`, `*_not_found_total`, `*_created_total{entity_id, company_id}`, `*_updated_total`, `*_activated_total`, `*_deactivated_total`, `*_deleted_total`, `*_queries_total{query_type, status[, count]}`, `*_system_errors_total{error_type, operation}`); `cnae_business_errors_total`, `cnae_created_total`, `cnae_updated_total`, `cnae_activated_total`, `cnae_deactivated_total`, `cnae_set_principal_total`, `cnae_deleted_total`, `cnae_queried_total`, `cnae_query_errors_total`; `company_policy_business_errors_total`, `company_policy_created_total`, `company_policy_updated_total`, `company_policy_deactivated_total`, `company_policy_queried_total{operation, company_id}`; `representative.created|updated|activated|deactivated|deleted{entity_id}` e `representative.found.*` (Prometheus troca `.` por `_` e adiciona `_total`).
- Métricas de Resilience4j (`infra/config/resilience/ResilienceConfig.java:26-69`): `TaggedCircuitBreakerMetrics`, `TaggedRetryMetrics`, `TaggedBulkheadMetrics`, `TaggedRateLimiterMetrics`, `TaggedTimeLimiterMetrics` ligadas ao registry (`resilience4j_circuitbreaker_*`, `resilience4j_retry_*`, `resilience4j_bulkhead_*`).
- Atenção: várias métricas usam `entity_id`/`company_id`/`count` como **tag** (cardinalidade ilimitada) e o mesmo nome de métrica aparece com conjuntos de tags diferentes (ex. `address_not_found_total` com `entity_id` ou `company_id`; `address_queries_total` com/sem `count`; `cnae_queried_total` com 3 formatos). O registry Prometheus do Micrometer não exporta (e apenas avisa) meters do mesmo nome com chaves de tag diferentes [framework — verificar em `/actuator/prometheus`].

### 8.3 Logging e auditoria (`@LogOperation`, lib)
- Aspecto `lib/logging/aspect/LoggingAspect.java:41-116` envolve todos os métodos `@LogOperation` dos Command Services (e 2 do CompanyPolicyQueryService com `audit=false`).
- Fluxo: monta contexto (parâmetros `UUID/String/Number/Boolean` por nome; UUID chamado `id`/`entityId` vira `entity_id`; String no formato `NN.NNN.NNN/NNNN-NN` vira `cnpj`), loga início/sucesso/erro, incrementa `operations_total`, e se `audit=true` publica evento com `outcome` `SUCCESS` ou `FAILURE` (também em erro).
- `description` com placeholders: `{id}` e `{companyId}` são resolvidos (vêm de parâmetros); `{name}` (create company) e `{code}` (create policy) **ficam literais** porque o parâmetro é um record de comando.
- `entityId` do evento: `entity_id`/`id` do contexto; senão tenta `result.getId()` (os View DTOs são records com `id()`, não `getId()` → falha silenciosa) → `null` nos creates.
- Evento (`lib/audit/AuditEvent.java`, montado por `lib/audit/AuditContextCollector.java:15-84`):
  ```json
  {"eventId":"<uuid>","occurredAt":"<Instant ISO>","schemaVersion":1,"sourceService":"ms-company",
   "correlationId":"<MDC correlationId | header X-Correlation-ID | uuid>","requestId":null,
   "tenantId":"<MDC tenantId | X-Tenant-Id | jwt | MDC companyId | X-Company-Id>",
   "companyId":"<MDC companyId | X-Company-Id | ... | tenantId>",
   "actor":{"type":"USER|ANONYMOUS","codeUser":"<MDC codeUser | X-User-ID>","roles":null,"clientIp":"<X-Forwarded-For[0] | remoteAddr>","deviceId":"<X-Device-Id>"},
   "action":"CREATE|UPDATE|APPROVE|REJECT|ACTIVATE|DEACTIVATE|SUSPEND|BLOCK|DELETE",
   "resource":{"type":"COMPANY|ADDRESS|BANK_ACCOUNT|CNAE|CONTACT|REPRESENTATIVE|COMPANY_POLICY","id":"<entityId|null>"},
   "outcome":"SUCCESS|FAILURE","reason":"<description, máx 512>","changes":null,"metadata":null}
  ```
  Publicado assíncrono (`lib/audit/RabbitAuditEventPublisher.java:41-69`): exchange `topic` durável declarado uma vez, routing key `audit.event`, `content-type: application/json`, `deliveryMode PERSISTENT`, header `X-Correlation-ID`. Executor `auditExecutor` core 2 / max 4 / fila 500, rejeição descartada silenciosamente (`lib/audit/RabbitAuditConfiguration.java:22-32`). Falhas só logam warn.
  Ações de auditoria por método: Company `CREATE, UPDATE, APPROVE, REJECT, ACTIVATE, DEACTIVATE, UPDATE (mfa), SUSPEND, BLOCK, DELETE`; Address/BankAccount/Contact/Representative `CREATE, UPDATE, ACTIVATE, DEACTIVATE, DELETE`; CNAE `CREATE, UPDATE (update/activate/deactivate/set-principal), DELETE`; Policy `CREATE, UPDATE (update e deactivate)`.
- Logback (`resources/logback-spring.xml`):
  - `local,test` (`:36-55`) e fallback: console legível `%d{HH:mm:ss.SSS} %highlight(%-5level) [%thread] %cyan(%logger{36}) %X{correlationId:- CorrelationId: %X{correlationId}} - %msg%n`; `com.keepguard.ms_company` em DEBUG; logs do `CorrelationIdFilter` e dos `LoggingService/MetricsAspect/StructuredLogger` da lib **desligados**.
  - `dev` (`:58-78`): mesmo formato, INFO.
  - `prod` (`:81-115`): `STDOUT_JSON` (pattern JSON manual: `timestamp, level, logger, message, service, correlationId, userId, action, entityType, entityId, durationMs, operation, errorCode, traceId, spanId`) + `LOGSTASH` TCP `${logging.logstash.host:-localhost}:${logging.logstash.port:-5000}` (`LogstashEncoder`; dependência `logstash-logback-encoder` **não está no pom** — só funcionaria se vier transitiva).
  - Logger `AUDIT` → STDOUT, sem additividade.

### 8.4 Segurança
- **Sem Spring Security / lib-security / JWT**. Nenhuma rota protegida; nada é validado de token. CORS não configurado. Swagger e Actuator públicos.

### 8.5 Resiliência (Resilience4j 2.2.0)
Config YAML (`application.yml:97-135`):
- CB `default`: `slidingWindowSize 10`, `failureRateThreshold 50`, `waitDurationInOpenState 60s`, `permittedNumberOfCallsInHalfOpenState 3`, `slowCallDurationThreshold 5000ms`, `slowCallRateThreshold 50`; instância `redisCache` (`registerHealthIndicator: true`).
- Retry `default`: `maxAttempts 2`, `waitDuration 1s`, `exponentialBackoffMultiplier 2` (sem `enableExponentialBackoff`), `retryExceptions: java.sql.SQLException, org.springframework.dao.DataAccessException, org.springframework.data.redis.RedisConnectionFailureException`; instâncias `redisCache`, `databaseOperation`.
- Bulkhead `default`: `maxConcurrentCalls 50`, `maxWaitDuration 0ms`; instância `databaseOperation`.
- **Porém** `ResilienceConfig` declara beans próprios `CircuitBreakerRegistry.ofDefaults()`, `RetryRegistry.ofDefaults()`, `BulkheadRegistry.ofDefaults()` etc. (`infra/config/resilience/ResilienceConfig.java:26-69`). Como a autoconfiguração do resilience4j-spring-boot3 só cria os registries se não houver bean, é provável que a configuração YAML **não seja aplicada** e valham os defaults da biblioteca [framework — verificar em `/actuator/health` (circuitbreakers) e `/actuator/prometheus`]: CB janela 100 / mínimo 100 chamadas / 50% / 60 s aberto / 10 em half-open; Retry 3 tentativas, 500 ms, **retry em qualquer exceção**; Bulkhead 25 concorrentes, espera 0.
- Aplicação: `@Retry` + `@Bulkhead` (`databaseOperation`) em todos os Repository Adapters (nível de classe); `@CircuitBreaker` (+ `@Retry` nas leituras) em todos os Cache Services (`redisCache`). Bulkhead cheio → `BulkheadFullException` → 500. Retry de DB acontece **dentro** da transação do service (após erro no Postgres a transação está abortada, então a nova tentativa tende a falhar igual).

### 8.6 Outras configurações (`application*.yml`)
- `spring.threads.virtual.enabled: true` (`application.yml:4-6`) — virtual threads.
- `spring.cloud.compatibility-verifier.enabled: false`.
- `spring.profiles.active: local` no `application.yml:11` (sobrescrito por `SPRING_PROFILES_ACTIVE`).
- `spring.servlet.multipart` 10MB (sem uso).
- `management.health.circuitbreakers.enabled`, `ratelimiters.enabled: true`.
- `springdoc.swagger-ui.enabled`, `api-docs.enabled: true`.
- Env vars referenciadas: `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`/`RABBITMQ_DEFAULT_USER`, `RABBITMQ_PASSWORD`/`RABBITMQ_DEFAULT_PASS`, `KEEPGUARD_AUDIT_EXCHANGE`, `AUTH_SERVICE_URL`; Helm ainda seta `SPRING_*` equivalentes.
- Health do Actuator inclui automaticamente db, redis, rabbit, diskSpace, ping, `healthController` [framework]: Redis ou RabbitMQ fora deixam `/actuator/health` DOWN (mas não os grupos liveness/readiness usados pelas probes).

---

## 9. Dependências das lib-* (imports `com.keepguard.lib*` em `src/main`)

Só **lib-common** é usada (não há lib-validation nem lib-security no `pom.xml` nem nos imports). `@SpringBootApplication(scanBasePackages = {"com.keepguard.ms_company", "com.keepguard.lib_common"})` (`MsCompanyApplication.java:10`) também registra os componentes da lib: `LoggingAspect`, `LoggingService`, `StructuredLogger`, `MetricsAspect`, `MetricsService`, `DateConverter` (sem uso), `RabbitAuditConfiguration`, `AuditPublisherFallbackConfiguration`.

| Arquivo (ms) | Import | Uso |
|---|---|---|
| `MsCompanyApplication.java:3` | `lib_common.config.MetricsConfig` | `@Import` — beans `MetricsService` e `MetricsAspect` |
| `ctrl/{address,bankaccount,cnae,company,companypolicy,contact,representative}/*Controller.java:3` | `lib_common.metrics.annotation.MetricsEndpoint` | métricas por endpoint (8.2) |
| `svc/address/AddressCommandService.java:13`, `svc/bankaccount/BankAccountCommandService.java:13`, `svc/cnae/CnaeCommandService.java:3`, `svc/company/CompanyCommandService.java:17`, `svc/companypolicy/CompanyPolicyCommandService.java:3`, `svc/companypolicy/CompanyPolicyQueryService.java:3`, `svc/contact/ContactCommandService.java:14`, `svc/representative/RepresentativeCommandService.java:3` | `lib_common.logging.annotation.LogOperation` | log estruturado + `operations_total` + auditoria RabbitMQ (8.3) |
| `svc/company/CompanyCommandService.java:3,14` | `lib_common.utils.BrazilianValidationUtils`, `lib_common.exception.ValidationException` | pré-validação de CNPJ; erro "política ativa" |
| `dom/entity/Company.java:3-5` | `InvalidStatusException`, `ValidationException`, `BrazilianValidationUtils` | transições de status, CNPJ, dados para aprovação |
| `dom/entity/Address.java:3-4` | `ValidationException`, `BrazilianValidationUtils` | UF, CEP |
| `dom/entity/BankAccount.java:3-4` | idem | código de banco |
| `dom/entity/Cnae.java:3-4` | idem | código CNAE |
| `dom/entity/CompanyPolicy.java:3` | `ValidationException` | code/description |
| `dom/entity/Contact.java:3-5` | `ValidationException`, `BrazilianValidationUtils`, `ValidationUtils` | email (lança `InvalidEmailException`), telefone |
| `dom/entity/Representative.java:3-5` | idem | CPF, email, telefone |
| `infra/metrics/MetricsAdapter.java:3` | `lib_common.metrics.service.MetricsService` | implementação do `MetricsPort` |
| `infra/rest/GlobalExceptionHandler.java:3,4,10` | `InvalidStatusException`, `ValidationException`, `lib_common.logging.service.LoggingService` | mapeamento de erros + log/métricas de erro |

Para o Go: reimplementar (ou reaproveitar de serviço Go existente) validação de CNPJ/CPF/CEP/UF/telefone/banco/CNAE/email com as **mesmas mensagens**, publisher de auditoria no mesmo formato JSON/exchange, e as métricas `api_requests_total`/`api_requests_latency_seconds`/`operations_total` se os dashboards dependerem delas (`k8s/observability/dashboards/ms-company.json`).

---

## 10. Deploy

### 10.1 Dockerfile (`Dockerfile:10-32`)
- Base `eclipse-temurin:25-jre`; instala `wget` e `curl`; usuário não-root `appuser`; copia `target/*.jar` (JAR compilado fora do Docker); `EXPOSE 8083`.
- `JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"`; `ENTRYPOINT sh -c "java $JAVA_OPTS -jar app.jar"`.
- Incoerência: `-Xmx1024m` com limite de memória do pod de 512Mi (risco de OOMKill).

### 10.2 Helm (`helm/`)
- `Chart.yaml`: `ms-company` versão/appVersion `1.0.9-1.0.0`.
- `values.yaml`: imagem `ghcr.io/keepguard/ms-company:1.0.9-1.0.0` (o deploy troca por tag SHA), `pullPolicy IfNotPresent`, `service.port 8083`, `resources.limits.memory 512Mi`, `requests.memory 256Mi` (sem CPU), `imagePullSecrets ghcr-secret`; env: `springProfilesActive: local`, `serverPort 8083`, `redisHost redis`, `redisPort 6379`, `rabbitmqHost rabbitmq-service`, `rabbitmqPort "5672"`, `userServiceUrl http://ms-user:8085`, `authServiceUrl http://ms-auth:8081`, `postgresDb keepguard_api_db`, `postgresUser keepguard_api_user`.
- `templates/deployment.yaml`: 1 réplica, label `app: ms-company`; env `SPRING_PROFILES_ACTIVE`, `SPRING_CLOUD_COMPATIBILITY_VERIFIER_ENABLED=false`, `SERVER_PORT`, `JWT_SECRET` (secret `keepguard-secret`), `SPRING_DATASOURCE_URL/USERNAME (configMap keepguard-config POSTGRES_USER)/PASSWORD (secret POSTGRES_PASSWORD)`, `SPRING_DATA_REDIS_HOST/PORT`, `SPRING_RABBITMQ_HOST/PORT/USERNAME (configMap RABBITMQ_DEFAULT_USER)/PASSWORD (secret RABBITMQ_DEFAULT_PASS)`, `RABBITMQ_HOST/PORT`, `KEEPGUARD_AUDIT_EXCHANGE=srv-audit-exchange-prod`, `USER_SERVICE_URL`, `AUTH_SERVICE_URL`, `KEEPGUARD_VALIDATION_MODERATION_ENABLED=false`.
  - liveness `GET /actuator/health/liveness` porta 8083, `initialDelaySeconds 30`, `periodSeconds 20`, `timeoutSeconds 3`, `failureThreshold 3`.
  - readiness `GET /actuator/health/readiness`, `20 / 10 / 3 / 3`.
- `templates/service.yaml`: Service `ms-company` porta 8083 → 8083 (ClusterIP).
- bff-core também checa `http://ms-company:8083/actuator/health/liveness` (`bff-core/application-prod.yml:156`).

### 10.3 Scripts
- `script-deploy-github-ms-company.sh`: argumentos `up`, `prod`, `merge <branch>`. Faz `git add -A` + commit `feat(ms-company): update ms-company <data>` + push; com merge/prod: checkout `main`, pull rebase, merge, push, volta à branch; `prod`: `mvn clean package -DskipTests`, `docker build --platform linux/amd64` com tags `<sha>`, `latest`, `main-latest` em `ghcr.io/keepguard/ms-company`, push, `kubectl set image deployment/ms-company ms-company=<img:sha> -n keepguard` (kubeconfig `keepguard-core/docker/keepguard-kubeconfig.yaml` ou `~/.kube/config`) e `rollout status --timeout=300s`.
- `script-deploy-k8s-prod.sh`: só `kubectl set image` com tag (arg ou SHA local), `rollout restart` se a tag contém `latest`, `rollout status --timeout=360s`.

### 10.4 Seed
- `scripts/seed_company_mfa_email_only.sql` (seção 5.2).

---

## 11. Testes existentes (especificação de regressão)

> Todos são unitários (JUnit 5 + Mockito); **não há teste de integração** com banco, Redis ou HTTP real. `src/test/resources/application-test.yml` (H2, `create-drop`, Rabbit excluído, `keepguard.audit.enabled: false`) não é usado por nenhum `@SpringBootTest`. Relatórios surefire em `target/surefire-reports/`.

(Ver subseção 11.1 — preenchida a partir da leitura de todos os arquivos de teste.)

### 11.1 Por arquivo

Caminhos relativos a `src/test/java/com/keepguard/ms_company/`. Última execução do surefire (10/set) sem falhas; nenhum `.java` mudou depois. Métricas são verificadas só pelo nome (tags com `any()`); nenhum teste usa `InOrder`. Marcados **[ESTRANHO]** os testes que fixam comportamento acidental ou são incoerentes.

**Domínio**
- `domain/entity/CompanyTest.java`: `create` gera id/codeCompany/tenantId, status `PENDING_APPROVAL`, listas vazias; `of` preserva ids; `addCnae` principal/secundário; `setPrincipalCnae`; `getActiveCnaes`; `removeCnae`; `approve()` com todos os filhos ativos → ACTIVE; sem cada filho → `ValidationException` com as 5 mensagens exatas (um item faltando por teste — ordem das checagens não fixada); transições reject/block → BLOCKED, deactivate ACTIVE→INACTIVE, activate INACTIVE→ACTIVE, suspend ACTIVE→SUSPENDED; `updateBasicInfo`, `updateTaxRegime`; CNPJ null/"" → "CNPJ é obrigatório" (DV inválido **não** testado); equals/hashCode por id; `toString` com `name/legalName/cnpj/status`; `isBlockedOrSuspended`; `validateStatusForOperations` passa em ACTIVE/INACTIVE/PENDING e lança com "Não é possível realizar operações na empresa com status 'Bloqueada'|'Suspensa'" + "Operações são permitidas apenas para empresas com status Ativa, Inativa ou Aguardando Aprovação". Massa (linhas 548-628): endereço "Rua Teste","123","Apto 1","Centro","São Paulo","SP","Brasil","01234567"; conta "001","1234","5","12345678","9",CORRENTE; CNAE "7020400"; contato "João Silva","joao@empresa.com","11999999999"; representante CPF "11144477735", 1980-01-01, "11988888888". [ESTRANHO] `:632-662` checa a mesma instância duas vezes.
- `domain/entity/AddressTest.java`: create ativo; IAE para street null/branco/151 chars, number null/21 chars, district/city/country null, estado "SPA", CEP null ou 7 dígitos; `ValidationException` "Estado é obrigatório"/"CEP é obrigatório"; normaliza "sp"→"SP", "01234-567"→"01234567", trim geral; complement pode ser null.
- `domain/entity/BankAccountTest.java`: create ativo; `of()` com code null/"" → "Código do banco é obrigatório"; IAE para code/agency/accountNumber/accountDigit nulos; `accountType` null → **NullPointerException**; trim de code/agency/accountNumber.
- `domain/entity/CnaeTest.java`: create ativo com timestamps; code null/""/branco → IAE ("Código CNAE é obrigatório"); "123-4567" → "1234567"; descrição vazia/501 chars → IAE; companyId null → NPE; `deactivate()` em principal → **IllegalStateException**; `setAsPrincipal` força ativo; `updateDescription/updateCode` mexem em updatedAt. [ESTRANHO] `:87-105` repete o mesmo código 3x.
- `domain/entity/ContactTest.java`: telefone "(11) 99999-9999" é **mantido formatado**; nome null/branco/101 chars → IAE; email null/"" → "Email é obrigatório"; "email-invalido" → `InvalidEmailException`; telefone null/"" → "Telefone é obrigatório"; "123" e 16×"1" → erro; email vai para minúsculas.
- `domain/entity/RepresentativeTest.java`: nome trim, email lower; RG/role opcionais; telefone formatado aceito e mantido; tabela de mensagens: nome → "Nome do representante é obrigatório" / "Nome deve ter no máximo 150 caracteres"; CPF → "CPF é obrigatório" / "CPF deve conter exatamente 11 dígitos"; RG 16 → "RG deve ter no máximo 15 caracteres"; nascimento null/amanhã/hoje-121 anos → "Data de nascimento é obrigatória"/"não pode ser futura"/"inválida"; email vazio → "Email do representante é obrigatório"; telefone vazio → "Telefone do representante é obrigatório"; "123456789" ou 16 dígitos → "Formato de telefone inválido. Use: (XX) XXXXX-XXXX ou (XX) XXXX-XXXX"; role 101 → "Cargo deve ter no máximo 100 caracteres"; email "email-invalido" → "Formato de email inválido: email-invalido"; local-part 151 → "Parte local do email muito longa (máximo 64 caracteres)". [ESTRANHO] `:98-128` nomes falam em "limpar formatação" mas o telefone fica formatado.

**Application services**
- `application/service/company/CompanyCommandServiceTest.java`: create chama `existsByCnpj`, `toDomain`, `save`, `provisionCompanyRoles(UUID)`, `toViewDTO`, métrica `company_created_total`; CNPJ duplicado → `AlreadyExistsException("CNPJ já cadastrado: 11222333000181")` + `company_business_errors_total`, sem save; update/approve/reject/activate/deactivate/suspend/block/delete com empresa inexistente → "Empresa não encontrada: <id>" + `company_not_found_total`; update em BLOCKED/SUSPENDED → `InvalidStatusForOperationException` sem save (ACTIVE/INACTIVE/PENDING permitidos); approve sem política ativa → "Empresa deve ter pelo menos uma política ativa para ser aprovada" + `company_business_errors_total`, sem save; transições e métricas `company_rejected|activated|deactivated|suspended|blocked|deleted_total`; delete faz `deleteById`; exceções de mapper/repositório propagadas sem wrap. Invalidação de cache **não verificada**. [ESTRANHO] `:562-619` nome cita métrica que não é verificada.
- `application/service/company/CompanyQueryServiceTest.java`: getById — hit não consulta repo nem grava; miss consulta, mapeia e grava `cacheCompanyById(id.toString(), view)`; não achou → "Empresa não encontrada: <id>" + `company_not_found_total`, sem gravar; erro de repo/mapper → `RuntimeException("Erro interno ao buscar empresa por ID")`. getByCnpj idem, sem normalizar CNPJ ("   " → "Empresa não encontrada:    "); [ESTRANHO] CNPJ null cai em NPE dentro do `orElseThrow` (`Map.of` com null) e vira "Erro interno ao buscar empresa por CNPJ". search repassa items/total/page/size; lista vazia não chama mapper; criteria null → "Erro interno ao buscar empresas". getSimpleByTenantId: hit retorna sem checar status; vazio → "Empresa não encontrada: <tenant>" errorCode `COMPANY_NOT_FOUND`; não ACTIVE → "Empresa não está ativa: <tenant>" errorCode `COMPANY_NOT_ACTIVE` + `company_invalid_status_total`, sem cache; ACTIVE → grava `cacheSimpleCompanyByTenantId`.
- `application/service/company/CompanyUseCaseServiceTest.java`: delegação pura (comandos → Command, consultas → Query).
- `application/service/address/AddressCommandServiceTest.java`: create desativa o ativo anterior com `save(existing)` (1 arg) — **um endereço ativo por empresa** —, depois `save(address, companyId)`, `companyRepository.save(company)`, `address_created_total`; empresa inexistente → "Empresa não encontrada: <id>" + **`address_business_errors_total`**; BLOCKED/SUSPENDED → exceção sem tocar repositório; update valida status da empresa, `address_updated_total`, inexistente → "Endereço não encontrado: <id>" + `address_not_found_total`; activate desativa outro ativo e **não consulta a empresa** (idem deactivate); delete com `existsById`/`deleteById`.
- `application/service/address/AddressQueryServiceTest.java`: getById com `findCompanyIdByAddressId`; erro de repo → "Falha ao buscar endereço" + `address_system_errors_total`; listByCompanyId grava cache **inclusive lista vazia**; getActiveByCompanyId com cache, não achou → "Endereço ativo não encontrado para a empresa: <companyId>"; listAll/search com lookup de companyId por item.
- `application/service/address/AddressUseCaseServiceTest.java`: delegação pura.
- `application/service/bankaccount/BankAccountCommandServiceTest.java`: espelho do Address (conta ativa exclusiva; métricas `bank_account_*`; "Dados bancários não encontrados: <id>"; "Empresa não encontrada para os dados bancários: <id>"; activate sem consultar empresa).
- `application/service/bankaccount/BankAccountQueryServiceTest.java`: espelho; "Falha ao buscar dados bancários"; "Dados bancários ativos não encontrados para a empresa: <id>"; search com `accountType` como String ("CORRENTE"/"POUPANCA") e sort `["code"]`.
- `application/service/cnae/CnaeCommandServiceTest.java`: exceções verificadas só como `RuntimeException`; create com duplicado → "CNAE já existe para esta empresa: <code>" + `cnae_business_errors_total`, sem save (código cru); update/activate/deactivate/setAsPrincipal/delete não encontrado → "CNAE não encontrado: <id>"; delete com 1 ativo → "Não é possível remover o último CNAE ativo da empresa", com 2 → `deleteById` + `cnae_deleted_total`; setAsPrincipal desmarca anterior (2 saves) + `cnae_set_principal_total`; deactivate principal → "Não é possível desativar o CNAE principal. Defina outro como principal primeiro.".
- `application/service/cnae/CnaeQueryServiceTest.java`: sem cache; "CNAE não encontrado: <id>" + `cnae_query_errors_total`; "CNAE principal não encontrado para empresa: <companyId>"; demais com `cnae_queried_total`. [ESTRANHO] `:104,:165,:275` verificam `never().toView` (método não usado).
- `application/service/contact/ContactUseCaseServiceTest.java`: delegação pura. **Não há testes de ContactCommandService/ContactQueryService.**
- `application/service/representative/RepresentativeCommandServiceTest.java`: create com empresa inexistente → "Empresa não encontrada" (sem id); CPF duplicado → `IllegalArgumentException("Representante com este CPF já existe para esta empresa")`; métricas com ponto `representative.created|updated|activated|deactivated|deleted`; update usa `save(rep)` 1 arg; activate/deactivate sem consultar empresa; NotFound "Representante não encontrado".
- `application/service/representative/RepresentativeQueryServiceTest.java`: métricas `representative.found.by_id|all|by_company|active_by_company|all_active|by_cpf|by_email|by_name|by_role`; mensagens "Representante não encontrado", "Representante ativo não encontrado para esta empresa", "Representante não encontrado com este CPF", "Representante não encontrado com este email"; cache só em by_company e active_by_company.

**Cache**
- `infrastructure/redis/CompanyCacheServiceTest.java`: chaves exatas `company_cache:tenantId:<lower>` ("TENANT-ABC" → "tenant-abc"), `company_cache:cnpj:<só dígitos>` ("12.345.678/0001-90" → "12345678000190"), `company_cache:simple:tenantId:<id>`; `set(key, json, 2592000, SECONDS)`; leitura `get` + `readValue(CompanyViewDTO.class)`; removes apagam só a chave correspondente; nunca usa chaves legadas `company_cache:xapp:`/`company_cache:simple:xapp:`.

**Mappers** (`application/mapper/*Test`, `adapters/in/rest/*/mapper/*Test`): entrada null → saída null; updates parciais (null mantém o valor existente, id e `active` preservados); `CompanyApplicationMapper.toDomain(update, existing)` mantém CNPJ e atualiza nome/razão/IE/IM/regime/ein, `toDomain(null, existing)` → existing, `toDomain(cmd, null)` → null; `toCompanyAddressDTO`/`toCompanyBankAccountDTO`/`toCompanyContactDTO` (só email/phone/website)/`toCompanyRepresentativeDTO` sem id. [ESTRANHO] `CnaeAdapterMapperTest:36-70` testes "sucesso" só passam null.

**Controllers** (`adapters/in/rest/address/AddressControllerTest.java`, `contact/ContactControllerTest.java`, `company/CompanyControllerTest.java`; chamada direta, sem MockMvc): create → 201 com corpo; update/get → 200; delete → 204 sem corpo; exceções do port propagadas (tratamento HTTP de erro **não** coberto). `MsCompanyApplicationTest.java`: só reflexão das anotações (`scanBasePackages`, `@EnableJpaRepositories`, `@Import(MetricsConfig)`).

**Massa válida dos builders** (`test/builder/`): CPFs `11144477735`, `98765432100`, `12345678909`, `55566677720`, `99988877714`; CNPJs `11222333000181`, `98765432000198`, `12345678000195`; endereço default "Rua das Flores","123","Sala 1","Centro","São Paulo","SP","Brasil","01234567" (+ presets RJ 20000000, MG 30000000, BA 40000000, DF 70000000); conta "001","1234","5","12345678","9",CORRENTE; CNAE default "1234567" (presets 4711301, 1011201, 6201500); contato "(11) 99999-9999"; representante "11999999999", 1990-01-01. `CompanyTestBuilder` usa `ein = stateRegistration` (`:167,:188`).

**Lacunas a cobrir no Go**: testes HTTP de contrato de erro (inexistentes hoje), DV inválido de CNPJ/CPF, invalidação de cache, Contact command/query, e testes de integração com Postgres/Redis (nenhum existe).

---

## 12. Pontos de atenção (comportamento atual — **não** corrigido; decidir caso a caso se o Go replica)

### 12.1 Bugs aparentes / riscos de dados
1. **`PUT /api/v1/companies/{id}` apaga todos os canais MFA**: `CompanyApplicationMapper.toDomain(update, existing)` cria um `Company.of` sem `mfaChannels` (`application/mapper/CompanyApplicationMapper.java:47-61`); o adapter faz o diff contra lista vazia e remove todos (`infra/persistence/CompanyRepositoryAdapter.java:52-77`, `orphanRemoval`).
2. **Criação de empresa não é transacional e não compensa**: se o ms-auth falhar, a empresa fica gravada e a API devolve 500 (`svc/company/CompanyCommandService.java:62-65`). `@Transactional` em método privado sem efeito (`:74-77`).
3. **Insert com id pré-gerado + `@GeneratedValue`**: domínio gera o UUID e o adapter chama `save` (merge) com id preenchido (`CompanyJpaMapper.toEntity`, `AddressJpaMapper.toEntity(address, companyId)`, `CnaeJpaMapper`, `ContactJpaMapper`, `RepresentativeJpaMapper`, `CompanyPolicyJpaMapper`). No Hibernate 6.6 o merge de entidade com id gerado inexistente no banco pode lançar `OptimisticLockException`/`ObjectOptimisticLockingFailureException` em vez de inserir [framework — **verificar em prod/logs se creates funcionam**; versões ≤6.5 inseriam]. No Go: INSERT explícito com UUID gerado na aplicação (comportamento pretendido).
4. **Desativar o endereço/conta ativa anterior usa `save` sem companyId** (`AddressCommandService.java:53-57,123-129`; `BankAccountCommandService.java:53-57,123-129`): o merge leva `company=null` sobre coluna `NOT NULL` → provável erro 500 ao criar/ativar o 2º endereço/conta de uma empresa [framework — verificar]. Intenção clara: desativar o anterior mantendo a empresa.
5. **Busca de empresa por `city`/`state` quebrada** (Specification usa `address` inexistente, `CompanyRepositoryAdapter.java:224-230`) → 500.
6. **Busca de contas por `accountType`** passa String contra enum (`BankAccountSpringRepository.java:40`) → provável 500 quando informado.
7. **Cache nunca invalidado nas escritas de filhos** e listas vazias cacheadas por 30 dias: `POST /addresses/company/{id}` depois de um `GET /addresses/company/{id}` continua mostrando a lista antiga até alguma mutação da empresa (update/approve/...). Idem bank accounts, contacts, representatives (`svc/*/…QueryService` + ausência de evict nos `…CommandService`).
8. **Delete de empresa deixa cache por cnpj/code/tenantId** (`CompanyCacheService.java:269-284`) → `GET /companies/x-tenant-id/{t}` pode retornar empresa apagada por até 30 dias.
9. **`getByTenantId` com cache**: o filtro "só ACTIVE" só vale no miss; mudanças de status invalidam a chave, então na prática é consistente, exceto pelo item 8.
10. **CNPJ formatado em `/companies/cnpj/{cnpj}`**: chave de cache normaliza (só dígitos), banco não (`CompanyQueryService.java:114-122`).
11. **Email de contato/representante inválido para a lib mas aceito pelo `@Email`** → `InvalidEmailException` (RuntimeException) → **500** com "Erro interno do servidor" (ex. `a@b.c` com TLD de 1 letra, domínio com `..`).
12. **Unicidade de email de contato**: checagem com valor cru (case-sensitive) mas gravação em minúsculas + `UNIQUE` → `Foo@x.com` quando já existe `foo@x.com` passa na checagem e explode no banco → 500 (`ContactCommandService.java:54-59`).
13. **CNAE**: não encontrado / duplicado / último ativo / desativar principal → **500** (RuntimeException/IllegalStateException) em vez de 404/409/400; update ignora `section/division/groupCode/classCode/subclassCode`; validador rejeita CNAEs que começam com `0`; `companyId` do path ignorado (sem checagem de pertença).
14. **Representante com CPF duplicado** → 400 (IAE), não 409. `createdAt/updatedAt` sempre `null` na resposta. Busca por email é case-sensitive contra valor em minúsculas. `findByCpf` global quebra (500) se o CPF existir em 2 empresas.
15. **Policy**: `DELETE` exige `?updatedBy=` (sem ele → 500) e responde 200 com corpo. Não valida existência da empresa nem pertença ao `companyId` do path. Desativação em massa ao ativar uma nova não incrementa versão das desativadas.
16. **Approve**: checa política ativa antes do status — empresa já ACTIVE sem política recebe 400 "Empresa deve ter pelo menos uma política ativa..." em vez do erro de transição.
17. **`validateStatusForOperations` (403)** só é aplicado em: company update; create/update de address, bank account, contact, representative; create/update de CNAE. **Não** em activate/deactivate/delete desses recursos, nem em MFA, nem em policies.
18. **Mensagens com `Set.toString()`** (UF e bancos) têm ordem não determinística.
19. `Company.validateName/LegalName` e similares medem tamanho **antes** do trim (string de 151 chars com espaço final é rejeitada).
20. `HealthController.health()`/`/api/v1/health` abrem conexão nova a cada chamada (custo).
21. `UndeclaredThrowableException` sem causa → NPE dentro do handler (`GlobalExceptionHandler.java:301`).
22. Métricas com tags de alta cardinalidade e conjuntos de tags inconsistentes (8.2).
23. `-Xmx1024m` vs limite 512Mi (10.1).
24. `application-prod.yml:40-43` com YAML mal indentado (`logging.port`/`enabled` com 4 espaços e `level` com 2) — provavelmente quebraria o boot se o profile `prod` fosse ativado; Helm usa `local`.

### 12.2 Contrato / serialização
- camelCase em tudo; nenhum snake_case no JSON das respostas de negócio. Exceções: `HelperController.info` usa `java_version`, `spring_version`.
- `null` serializado explicitamente (inclusive `address/contacts/representatives/bankAccount/cnaes` da Company, `complement`, `agencyDigit`, `rg`, `role`, `effectiveTo`, `createdAt/updatedAt` do representante).
- `PageResultDTO` com `totalPages` e `empty` derivados (validar).
- `LocalDateTime` sem fuso, com micro/nanossegundos variáveis; `timestamp` dos erros é `Instant` UTC com `Z`.
- Respostas indentadas (INDENT_OUTPUT) — clientes JSON não ligam, mas comparações byte a byte sim.
- Status HTTP atípicos: 403 para empresa bloqueada/suspensa; 500 para quase todo erro de framework (body inválido, UUID inválido, rota/método inexistente, param obrigatório ausente); 200 com corpo no DELETE de policy; 404 (não 403/409) para "Empresa não está ativa" no lookup por tenant.
- Contato usa PUT para activate/deactivate; demais recursos usam PATCH.
- `X-Correlation-ID` sempre devolvido (exceto nos 4 paths de actuator ignorados).

### 12.3 Normalização / tipos
- Trim em quase todos os textos de domínio; `complement`, `agencyDigit`, `rg`, `role` brancos viram `null`; `website/position/department` brancos ficam `""`.
- Email lower-case (contato e representante); UF upper-case; CNPJ/CPF/CEP/código de banco/CNAE só dígitos.
- Comparações de busca: `name/legalName/email/position/department` case-insensitive (`lower LIKE`); `city` de endereço `ILIKE`; `cnpj`, `zipCode`, `state`, `bankCode` case-sensitive/exatos.
- Não há BigDecimal no serviço. Datas: `LocalDate` (nascimento), `LocalDateTime` (timestamps, vigência de política).
- Timezone: `LocalDateTime.now()` da JVM (UTC no container) gravado sem fuso; `birthDate` "futura" avaliada com `LocalDate.now()` da JVM.
- UUIDs aceitos em maiúsculas no path (Spring converte); chaves de cache em minúsculas.
- Ordenação: só os `search` têm ordem default (company `name ASC`, address `city ASC`, contact `name ASC`, bank account nenhuma); listas não paginadas sem `ORDER BY`.
- Lazy loading: `findById` de Company dispara N queries (1 + até 6 coleções em lotes de 10); `listAll`/`search` de address/bank/contact fazem 1 query extra por item para obter `companyId`.
