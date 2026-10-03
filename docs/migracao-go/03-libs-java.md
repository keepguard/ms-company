# Levantamento das libs Java compartilhadas (lib-common, lib-validation, lib-security) → migração para Go

Data: 2026-10-03. Base: `keepguard-core/backend/ms/`. Todas as referências `arquivo:linha` são relativas a essa pasta, salvo indicação.
Abreviações: `LC` = `lib-common/src/main/java/com/keepguard/lib_common`, `LS` = `lib-security/src/main/java/com/keepguard/lib_security`, `LV` = `lib-validation/src/main/java/com/keepguard/lib_validation/moderation`.

---

## 0. Coordenadas, versões, publicação e consumo

| Lib | groupId:artifactId | Versão no pom da lib | Versão consumida pelos ms-* | Parent Spring Boot | Java |
|---|---|---|---|---|---|
| lib-common | `com.keepguard:lib-common` | `1.0.38-SNAPSHOT` (lib-common/pom.xml:11-13) | `1.0.37-SNAPSHOT` em todos (ex.: ms-user/pom.xml:121-122) — **pom da lib está 1 versão à frente dos consumidores** | 3.3.2 (lib-common/pom.xml:8) | 25 |
| lib-validation | `com.keepguard:lib-validation` | `1.0.3-SNAPSHOT` (lib-validation/pom.xml:15-17) | `1.0.3-SNAPSHOT` (só ms-user/pom.xml:188-189) | 3.5.3 (lib-validation/pom.xml:11) | 25 |
| lib-security | `com.keepguard:lib-security` | `1.0.4-SNAPSHOT` (lib-security/pom.xml:12-14) | `1.0.4-SNAPSHOT` (ms-billing/pom.xml:60-61, ms-knowledge/pom.xml:54-55, ms-user/pom.xml:82-83) | 3.5.3 (lib-security/pom.xml:8) | 25 |

**Dependências entre as libs: nenhuma.** Nenhum pom referencia outra lib KeepGuard. (lib-common usa reflexão para ler o `SecurityContextHolder` do Spring Security sem depender da lib-security — LC/audit/AuditContextCollector.java:144-172.)

Dependências externas relevantes:
- lib-common: spring-boot-starter, starter-validation, jackson-databind + jsr310, micrometer-core, aspectjweaver, spring-web, jakarta.servlet-api, **spring-boot-starter-amqp opcional** (lib-common/pom.xml:24-96).
- lib-validation: starter, starter-validation, starter-cache, okhttp 4.12.0, jackson, resilience4j 2.2.0 (spring-boot3, circuitbreaker, retry) (lib-validation/pom.xml:28-89).
- lib-security: starter-security, starter-oauth2-resource-server (não usado no código), jjwt 0.12.5 (api/impl/jackson), spring-web, spring-webmvc, servlet-api (lib-security/pom.xml:23-101).

**Como é publicada/instalada:**
- Os 3 poms têm um profile `github` com `distributionManagement` para GitHub Packages `https://maven.pkg.github.com/rafaelnogueirasoares/keepguard-template` (lib-common/pom.xml:186-195; lib-validation/pom.xml:204-213; lib-security/pom.xml:188-197) — profile **não ativo por padrão** (o ativo é `local`).
- Fluxo real: `mvn clean install` local → `~/.m2/repository/com/keepguard/lib-*` (script lib-common/script-update-lib-common.sh:134-145; o script também tenta `mvn versions:use-latest-versions` em `ms-auth ms-communication ms-company ms-user ms-terms` — lista desatualizada, linha 151, `ms-terms` não existe mais).
- No `~/.m2` local existem: lib-common até 1.0.38-SNAPSHOT (+ metadata de um Nexus antigo `nexus-keepguard-*`), lib-security até 1.0.4-SNAPSHOT, lib-validation até 1.0.3-SNAPSHOT. `~/.m2/settings.xml` declara `github`, `nexus-keepguard-*` e mirror `central`.
- **Dockerfile não compila nada**: só `COPY target/*.jar app.jar` (ms-user/Dockerfile:18-19; ms-billing/Dockerfile:18-19; igual nos outros). O JAR é gerado no Mac por `mvn clean package -DskipTests` dentro do script de deploy (ms-user/script-deploy-github-ms-user.sh:134). Ou seja, **as libs são resolvidas do `~/.m2` da máquina do Rafael** — sem `install` prévio da lib, o build do ms quebra ou pega SNAPSHOT velho.

**Como os ms-* ativam as libs:**
- lib-common **não tem** `AutoConfiguration.imports`; os beans (`@Component/@Service/@Configuration/@Aspect`) só entram porque cada ms faz `scanBasePackages = {..., "com.keepguard.lib_common"}`: ms-auth/.../MsAuthApplication.java:9, ms-billing/.../MsBillingApplication.java:12, ms-communication/.../MsCommunicationApplication.java:9, ms-company/.../MsCompanyApplication.java:10, ms-knowledge/.../MsKnowledgeApplication.java:12, ms-user-consents/.../MsUserConsentsApplication.java:9, ms-user/.../MsUserApplication.java:11.
- lib-security: autoconfig via `META-INF/spring/...AutoConfiguration.imports` (lib-security/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports:1) + `@EnableJwtSecurity` (ms-billing, ms-knowledge, ms-user). ms-billing e ms-user **também** fazem component-scan de `com.keepguard.lib_security` (MsBillingApplication.java:12, MsUserApplication.java:11), o que registra `JwtService/SecurityContext/JwtAuthenticationFilter` duas vezes (por `@Component` e por `@Bean`) — funciona por override de bean, mas é ruído.
- lib-validation: autoconfig via imports (lib-validation/src/main/resources/META-INF/spring/...imports:1), ligada por `keepguard.validation.moderation.enabled=true` (ms-user/src/main/resources/application.yml:142-160).

---

## 1. Classe a classe

### 1.1 lib-common (34 classes em src/main)

Propósito declarado: "Biblioteca compartilhada para microserviços KeepGuard" (lib-common/pom.xml:15). Na prática agrupa 5 coisas: validações brasileiras/e-mail/senha, exceções de domínio, logging+auditoria por AOP (publica no RabbitMQ do srv-audit), métricas Micrometer por AOP, e enums de comunicação.

| Classe | Tipo | O que faz exatamente / regras | Spring? |
|---|---|---|---|
| `LibCommonApplication` (LC/LibCommonApplication.java:6-12) | config | `@SpringBootApplication` com `main` dentro de uma lib. Sem função. | Sim |
| `utils.BrazilianValidationUtils` (LC/utils/BrazilianValidationUtils.java) | util | Métodos estáticos que lançam `ValidationException`. **CNPJ** (40-60): null/blank → "CNPJ não pode ser nulo ou vazio"; remove `\D`; precisa casar `^\d{14}$` → "CNPJ deve conter exatamente 14 dígitos"; todos iguais `(\d)\1{13}` → "CNPJ inválido: todos os dígitos são iguais"; DV com pesos `{5,4,3,2,9,8,7,6,5,4,3,2}` e `{6,5,4,3,2,9,8,7,6,5,4,3,2}`, `dv = soma%11<2 ? 0 : 11-soma%11` (222-240) → "CNPJ inválido: dígitos verificadores incorretos". **CPF** (68-88): mesmas regras com 11 dígitos, pesos `{10..2}` e `{11..2}` (242-260); mensagens análogas com "CPF". **CEP** (96-111): 8 dígitos `^\d{8}$`, rejeita todos iguais. **Telefone** (119-129): regex `^\(?[1-9][1-9]\)?\s?[1-9][0-9]{3,4}-?[0-9]{4}$` sobre o valor trimado (não limpa) → "Formato de telefone inválido. Use: (XX) XXXXX-XXXX ou (XX) XXXX-XXXX". **UF** (137-147): set de 27 siglas, `trim().toUpperCase()`. **Código bancário** (155-165): whitelist fixa `001,033,104,237,341,356,422,748,756`. **CNAE** (173-188, 262-291): 7 dígitos; seção 1-9, divisão 01-99, grupo 001-999 (checagem "hierárquica" fraca). `isValidCnpj/isValidCpf` (196-218) = versões booleanas. | Não |
| `utils.ValidationUtils` (LC/utils/ValidationUtils.java) | util | `validateAndParseUUID` (24-34): null → `IllegalArgumentException("ID não pode ser nulo")`; inválido → "ID inválido: X. Deve ser um UUID válido.". `validateEmail` (42-56): regex `^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$`, domínio ≥3 chars e sem `..`, total ≤254, local ≤64; lança `InvalidEmailException` com msgs "Email não pode ser nulo ou vazio", "Formato de email inválido: X", "Domínio de email muito curto: X", "Domínio de email inválido: X", "Email muito longo (máximo 254 caracteres)", "Parte local do email muito longa (máximo 64 caracteres)". Políticas de senha (100-158): Basic ≥8; Intermediate ≥8 + maiúscula + minúscula + dígito; Strong + especial `[!@#$%^&*()_+\[\]{};':|,.<>/?-]`; Advanced ≥12 + não conter username/parte local do e-mail + blacklist de 8 senhas comuns. `validateTenantId` (196-200): obrigatório e UUID, lança `InvalidTenantIdException("Header X-Tenant-Id é obrigatório")` / "...deve ser um UUID válido". | Não |
| `utils.CodeGeneratorUtils` (LC/utils/CodeGeneratorUtils.java) | util | `SecureRandom`. `generateSixDigitCode` (18-21) = 100000–999999. `generateNumericCode(4..10)`, `generateAlphanumericCode(4..20)` com `A-Z0-9`, `generateCustomCode`. | Não |
| `utils.DateConverter` (LC/utils/DateConverter.java:8-63) | util | `@Component`; parse/format `dd/MM/yyyy HH:mm:ss`. | Sim (só a anotação) |
| `utils.StringSanitizer` (LC/utils/StringSanitizer.java:31-67) | util | trim, tab→espaço, colapsa espaços, máx. 2 `\n` seguidos; variante multiline. | Não |
| `exception.ValidationException` (LC/exception/ValidationException.java:6) | exception | `extends IllegalArgumentException`. | Não |
| `exception.InvalidEmailException` / `InvalidPasswordException` | exception | `RuntimeException` simples. | Não |
| `exception.InvalidTenantIdException` (LC/exception/InvalidTenantIdException.java:4-6) | exception | default "Header X-Tenant-Id é obrigatório e deve ser um UUID válido." | Não |
| `exception.InvalidStatusException` (LC/exception/InvalidStatusException.java:32-69) | exception | carrega entityType/currentStatus/expectedStatus; mensagens "%s com status '%s' não pode realizar esta operação. Status esperado: '%s'", "%s com status '%s' não pode ser %s" etc. | Não |
| `communication.enums.CommunicationTypeEnum` | dto/enum | email, sms, push_notification, whatsapp, telegram, sendgrid, push (LC/communication/enums/CommunicationTypeEnum.java:4-10) | Não |
| `communication.enums.MessageTypeEnum` | dto/enum | email, sms, push_notification, whatsapp, push | Não |
| `communication.enums.TemplateTypeEnum` | dto/enum | 16 templates (`autenticacao_email_token`, ..., `fatura_paga`, `fatura_vencida`) (LC/communication/enums/TemplateTypeEnum.java:4-19) — **contrato com ms-communication e srv-email/sms-sender** | Não |
| `config.MetricsConfig` (LC/config/MetricsConfig.java:9-21) | config | Registra `MetricsService` e `MetricsAspect`. ms-* fazem `@Import(MetricsConfig.class)`. | Sim |
| `metrics.service.MetricsService` (LC/metrics/service/MetricsService.java) | util | Wrapper Micrometer. `recordSuccess/recordError` → counter `<metricName>` (normalmente `api_requests_total`) + timer `api_requests_latency_seconds` com tags `endpoint, application, status=success|error` (51-80). `recordMessageSend` → `message_send_total`, `message_send_success|error` (83-104). | Sim (Micrometer) |
| `metrics.annotation.@MetricsEndpoint` (LC/metrics/annotation/MetricsEndpoint.java:12-31) | anotação | `endpoint`, `applicationParam`, `operation`. | — |
| `metrics.aspect.MetricsAspect` (LC/metrics/aspect/MetricsAspect.java:29-108) | filter/aspect | `@Around` cronometra o método; `application` = 1º arg String sem `-` e <50 chars (se `applicationParam`), senão header `X-Tenant-Id`, senão `"none"`. | Sim (AOP) |
| `logging.annotation.@LogOperation` (LC/logging/annotation/LogOperation.java:26-62) | anotação | `operation, description, contextProvider, audit=true, auditAction, auditEntityType="ENTITY"`. | — |
| `logging.annotation.@LogQuery` / `@LogAudit` | anotação | Interceptadas pelo aspect; **ninguém usa** (0 ocorrências). | — |
| `logging.aspect.LoggingAspect` (LC/logging/aspect/LoggingAspect.java) | filter/aspect | Em `@LogOperation`: monta contexto por reflexão dos args (UUID/String/Number/Boolean; DTOs via getters `username, deviceId, codeUser, companyId, userId, challengeSessionId` — 195-274), loga início/sucesso/erro, conta métricas e **por padrão publica auditoria** (`audit=true`) com outcome SUCCESS/FAILURE (88-113). Resolve entityId por regras para DEVICE/USER (231-265). Substitui `{chave}` na descrição (342-356). | Sim (AOP) |
| `logging.service.LoggingService` (LC/logging/service/LoggingService.java) | util | Logs + métricas `operations_total{operation,status}`, `business_errors_total{error_type}`, `validation_errors_total{field}`, `queries_total` (49-152). `logAudit` → `StructuredLogger.logAudit` + `publishAudit` (186-222): **descarta** `REFRESH_TOKEN` e `OAUTH_TOKEN_ISSUE` com SUCCESS (199-204); monta `AuditEvent` e chama `publishAsync`. `sourceService` = `keepguard.audit.source-service` ou `spring.application.name` (35-36). | Sim |
| `logging.StructuredLogger` (LC/logging/StructuredLogger.java) | util | Coloca chaves no MDC (`requestId, operation, application, auditAction, errorCode, duration, status...`) e loga. | Sim (`@Component`) |
| `logging.LoggingContext` (LC/logging/LoggingContext.java:26-39) | util | AutoCloseable que põe `correlationId/requestId/operation/application/timestamp` no MDC. **Sem uso.** | Não (SLF4J) |
| `audit.AuditEvent` (LC/audit/AuditEvent.java:18-69) | dto | Envelope publicado para o srv-audit (ver JSON na seção 4). `schemaVersion=1`. | Não (Jackson/Lombok) |
| `audit.AuditEventPublisher` (LC/audit/AuditEventPublisher.java:7-10) | port | `publishAsync(AuditEvent)`. | Não |
| `audit.RabbitAuditEventPublisher` (LC/audit/RabbitAuditEventPublisher.java) | adapter | Executor assíncrono; declara exchange **topic durable** uma vez (71-80); publica JSON `PERSISTENT`, header `X-Correlation-ID`; exchange default `srv-audit-exchange-local`, routing key `audit.event` (37, e RabbitAuditConfiguration.java:40-41). Erros só logados. | Sim (spring-amqp) |
| `audit.RabbitAuditConfiguration` (LC/audit/RabbitAuditConfiguration.java:17-45) | config | Ativa se `RabbitTemplate` no classpath e `keepguard.audit.enabled` (default true). Pool 2-4 threads, fila 500, **descarta silenciosamente** quando cheia (29). | Sim |
| `audit.AuditPublisherFallbackConfiguration` + `NoOpAuditEventPublisher` | config | Fallback no-op. | Sim |
| `audit.AuditContextCollector` (LC/audit/AuditContextCollector.java:15-84) | util | Monta o evento: correlationId = MDC `correlationId` → header `X-Correlation-ID` → UUID novo; codeUser = MDC `codeUser` → header `X-User-ID` → `sub` do JWT; tenantId = MDC `tenantId` → `X-Tenant-Id` → claim → MDC `companyId` → `X-Company-Id`; clientIp = 1º `X-Forwarded-For` ou remoteAddr; reason truncado a 512. | Sim (RequestContextHolder) |
| `audit.AuditChangeExtractor` (LC/audit/AuditChangeExtractor.java:12-56) | util | Para `USER_EMAIL_UPDATED` grava `email` mascarado (`a***@dominio`); para `USER_ROLE_ADDED/REMOVED` grava `role`. | Não (reflexão) |

### 1.2 lib-validation (13 classes)

Propósito: "Advanced Validation Library - Content Moderation, Document Validation, Image Validation" (lib-validation/pom.xml:19) — **só moderação de conteúdo existe**; validação de documento/imagem nunca foi implementada.

| Classe | Tipo | O que faz / regras | Spring? |
|---|---|---|---|
| `@ModeratedContent` (LV/application/validator/ModeratedContent.java:30-71) | anotação (Bean Validation) | `message="Conteúdo impróprio detectado"`, `categories` (vazio = todas), `threshold=0.7`, `async=false`. | Jakarta Validation |
| `ModeratedContentValidator` (LV/application/validator/ModeratedContentValidator.java) | validator | Pega `ContentModerationService` de um `ApplicationContext` estático (28, 37-64). Null/blank → válido (83). **Fail-open** se serviço ausente (75-80) ou exceção (127-131). Viola se `exceedsThreshold`. Mensagem: "Seu conteúdo contém linguagem inapropriada (%s). Por favor, revise sua mensagem e evite usar linguagem ofensiva." com descrições PT das categorias, ou "Seu conteúdo contém linguagem inapropriada. Por favor, revise sua mensagem." (142-168). **Loga o conteúdo do usuário em INFO** (68, 87, 107) — PII em log. | Sim |
| `ContentModerationService` (LV/application/service/ContentModerationService.java) | service | `validate`, `validateAsync`, `validateOrThrow` (lança `ContentViolationException("Conteúdo impróprio detectado")`, 84-93), `isAppropriate`. | Não |
| `ContentViolationException` | exception | carrega `ModerationResult`. | Não |
| `ModerationException` | exception | erro de chamada à API. | Não |
| `ModerationCategory` (LV/domain/model/ModerationCategory.java:15-69) | enum | 11 categorias OpenAI (`sexual`, `sexual/minors`, `hate`, `hate/threatening`, `self-harm`, `self-harm/intent`, `self-harm/instructions`, `violence`, `violence/graphic`, `harassment`, `harassment/threatening`) + descrição PT. | Não |
| `ModerationResult` (LV/domain/model/ModerationResult.java:17-82) | dto/record | `exceedsThreshold`: sem categorias → qualquer score ≥ threshold; com categorias → score da categoria ≥ threshold (52-65). `safe()` = resultado não-flagged. | Não |
| `ContentModerationPort` | port | `moderate`, `moderateAsync`, `isFlagged`. | Não |
| `OpenAIModerationAdapter` (LV/infrastructure/adapter/OpenAIModerationAdapter.java) | adapter | POST `{model, input}` em `apiUrl` com `Bearer apiKey`; parse `results[0].flagged/categories/category_scores` (91-125). | Não (OkHttp) |
| `ResilientContentModerationPort` (LV/infrastructure/resilience/...:45-65) | adapter/decorator | Retry + circuit breaker Resilience4j; em falha final devolve `ModerationResult.safe()` (fail-open). | Não (Resilience4j) |
| `CachedContentModerationPort` (LV/infrastructure/cache/...:38-56) | adapter/decorator | Cache por SHA-256 do conteúdo. **O `ttl` é recebido e nunca usado** (29-35) e o `CacheManager` é `ConcurrentMapCacheManager` (ModerationAutoConfiguration.java:121-124) → cache **sem TTL e sem limite** (cresce para sempre). | Sim (Spring Cache) |
| `ModerationProperties` (LV/infrastructure/config/ModerationProperties.java) | config | prefixo `keepguard.validation.moderation`; defaults: url `https://api.openai.com/v1/moderations`, model `omni-moderation-latest`, timeout 5s, threshold 0.7, cache 24h, CB 5/30s, retry 3×500ms. | Sim |
| `ModerationAutoConfiguration` (LV/infrastructure/config/ModerationAutoConfiguration.java:33-125) | config | Monta a cadeia Cache→Resilient→OpenAI; CB `failureRateThreshold=50`, `slidingWindowSize=failureThreshold` (82-87). | Sim |

### 1.3 lib-security (7 classes)

Propósito: "Biblioteca de segurança JWT para microserviços KeepGuard" (lib-security/pom.xml:16). Valida o JWT HS256 emitido pelo ms-auth.

| Classe | Tipo | O que faz / regras | Spring? |
|---|---|---|---|
| `@EnableJwtSecurity` (LS/annotation/EnableJwtSecurity.java:49-54) | anotação | `@Import(JwtSecurityAutoConfiguration.class)`. | Sim |
| `@PublicEndpoint` (LS/annotation/PublicEndpoint.java:40-52) | anotação | Marca método de controller como público; `value` = descrição. | — |
| `JwtSecurityAutoConfiguration` (LS/autoconfigure/JwtSecurityAutoConfiguration.java) | config | Ativa com `keepguard.security.jwt.enabled` (default true, 77-82). Cria `JwtService`, `SecurityContext`, `JwtAuthenticationFilter`, `SecurityFilterChain` (stateless, CSRF off, formLogin/httpBasic off, filtro antes do `UsernamePasswordAuthenticationFilter`) (134-181). Permite `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/swagger-ui.html` + rotas `@PublicEndpoint` descobertas por varredura do `RequestMappingHandlerMapping` (210-260); resto `authenticated()`. CORS: `allowedOriginPatterns=*`, métodos GET/POST/PUT/DELETE/PATCH/OPTIONS, headers `*`, `allowCredentials=true`, maxAge 3600 (187-200). `@EnableMethodSecurity(securedEnabled, jsr250Enabled)` (84). | Sim (Spring Security) |
| `JwtAuthenticationFilter` (LS/filter/JwtAuthenticationFilter.java) | filter | Ver seção 3. | Sim |
| `JwtService` (LS/service/JwtService.java) | util | Chave `Keys.hmacShaKeyFor(secret.getBytes())` (51-55) — **bytes crus da string, não decodifica base64**. `validateToken`: assinatura+exp via jjwt + issuer se `validateIssuer` (71-109). Extratores: `extractUserId` = `sub` como UUID (118-126); `extractRoles` = claim `roles` (135-144); `extractAuthorities` = `authorities` (153-162); `extractTenantId` = `tenant_id` → `tenantId` → `company_id` → `companyId` (186-204); `extractClientId` = `client_id` ou `"unknown"` (212-221); `extractAgentId`/`extractAgentCode` = `agent_id`/`agent_code` (226-252); `extractIssuer`, `getExpiration`. Cada extrator re-parseia e re-verifica o token (261-267). | Não (jjwt), só `@Service` |
| `SecurityContext` (LS/context/SecurityContext.java) | util/contexto | `ThreadLocal` com codeUser, roles, authorities, clientId, tenantId (53, 64-78). `hasRole/hasAuthority/hasAnyRole/hasAnyAuthority` retornam **true para qualquer pergunta se o usuário tiver `ROLE_ADMIN` ou `ROLE_SYSTEM`** (149-210). | Sim (`@Component`) |
| `JwtSecurityProperties` (LS/properties/JwtSecurityProperties.java:27-71) | config | prefixo `keepguard.security.jwt`: `enabled=true`, `secret` **default hardcoded** `Q2FjaG9ycm8gU2VndXJhbmNhIFByb2pldG8gS2VlcEd1YXJkIQ==` (47), `expiration=3600000`, `issuer="ms-auth"`, `validateIssuer=true`. | Sim |

---

## 2. Matriz de uso (imports em `src/main`, contagem de arquivos que importam; `+FQN` = uso por nome qualificado sem import)

Fonte: `grep import com.keepguard.lib_*` em `ms-*/src/main`. **ms-ai-guardian não usa nenhuma lib** (0 imports, sem dependência no pom).

| Classe | ms-auth | ms-billing | ms-communication | ms-company | ms-knowledge | ms-user | ms-user-consents | Total |
|---|---|---|---|---|---|---|---|---|
| **lib-common** | | | | | | | | |
| `@MetricsEndpoint` (anotações aplicadas) | 11 arq / 71 usos | – | 3 / 20 | 7 / 74 | 8 / 16 | 6 / 37 | – | 218 usos |
| `@LogOperation` (anotações aplicadas) | 8 / 45 | – | 5 / 15 | 8 / 50 | 6 / 8 | 5 / 26 | 2 / 9 | 153 usos |
| `MetricsService` | 1 (MetricsAdapter) | – | 1 | 1 | 1 | 1 | 1 | 6 |
| `MetricsConfig` (`@Import`) | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 7 |
| `LoggingService` | – | – | – | 1 (GlobalExceptionHandler) | 1 (GlobalExceptionHandler) | – | – | 2 |
| `ValidationUtils` | 4 | – | 3 | 2 | – | 5 | 3 | 17 (só `validateAndParseUUID` ×9 e `validateEmail` ×2 chamados) |
| `BrazilianValidationUtils` | – | – | – | 7 | – | 1 (+1 FQN em PersonProfile.java:170) | – | 9 |
| `CodeGeneratorUtils` | 2 | – | – | – | – | 1 | – | 3 (só `generateSixDigitCode`) |
| `ValidationException` | – | – | – | 9 (+FQN CompanyCommandService.java:49) | 1 | 16 | – | 26 |
| `InvalidStatusException` | – | – | – | 2 | – | – | – | 2 |
| `InvalidTenantIdException` | 1 | – | – | – | – | 1 | – | 2 |
| `InvalidEmailException` | 1 | – | – | – | – | – | – | 1 |
| `InvalidPasswordException` | 2 | – | – | – | – | – | – | 2 |
| `CommunicationTypeEnum` | 4 | – | 42 | – | – | – | – | 46 |
| `MessageTypeEnum` | 6 | – | 30 | – | – | – | – | 36 |
| `TemplateTypeEnum` | 6 | – | 29 | – | – | – | – | 35 |
| `AuditEvent` / `AuditEventPublisher` | – | 1 / 1 (BillingAuditAdapter) | – | – | – | – | – | 2 |
| `@LogQuery`, `@LogAudit` | 0 | 0 | 0 | 0 | 0 | 0 | 0 | **morto** |
| `LoggingContext`, `DateConverter`, `StringSanitizer`, `LibCommonApplication` | 0 | 0 | 0 | 0 | 0 | 0 | 0 | **morto** |
| `StructuredLogger`, `LoggingAspect`, `MetricsAspect`, `AuditContextCollector`, `AuditChangeExtractor`, `RabbitAudit*`, `NoOp*` | uso interno (via scan/aspect) | | | | | | | interno |
| **lib-security** | | | | | | | | |
| `@EnableJwtSecurity` | – | 1 | – | – | 1 | 1 | – | 3 |
| `@PublicEndpoint` (anotações aplicadas) | – | 2 arq / 3 usos | – | – | – | 4 arq / 14 usos | – | 17 |
| `SecurityContext` | – | 1 + 3 FQN (Entitlements/Plan/InvoiceController:25-33) | – | – | 1 (KnowledgeAccess) | 4 | – | 9 |
| `JwtService` (direto) | – | – | – | – | 1 (KnowledgeIngestSupport: `extractAgentId/AgentCode`) | – | – | 1 |
| `JwtAuthenticationFilter`, `JwtSecurityAutoConfiguration`, `JwtSecurityProperties` | interno | | | | | | | interno |
| **lib-validation** | | | | | | | | |
| `@ModeratedContent` | – | – | – | – | – | 3 arq / 5 usos (PersonRequestDTO.java:27, RegisterInitRequestDTO.java:31, CompanyRequestDTO.java:22,43; todas `categories={HATE,HARASSMENT,SEXUAL}, threshold=0.15`) | – | 5 |
| `ModerationCategory` (static import) | – | – | – | – | – | 3 | – | 3 |
| `ContentViolationException`, `ModerationResult` | – | – | – | – | – | 1 (GlobalExceptionHandler) | – | 1 |

Observações de código morto / quase-morto em produção:
- `ValidationUtils.validatePassword*` e `validateTenantId` — 0 chamadas em main (ms-user tem `validateTenantId` próprio em User.java:81 e RegisterSession.java:163).
- `CodeGeneratorUtils.generateNumericCode/generateAlphanumericCode/generateCustomCode` — 0 chamadas.
- `LoggingService.measureOperation/logCrudOperation` — 0 chamadas.
- `SecurityContext.hasAnyRole/hasAnyAuthority/getTenantIdAsUuid` — 0 chamadas; `securityContext.getCodeUser/getTenantId/getClientId` — 0 chamadas via `securityContext.` (o usuário vem de header `X-User-Id`/`X-Company-Id` nos controllers). Usados: `hasRole` ×13, `hasAuthority` ×2, `getRoles` ×1, `getAuthorities` ×1.
- `JwtService.extractIssuer`, `getExpiration` — 0 chamadas.
- `ContentModerationService.validateOrThrow/isAppropriate`, `ContentModerationPort.isFlagged` — 0 chamadas. Consequência: **`ContentViolationException` nunca é lançada**, logo o handler em ms-user/.../GlobalExceptionHandler.java:91-122 é morto. `@ModeratedContent(async=true)` nunca é usado.
- Toda a lib de logging tem os loggers desligados em vários ms: `LoggingService`, `MetricsAspect`, `StructuredLogger` = `OFF` em ms-auth/ms-communication/ms-company/ms-user-consents logback-spring.xml:49-51 (e repetido nos profiles), ms-user application-dev.yml:84-86. A parte que realmente importa do aspect é **métrica + publicação de auditoria**.

Testes (src/test) também importam: ms-auth (ValidationUtils, InvalidPassword/Email, enums), ms-communication (enums, ValidationUtils), ms-company (ValidationException, InvalidEmailException via FQN), ms-knowledge (SecurityContext, LoggingService), ms-user (ValidationException, SecurityContext). Os testes de `MsXxxApplicationTest` assertam o `scanBasePackages` com `com.keepguard.lib_common` (ms-auth MsAuthApplicationTest.java:73,90; ms-company MsCompanyApplicationTest.java:73,107; ms-user MsUserApplicationTest.java:78-79,96).

**investbot e achadinhos**: 0 arquivos `.java` e 0 `pom.xml` (find em `investbot/` e `achadinhos/`). Nenhum uso das libs fora de keepguard-core/backend/ms.

---

## 3. lib-security — fluxo de autenticação completo

### 3.1 Emissor (ms-auth, que NÃO usa lib-security; tem JwtService próprio)
`ms-auth/src/main/java/com/keepguard/ms_auth/infrastructure/config/security/JwtService.java`
- Chave: `Keys.hmacShaKeyFor(secret.getBytes())` com `security.jwt.secret=${JWT_SECRET}` (JwtService.java:16-30; ms-auth application.yml:78-79). Mesmo esquema de bytes crus da lib.
- **Token de usuário** (45-71): `alg=HS256`, `iss="ms-auth"`, `aud=[client_id]`, `jti=UUID`, `sub=codeUser`, `roles`, `authorities`, `client_id`, `tenant_id`, `login_method="password"`, opcionais `device_id`, `sid`; `iat`; `exp = agora + access-expiration` (default 900000 ms = 15 min, linha 22).
- **Token de serviço / client_credentials** (84-114): `sub=clientUuid`, `roles`, `authorities`, `client_id`, **`company_id`** (não `tenant_id`), `token_type="service"`, `login_method="client_credentials"`, opcionais `agent_id`, `agent_code`; `exp = agora + ttlMillis`.
- `client_id` saneado: vazio → `keepguard-default-client`; com vírgula → 1º item (190-197).

### 3.2 Validador (lib-security, nos ms-billing / ms-knowledge / ms-user)
Ordem por requisição (LS/filter/JwtAuthenticationFilter.java:73-167):
1. Paths ignorados por prefixo: `/actuator/`, `/swagger-ui/`, `/v3/api-docs/`, `/swagger-ui.html` (65-70, 195-205) → segue sem autenticar.
2. `Authorization` precisa começar com `Bearer ` (case-sensitive, 175-187). **Sem token → segue a cadeia** (90-94); quem decide é o `SecurityFilterChain`: rota `@PublicEndpoint` passa, demais caem em `anyRequest().authenticated()` (JwtSecurityAutoConfiguration.java:174). Como formLogin/httpBasic estão desligados e não há entry point configurado, a resposta é a padrão do Spring Security (a confirmar em runtime; provavelmente 403 sem corpo), **não** o JSON da lib.
3. `JwtService.validateToken`: jjwt `verifyWith(key)` (assinatura HMAC + `exp`) + issuer igual a `keepguard.security.jwt.issuer` (default `ms-auth`) se `validate-issuer` (JwtService.java:71-109). jjwt aceita qualquer HS* compatível com o tamanho da chave (não fixa HS256). **Não valida `aud`, `jti`, `sid`, revogação, `token_type`.** Inválido → 401 `{"error":"Token inválido ou expirado"}`.
4. Extrai `sub`(UUID), `roles`, `authorities`, `client_id`, tenant (`tenant_id`→`tenantId`→`company_id`→`companyId`) (104-108). `sub` não-UUID → exceção → 401 `{"error":"Erro na autenticação"}` (152-156).
5. Popula `SecurityContextHolder` com `UsernamePasswordAuthenticationToken(principal=codeUser.toString(), authorities = claim "authorities")` (114-124) — **as roles não viram GrantedAuthority**, só as authorities; e o `SecurityContext` ThreadLocal próprio (127) + MDC `codeUser`, `tenantId` (128-133).
6. Isolamento de tenant (RN-AUTH-02, 135-147): se veio header `X-Tenant-Id` e o usuário **não** tem `ROLE_ADMIN`/`ROLE_SYSTEM`, exige igualdade (case-insensitive ou UUID equivalente, 228-244) com o tenant do token; senão 403 `{"error":"Inconsistência de isolamento de tenant"}`. Sem header → não checa.
7. `finally`: limpa ThreadLocal, `SecurityContextHolder` e MDC (159-166).

Respostas de erro da lib (214-226): `Content-Type: application/json`, corpo `{"error":"<mensagem>"}` montado com `String.format` sem escape.

Configuração real nos ms: `keepguard.security.jwt.secret: ${JWT_SECRET:dGhpcy1pcy1hLXZlcnktc2VjdXJlLWFuZC1sb25nLWVub3VnaC1zZWNyZXQta2V5LTMyeC1jaGFycw==}`, `issuer: ms-auth`, `validate-issuer: true` (ms-user application.yml:132-139; ms-billing application.yml:90-96; ms-knowledge application.yml:191-197). **Há dois fallbacks de secret hardcoded** (o do yml e o da lib, JwtSecurityProperties.java:47) — se `JWT_SECRET` faltar no pod, o serviço sobe aceitando tokens assinados com chave pública no Git.

Autorização: não há `@PreAuthorize`/roles por rota na lib; cada controller testa manualmente `securityContext.hasRole("ROLE_ADMIN") || hasRole("ROLE_SYSTEM")` (ms-billing SubscriptionController.java:119, EntitlementsController.java:45, InvoiceController.java:59, PlanController.java:50-57 com `ROLE_ORGANIZATION_ADMIN`, `billing:write`, `billing:admin`; ms-user InternalUserController.java:48,71; ms-knowledge KnowledgeAccess.java:40,50). Atenção: `hasRole(X)` devolve true para admin/system em qualquer X (SecurityContext.java:149-154).

### 3.3 Propagação de contexto e serviço-a-serviço
- O BFF (Go, bff-core) é a borda: valida o JWT do usuário, depois chama os ms com `X-Company-Id`, `X-Correlation-ID`, às vezes `X-Tenant-Id`/`X-User-ID` (contagem por client em `keepguard-core/backend/bff/bff-core/internal/adapters/outbound/http/client/*.go`). Os controllers dos ms leem `@RequestHeader("X-Company-Id")` (59 ocorrências em ms-user/ms-company/ms-billing) e `X-User-Id` (10).
- Para ms protegidos por lib-security, o BFF envia `Authorization: Bearer` de duas formas: repassa o token do usuário (`BearerTokenFromContext`, billing_client.go:39-40, llm_client.go:38-39) ou obtém **token de serviço client_credentials por company** no ms-auth com cache e renovação 10 min antes de expirar (bff_oauth_token.go:63-131). Esse token traz `company_id`, por isso a lib aceita `company_id` como tenant (JwtService.java:194-198).
- ms-knowledge usa `agent_id`/`agent_code` do token de serviço para ingestão de agentes coletores (KnowledgeIngestSupport.java:28,39).
- Correlation-ID **não** está na lib-security nem na lib-common como filtro: cada ms tem seu `CorrelationIdFilter` copiado (ms-user/.../infrastructure/filter/CorrelationIdFilter.java:46-61: lê `X-Correlation-ID` ou gera UUID, devolve no response; idem ms-auth, ms-communication, ms-company, ms-knowledge, ms-user-consents). A lib-common só **lê** o `correlationId` do MDC/header na hora de auditar (AuditContextCollector.java:18-24). ms-auth tem ainda `FeignCorrelationIdInterceptor` para propagar em chamadas Feign.

---

## 4. Formato de erro padrão

**lib-common NÃO define formato de erro HTTP** (não há `ErrorResponse`, `@ControllerAdvice` nem DTO de erro na lib). Cada ms tem seu próprio `GlobalExceptionHandler` (ms-ai-guardian, ms-auth, ms-billing, ms-communication, ms-company, ms-knowledge, ms-user, ms-user-consents — `*/infrastructure/rest/GlobalExceptionHandler.java`).

Formato de fato nos ms (exemplo ms-user, ms-user/.../infrastructure/rest/GlobalExceptionHandler.java:37-42, 157-175, 286-289):
```json
{
  "timestamp": "2026-10-03T12:00:00-03:00",
  "status": 400,
  "error": "Bad Request",
  "message": "CNPJ inválido: dígitos verificadores incorretos",
  "path": "/api/users",
  "errorCode": "OPCIONAL",
  "errors": { "campo": "mensagem" },
  "operation": "OPCIONAL", "context": { }
}
```
(`path` é **hardcoded** `"/api/users"` em ms-user GlobalExceptionHandler.java:339-342.)

Os únicos "formatos" que as libs emitem diretamente:
- lib-security: `{"error":"Token inválido ou expirado"}` / `{"error":"Erro na autenticação"}` (401) e `{"error":"Inconsistência de isolamento de tenant"}` (403) (JwtAuthenticationFilter.java:99,154,143,214-226).
- lib-validation: mensagem de constraint violation (vai parar no `errors` do `MethodArgumentNotValidException` de cada ms).
- lib-common: `AuditEvent` publicado no RabbitMQ (contrato com srv-audit), serializado com JavaTimeModule e datas ISO (RabbitAuditEventPublisher.java:32-34):
```json
{
  "eventId": "uuid", "occurredAt": "2026-10-03T15:00:00Z", "schemaVersion": 1,
  "sourceService": "ms-auth", "correlationId": "uuid", "requestId": "uuid|null",
  "tenantId": "uuid", "companyId": "uuid",
  "actor": {"type": "USER|ANONYMOUS", "codeUser": "uuid", "roles": null, "clientIp": "1.2.3.4", "deviceId": "..."},
  "action": "USER_EMAIL_UPDATED", "resource": {"type": "USER", "id": "uuid"},
  "outcome": "SUCCESS|FAILURE", "reason": "≤512 chars",
  "changes": [{"field": "email", "before": null, "after": "a***@x.com"}],
  "metadata": null
}
```
Exchange topic durável `keepguard.audit.exchange` (default `srv-audit-exchange-local`; prod ex.: `srv-audit-exchange-prod` em ms-user application-prod.yml:74-76), routing key `audit.event`, header AMQP `X-Correlation-ID`, delivery PERSISTENT.

Formato de erro dos BFFs Go (bff-core/bff-auth `internal/pkg/errors.go`): `{"error":"CODE","message":"...","correlationId":"...","details":[{"field","code","message"}]}` (keepguard-core/backend/bff/bff-core/internal/pkg/errors.go:9-21). **São três formatos diferentes hoje** (ms Java, lib-security, BFF Go).

---

## 5. Go compartilhado existente e equivalentes já espalhados

### 5.1 Código Go compartilhado: **não existe**
- 16 `go.mod`, cada um módulo isolado `github.com/keepguard/<serviço>`: keepguard-core/backend/{bff/bff-auth, bff/bff-core, mock-servers/mock-sms-gateway, srv/srv-audit, srv/srv-data-collector, srv/srv-email-sender, srv/srv-llm-gateway, srv/srv-sms-sender}; investbot/backend/{bff/bff-invest, ms/ms-analyst-finance, srv/srv-mt5-analytics, srv/srv-mt5-market-data, srv/srv-news-ingestion, srv/srv-quote-simulator}; achadinhos/backend/{bff/bff-achadinhos, ms/ms-achadinhos}.
- `go.work`: nenhum. `replace` em go.mod: nenhum. Nenhum go.mod requer outro `github.com/keepguard/*`.
- Pastas `pkg/`: só `internal/pkg` dentro de bff-auth e bff-core (privadas ao módulo, `internal`): `errors.go, jwt_utils.go, roles.go, user_messages.go` (+ `idempotency.go, saga.go` no bff-auth). São **cópias** quase idênticas entre os dois BFFs.

### 5.2 Equivalentes Go já reimplementados (paths)

| Conceito Java (lib) | Equivalentes Go hoje |
|---|---|
| JWT / auth (lib-security) | keepguard-core/backend/bff/bff-core/internal/adapters/inbound/http/middleware/jwt_middleware.go (HMAC qualquer, `[]byte(secret)` 208-213; claims `codeUser, sub, username, tenant_id/tenantId, userId, email, device_id, jti, sid, roles, authorities` 234-265; cross-check `tenant_id`×`X-Tenant-Id` **sem** bypass admin 267-269; revogação Redis por `session:revoked:<sid>` ou `tokenlogin:<user>:<token>` e blacklist `device:blacklist:<user>:<device>` 86-183; **não valida issuer; não lê `company_id`**); keepguard-core/backend/bff/bff-auth/internal/adapters/in/http/middleware/jwt_middleware.go (versão mais simples, HMAC, sem issuer); keepguard-core/backend/srv/srv-llm-gateway/internal/adapters/inbound/http/auth.go (o mais correto: `WithValidMethods(HS256)` 115, valida issuer default `ms-auth` 36-40/123-128, lê `company_id`/`tenant_id`, `requireLlmAuthority` com bypass ADMIN/SYSTEM normalizando prefixo `ROLE_` 132-141, + X-Api-Key); investbot/backend/bff/bff-invest/internal/adapters/in/http/middleware/jwt.go. Lib: `github.com/golang-jwt/jwt/v5` v5.3.0 (bff-auth, bff-core, bff-invest; v5.2.2 indireto no srv-llm-gateway). |
| Decodificação de claims sem validar | bff-core e bff-auth `internal/pkg/jwt_utils.go` (`ExtractAllClaims`, bff-core jwt_utils.go:77-112). |
| `@PublicEndpoint` | bff-core middleware/public_endpoint.go:29-64 (`c.Set("public_endpoint", true)` + `ConditionalJWTMiddleware`). |
| Roles/autorização (`SecurityContext.hasRole`) | bff-core e bff-auth `internal/pkg/roles.go`, `middleware/roles_middleware.go`; srv-llm-gateway auth.go:79-100. |
| Correlation-ID | bff-core middleware/middleware.go:57-66, 301-322 (`X-Correlation-ID`, gera UUID); bff-auth middleware/middleware.go; srv-audit internal/domain/valueobjects/correlation_id.go; achadinhos bff/ms `internal/adapters/in/http/middleware/middleware.go`; bff-invest server.go/httpx/caller.go. |
| Formato de erro | bff-core e bff-auth `internal/pkg/errors.go` (`ErrorResponse{error,message,correlationId,details}`); srv-llm-gateway usa `map[string]string{"error","message"}` (auth.go:44-47); achadinhos `dto/product_dto.go`, `dto/auth_dto.go`; srv-news-ingestion `handlers/errors.go`. |
| Auditoria (AuditEvent + publisher RabbitMQ) | Um publisher por serviço: bff-auth `adapters/out/messaging/audit/publisher.go`, bff-core `adapters/outbound/messaging/audit/publisher.go`, srv-sms-sender, srv-email-sender, srv-llm-gateway, srv-data-collector `.../audit/publisher.go`; investbot: ms-analyst-finance, bff-invest, srv-news-ingestion. Middlewares de auditoria: bff-core/bff-auth `middleware/audit_middleware.go`, bff-invest `middleware/audit_middleware.go`. |
| CNPJ | keepguard-core/backend/bff/bff-core/internal/domain/valueobjects/cnpj.go:15-116 (mesmo algoritmo de pesos da lib, msg "invalid CNPJ format"); investbot/backend/bff/bff-invest/internal/application/onboarding/validate.go:97-124 (`ValidCNPJ`); srv-data-collector collectors/cvm_dfp_parse.go:356 (`normalizeCNPJ`, só normaliza). |
| CPF | keepguard-core/backend/bff/bff-core/internal/adapters/inbound/http/handlers/billing_handlers.go:676 (`brazilianCPFValid`). |
| Validação de request | bff-core middleware/validation_middleware.go, bff-auth middleware/validation_middleware.go. |
| Paginação | não há helper comum; tipos/funções locais em srv-audit `application/port/out/audit_repository.go` e bff-invest `handlers/trade_handlers.go`. (A lib Java também não tem paginação.) |
| Métricas | bff-core `middleware.MetricsMiddleware` (middleware.go:229) com pacote `metrics` próprio. |

(investbot/achadinhos: apenas paths listados, sem análise de regra — regra de isolamento do keepguard-core/CLAUDE.md.)

---

## 6. O que vira o quê em Go

Proposta de módulo: `keepguard-core/backend/lib/go-common` (ou `pkg/kgcommon`) com `go.mod` próprio `github.com/keepguard/kgcommon`, consumido por `replace ../../lib/go-common` + `go.work` (ou tag Git). Atenção: hoje **nenhum** serviço usa replace/go.work e o Docker build de cada serviço Go precisa enxergar o diretório da lib (contexto de build) — isso muda os scripts de deploy.

Subpacotes sugeridos: `brdoc` (CPF/CNPJ/CEP/UF/CNAE/telefone), `validate` (email/uuid/senha), `auth` (JWT + middleware + context), `httperr` (formato de erro único), `correlation` (middleware + context key), `audit` (AuditEvent + publisher AMQP), `comm` (enums de comunicação), `moderation` (cliente OpenAI + decorators).

| Classe | Classificação | Destino em Go |
|---|---|---|
| **lib-common** | | |
| `LibCommonApplication` | desaparece (Spring) / morto | — |
| `BrazilianValidationUtils` | **porta para Go** (vira validator) | `brdoc.ValidateCNPJ/CPF/CEP/Phone/State/BankCode/CNAE` retornando `error` com as mesmas mensagens; unificar com bff-core cnpj.go, billing_handlers.go:676 e bff-invest validate.go. Manter os testes de lib-common/src/test/.../BrazilianValidationUtilsTest.java como tabela de casos. |
| `ValidationUtils.validateEmail`, `validateAndParseUUID` | **porta para Go** (vira validator) | `validate.Email`, `uuid.Parse` + msg. |
| `ValidationUtils.validatePassword*`, `validateTenantId` | morto (main) | portar só se o fluxo de senha for para Go (ms-auth). |
| `CodeGeneratorUtils.generateSixDigitCode` | **porta para Go** | `crypto/rand` (≈5 linhas). Demais métodos: morto. |
| `DateConverter`, `StringSanitizer`, `LoggingContext` | morto | — |
| `ValidationException`, `InvalidEmail/Password/TenantId/StatusException` | **porta para Go** (vira tipos de erro) | erros sentinela/tipados em `httperr` mapeados para 400/409/422; `InvalidStatusException` → `ErrInvalidStatus{Entity,Current,Expected}`. |
| `CommunicationTypeEnum`, `MessageTypeEnum`, `TemplateTypeEnum` | **porta para Go** (constantes) | `comm` com `const` string — é contrato de fila/API com srv-email/sms-sender; candidato forte a módulo compartilhado. |
| `MetricsConfig` | desaparece (Spring) | — |
| `MetricsService`, `@MetricsEndpoint`, `MetricsAspect` | vira middleware | middleware HTTP Prometheus com `api_requests_total{endpoint,application,status}` + histogram `api_requests_latency_seconds` (manter nomes se houver dashboard Grafana). `recordMessageSend` → helper. |
| `@LogOperation`, `LoggingAspect`, `LoggingService`, `StructuredLogger` | desaparece (Spring/AOP) → vira middleware + chamada explícita | log estruturado via zap/slog com campos de contexto; a parte que **não pode sumir** é a auditoria automática (`audit=true` por padrão em 153 métodos): em Go vira chamada explícita `audit.Publish(ctx, action, entity, id, outcome)` no use case, ou decorator por handler. |
| `@LogQuery`, `@LogAudit` | morto | — |
| `AuditEvent` | **porta para Go** (dto, contrato srv-audit) | struct com tags JSON idênticas; já existe equivalente em cada publisher Go — consolidar. |
| `AuditEventPublisher`, `RabbitAuditEventPublisher`, `RabbitAuditConfiguration`, `NoOpAuditEventPublisher`, `AuditPublisherFallbackConfiguration` | **porta para Go** (o publisher) / desaparece (as configs) | `audit.Publisher` (amqp091-go, exchange topic durável, `audit.event`, persistent, header `X-Correlation-ID`, assíncrono com buffer e drop) + `NoopPublisher`. |
| `AuditContextCollector` | vira middleware | middleware que coloca no `context.Context` correlationId, codeUser, tenant/company, clientIp, deviceId (mesma precedência de headers). |
| `AuditChangeExtractor` | **porta para Go** (pequeno) | regra de máscara de e-mail `a***@dom`; em Go o use case passa o `Change` explicitamente (sem reflexão). |
| **lib-security** | | |
| `@EnableJwtSecurity`, `JwtSecurityAutoConfiguration`, `JwtSecurityProperties` | desaparece (Spring) | config por env (`JWT_SECRET`, `JWT_ISSUER`, `JWT_VALIDATE_ISSUER`) — **sem default de secret**; CORS vira middleware do Echo (e revisar `*`+credentials). |
| `JwtService` | **porta para Go** | `auth.Parse(token) (Claims, error)` com `jwt.WithValidMethods(HS256)`, `[]byte(secret)` cru (compatível com ms-auth), issuer `ms-auth`, `WithExpirationRequired`; claims `sub, roles, authorities, client_id, tenant_id→tenantId→company_id→companyId, agent_id, agent_code, sid, device_id, token_type`. Base: srv-llm-gateway auth.go:114-130. |
| `JwtAuthenticationFilter` | vira middleware | `auth.Middleware(cfg)` Echo: ignora `/actuator`, `/swagger`, `/health`; 401 sem token/inválido (decidir: manter comportamento "sem token → deixa passar para rota pública"); cross-check `X-Tenant-Id` com bypass ADMIN/SYSTEM; põe claims no context. Revogação Redis do bff-core pode virar opção (`WithRevocation(redis)`). |
| `SecurityContext` (ThreadLocal) | vira middleware (context.Context) | `auth.FromContext(ctx)` + `HasRole/HasAuthority` com o bypass ADMIN/SYSTEM documentado; `RequireRole/RequireAuthority` como middleware. |
| `@PublicEndpoint` | desaparece (Spring) → roteamento | em Go a rota pública é registrada fora do grupo com middleware JWT (padrão do Echo), sem varredura por anotação. |
| **lib-validation** | | |
| `@ModeratedContent`, `ModeratedContentValidator` | vira validator | função explícita `moderation.Check(ctx, text, threshold, cats...)` chamada no handler/DTO (ou tag custom do go-playground/validator). Manter fail-open e a mensagem PT; **não logar o conteúdo**. |
| `ContentModerationService`, `ContentModerationPort`, `ModerationResult`, `ModerationCategory`, `ModerationException` | **porta para Go** | interface `Moderator` + struct `Result` + `ExceedsThreshold`. |
| `ContentViolationException` | morto (nunca lançada) | só portar se for usado `validateOrThrow`. |
| `OpenAIModerationAdapter` | **porta para Go** | `net/http` POST `/v1/moderations`. Avaliar passar pelo srv-llm-gateway em vez de chamar a OpenAI direto. |
| `ResilientContentModerationPort` | **porta para Go** | retry simples + `sony/gobreaker` (ou sem CB, já que é fail-open). |
| `CachedContentModerationPort` | **porta para Go** (corrigindo) | LRU com TTL de verdade (hoje o TTL é ignorado e o cache não tem limite). |
| `ModerationProperties`, `ModerationAutoConfiguration` | desaparece (Spring) | struct de config por env. |

### Prioridade sugerida para um módulo Go compartilhado
1. `auth` (JWT + middleware + context) — já há 4 implementações divergentes (issuer validado só no srv-llm-gateway; `company_id` ignorado no bff-core; bypass admin só em alguns).
2. `httperr` + `correlation` — unificar os três formatos de erro e os N filtros de correlation-id.
3. `audit` (AuditEvent + publisher) — 9+ publishers Go copiados + contrato com srv-audit.
4. `brdoc` — 3 implementações de CNPJ/CPF hoje.
5. `comm` enums — só quando ms-communication/ms-auth migrarem.
6. `moderation` — só ms-user usa; pode ficar dentro do serviço migrado em vez de virar lib.

### Riscos encontrados (para o plano)
- Secret JWT com fallback hardcoded na lib e nos ymls (JwtSecurityProperties.java:47; ms-user application.yml:136; ms-billing application.yml:93; ms-knowledge application.yml:194).
- Build dos ms depende do `~/.m2` local; versão do pom da lib-common (1.0.38) ≠ consumida (1.0.37).
- Cache de moderação sem TTL/limite; conteúdo de usuário logado em INFO (LGPD).
- `SecurityContext.hasRole` sempre true para admin/system — portar igual ou documentar mudança.
- Diferença de regra de tenant entre lib-security (bypass admin, só checa se header presente) e bff-core Go (sem bypass, tenant obrigatório).
