package com.keepguard.ms_company.adapters.in.rest.bankaccount;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.ms_company.adapters.in.rest.bankaccount.dto.BankAccountCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.bankaccount.dto.BankAccountResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.bankaccount.dto.BankAccountUpdateDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountCreateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountSearchCriteriaDTO;
import com.keepguard.ms_company.adapters.in.rest.bankaccount.mapper.BankAccountAdapterMapper;
import com.keepguard.ms_company.application.port.in.BankAccountPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bank-accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bank Account", description = "API para gerenciamento de dados bancários")
public class BankAccountController {

    private final BankAccountPort bankAccountPort;
    private final BankAccountAdapterMapper bankAccountAdapterMapper;

    @PostMapping("/company/{companyId}")
    @MetricsEndpoint(endpoint = "bank_account_create", operation = "Criar dados bancários")
    @Operation(summary = "Criar novos dados bancários", description = "Cria novos dados bancários para uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Dados bancários criados com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    public ResponseEntity<BankAccountResponseDTO> create(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId,
            @Valid @RequestBody BankAccountCreateDTO dto) {
        log.info("Criando dados bancários para empresa: {}", companyId);
        BankAccountCreateCommandDTO command = bankAccountAdapterMapper.toCreateCommand(dto);
        BankAccountViewDTO view = bankAccountPort.create(companyId, command);
        BankAccountResponseDTO response = bankAccountAdapterMapper.toResponseDTO(view);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @MetricsEndpoint(endpoint = "bank_account_update", operation = "Atualizar dados bancários")
    @Operation(summary = "Atualizar dados bancários", description = "Atualiza os dados bancários existentes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dados bancários atualizados com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Dados bancários não encontrados")
    })
    public ResponseEntity<BankAccountResponseDTO> update(
            @Parameter(description = "ID dos dados bancários") @PathVariable UUID id,
            @Valid @RequestBody BankAccountUpdateDTO dto) {
        log.info("Atualizando dados bancários ID: {}", id);

        BankAccountUpdateCommandDTO command = bankAccountAdapterMapper.toUpdateCommand(dto);
        BankAccountViewDTO view = bankAccountPort.update(id, command);
        BankAccountResponseDTO response = bankAccountAdapterMapper.toResponseDTO(view);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/activate")
    @MetricsEndpoint(endpoint = "bank_account_activate", operation = "Ativar dados bancários")
    @Operation(summary = "Ativar dados bancários", description = "Ativa dados bancários")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dados bancários ativados com sucesso"),
        @ApiResponse(responseCode = "404", description = "Dados bancários não encontrados")
    })
    public ResponseEntity<BankAccountResponseDTO> activate(
            @Parameter(description = "ID dos dados bancários") @PathVariable UUID id) {
        log.info("Ativando dados bancários ID: {}", id);

        BankAccountViewDTO view = bankAccountPort.activate(id);
        BankAccountResponseDTO response = bankAccountAdapterMapper.toResponseDTO(view);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/deactivate")
    @MetricsEndpoint(endpoint = "bank_account_deactivate", operation = "Desativar dados bancários")
    @Operation(summary = "Desativar dados bancários", description = "Desativa dados bancários")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dados bancários desativados com sucesso"),
        @ApiResponse(responseCode = "404", description = "Dados bancários não encontrados")
    })
    public ResponseEntity<BankAccountResponseDTO> deactivate(
            @Parameter(description = "ID dos dados bancários") @PathVariable UUID id) {
        log.info("Desativando dados bancários ID: {}", id);

        BankAccountViewDTO view = bankAccountPort.deactivate(id);
        BankAccountResponseDTO response = bankAccountAdapterMapper.toResponseDTO(view);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @MetricsEndpoint(endpoint = "bank_account_get_by_id", operation = "Buscar dados bancários por ID")
    @Operation(summary = "Buscar dados bancários por ID", description = "Retorna os dados bancários pelo ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dados bancários encontrados"),
        @ApiResponse(responseCode = "404", description = "Dados bancários não encontrados")
    })
    public ResponseEntity<BankAccountResponseDTO> getById(
            @Parameter(description = "ID dos dados bancários") @PathVariable UUID id) {
        log.info("Buscando dados bancários por ID: {}", id);

        BankAccountViewDTO view = bankAccountPort.getById(id);
        BankAccountResponseDTO response = bankAccountAdapterMapper.toResponseDTO(view);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}")
    @MetricsEndpoint(endpoint = "bank_account_list_by_company", operation = "Listar dados bancários por empresa")
    @Operation(summary = "Listar dados bancários por empresa", description = "Lista todos os dados bancários de uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de dados bancários retornada com sucesso")
    })
    public ResponseEntity<List<BankAccountResponseDTO>> listByCompanyId(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId) {
        log.info("Listando dados bancários da empresa: {}", companyId);

        List<BankAccountViewDTO> views = bankAccountPort.listByCompanyId(companyId);
        List<BankAccountResponseDTO> response = views.stream()
            .map(bankAccountAdapterMapper::toResponseDTO)
            .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}/active")
    @MetricsEndpoint(endpoint = "bank_account_get_active_by_company", operation = "Buscar dados bancários ativos por empresa")
    @Operation(summary = "Buscar dados bancários ativos por empresa", description = "Retorna os dados bancários ativos de uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dados bancários ativos encontrados"),
        @ApiResponse(responseCode = "404", description = "Dados bancários ativos não encontrados")
    })
    public ResponseEntity<BankAccountResponseDTO> getActiveByCompanyId(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId) {
        log.info("Buscando dados bancários ativos da empresa: {}", companyId);

        BankAccountViewDTO view = bankAccountPort.getActiveByCompanyId(companyId);
        BankAccountResponseDTO response = bankAccountAdapterMapper.toResponseDTO(view);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @MetricsEndpoint(endpoint = "bank_account_list", operation = "Listar dados bancários")
    @Operation(summary = "Listar dados bancários", description = "Lista todos os dados bancários com paginação")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de dados bancários retornada com sucesso")
    })
    public ResponseEntity<PageResultDTO<BankAccountResponseDTO>> list(
            @Parameter(description = "Página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size) {
        log.info("Listando dados bancários");

        BankAccountSearchCriteriaDTO criteria = new BankAccountSearchCriteriaDTO(
            null, null, null, null, page, size, null, "ASC"
        );

        PageResultDTO<BankAccountViewDTO> views = bankAccountPort.search(criteria);
        PageResultDTO<BankAccountResponseDTO> response = new PageResultDTO<>(
            views.items().stream()
                .map(bankAccountAdapterMapper::toResponseDTO)
                .toList(),
            views.total(),
            views.page(),
            views.size()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @MetricsEndpoint(endpoint = "bank_account_search", operation = "Buscar dados bancários")
    @Operation(summary = "Buscar dados bancários", description = "Busca dados bancários com filtros")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de dados bancários retornada com sucesso")
    })
    public ResponseEntity<PageResultDTO<BankAccountResponseDTO>> search(
            @Parameter(description = "Filtro por empresa") @RequestParam(required = false) UUID companyId,
            @Parameter(description = "Filtro por código do banco") @RequestParam(required = false) String bankCode,
            @Parameter(description = "Filtro por tipo de conta") @RequestParam(required = false) String accountType,
            @Parameter(description = "Filtro por status ativo") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campos para ordenação") @RequestParam(required = false) List<String> sortFields,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "ASC") String sortDirection) {
        log.info("Buscando dados bancários com filtros");

        BankAccountSearchCriteriaDTO criteria = new BankAccountSearchCriteriaDTO(
            companyId, bankCode, accountType, active, page, size, sortFields, sortDirection
        );

        PageResultDTO<BankAccountViewDTO> views = bankAccountPort.search(criteria);
        PageResultDTO<BankAccountResponseDTO> response = new PageResultDTO<>(
            views.items().stream()
                .map(bankAccountAdapterMapper::toResponseDTO)
                .toList(),
            views.total(),
            views.page(),
            views.size()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @MetricsEndpoint(endpoint = "bank_account_list_all", operation = "Listar todos os dados bancários")
    @Operation(summary = "Listar todos os dados bancários", description = "Retorna todos os dados bancários sem paginação")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de todos os dados bancários")
    })
    public ResponseEntity<List<BankAccountResponseDTO>> listAll() {
        log.info("Listando todos os dados bancários");

        List<BankAccountViewDTO> views = bankAccountPort.listAll();
        List<BankAccountResponseDTO> response = views.stream()
            .map(bankAccountAdapterMapper::toResponseDTO)
            .toList();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @MetricsEndpoint(endpoint = "bank_account_delete", operation = "Remover dados bancários")
    @Operation(summary = "Remover dados bancários", description = "Remove dados bancários do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Dados bancários removidos com sucesso"),
        @ApiResponse(responseCode = "404", description = "Dados bancários não encontrados")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID dos dados bancários") @PathVariable UUID id) {
        log.info("Removendo dados bancários ID: {}", id);

        bankAccountPort.delete(id);

        return ResponseEntity.noContent().build();
    }
}
