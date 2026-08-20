package com.keepguard.ms_company.adapters.in.rest.company;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.ms_company.adapters.in.rest.company.dto.request.CompanyCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.response.CompanyResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.request.CompanyUpdateDTO;
import com.keepguard.ms_company.application.dto.company.CompanyCreateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.company.CompanySearchCriteriaDTO;
import com.keepguard.ms_company.adapters.in.rest.company.mapper.CompanyAdapterMapper;
import com.keepguard.ms_company.application.port.in.CompanyPort;
import com.keepguard.ms_company.domain.enums.CompanyStatusEnum;
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
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Company", description = "API para gerenciamento de empresas")
public class CompanyController {

    private final CompanyPort companyPort;
    private final CompanyAdapterMapper companyAdapterMapper;

    @PostMapping
    @MetricsEndpoint(endpoint = "company_create", operation = "Criar empresa")
    @Operation(summary = "Criar nova empresa", description = "Cria uma nova empresa no sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Empresa criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "CNPJ já cadastrado")
    })
    public ResponseEntity<CompanyResponseDTO> create(@Valid @RequestBody CompanyCreateDTO dto) {
        log.info("Criando empresa: {}", dto.getName());
        CompanyCreateCommandDTO command = companyAdapterMapper.toCreateCommand(dto);
        CompanyViewDTO view = companyPort.create(command);
        CompanyResponseDTO response = companyAdapterMapper.toResponseDTO(view);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @MetricsEndpoint(endpoint = "company_update", operation = "Atualizar empresa")
    @Operation(summary = "Atualizar empresa", description = "Atualiza os dados de uma empresa existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    public ResponseEntity<CompanyResponseDTO> update(
            @Parameter(description = "ID da empresa") @PathVariable UUID id,
            @Valid @RequestBody CompanyUpdateDTO dto) {
        log.info("Atualizando empresa ID: {}", id);
        CompanyUpdateCommandDTO command = companyAdapterMapper.toUpdateCommand(dto);
        CompanyViewDTO view = companyPort.update(id, command);
        CompanyResponseDTO response = companyAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/approve")
    @MetricsEndpoint(endpoint = "company_approve", operation = "Aprovar empresa")
    @Operation(summary = "Aprovar empresa", description = "Aprova uma empresa pendente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa aprovada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    public ResponseEntity<CompanyResponseDTO> approve(
            @Parameter(description = "ID da empresa") @PathVariable UUID id) {
        log.info("Aprovando empresa ID: {}", id);
        CompanyViewDTO view = companyPort.approve(id);
        CompanyResponseDTO response = companyAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reject")
    @MetricsEndpoint(endpoint = "company_reject", operation = "Rejeitar empresa")
    @Operation(summary = "Rejeitar empresa", description = "Rejeita uma empresa pendente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa rejeitada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    public ResponseEntity<CompanyResponseDTO> reject(
            @Parameter(description = "ID da empresa") @PathVariable UUID id) {
        log.info("Rejeitando empresa ID: {}", id);
        CompanyViewDTO view = companyPort.reject(id);
        CompanyResponseDTO response = companyAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/activate")
    @MetricsEndpoint(endpoint = "company_activate", operation = "Ativar empresa")
    @Operation(summary = "Ativar empresa", description = "Ativa uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa ativada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    public ResponseEntity<CompanyResponseDTO> activate(
            @Parameter(description = "ID da empresa") @PathVariable UUID id) {
        log.info("Ativando empresa ID: {}", id);
        CompanyViewDTO view = companyPort.activate(id);
        CompanyResponseDTO response = companyAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/deactivate")
    @MetricsEndpoint(endpoint = "company_deactivate", operation = "Desativar empresa")
    @Operation(summary = "Desativar empresa", description = "Desativa uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa desativada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    public ResponseEntity<CompanyResponseDTO> deactivate(
            @Parameter(description = "ID da empresa") @PathVariable UUID id) {
        log.info("Desativando empresa ID: {}", id);
        CompanyViewDTO view = companyPort.deactivate(id);
        CompanyResponseDTO response = companyAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/suspend")
    @MetricsEndpoint(endpoint = "company_suspend", operation = "Suspender empresa")
    @Operation(summary = "Suspender empresa", description = "Suspende uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa suspensa com sucesso"),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    public ResponseEntity<CompanyResponseDTO> suspend(
            @Parameter(description = "ID da empresa") @PathVariable UUID id) {
        log.info("Suspendo empresa ID: {}", id);
        CompanyViewDTO view = companyPort.suspend(id);
        CompanyResponseDTO response = companyAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/block")
    @MetricsEndpoint(endpoint = "company_block", operation = "Bloquear empresa")
    @Operation(summary = "Bloquear empresa", description = "Bloqueia uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa bloqueada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    public ResponseEntity<CompanyResponseDTO> block(
            @Parameter(description = "ID da empresa") @PathVariable UUID id) {
        log.info("Bloqueando empresa ID: {}", id);
        CompanyViewDTO view = companyPort.block(id);
        CompanyResponseDTO response = companyAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @MetricsEndpoint(endpoint = "company_get_by_id", operation = "Buscar empresa por ID")
    @Operation(summary = "Buscar empresa por ID", description = "Retorna os dados de uma empresa pelo ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    public ResponseEntity<CompanyResponseDTO> getById(
            @Parameter(description = "ID da empresa") @PathVariable UUID id) {
        log.info("Buscando empresa por ID: {}", id);
        CompanyViewDTO view = companyPort.getById(id);
        CompanyResponseDTO response = companyAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/x-tenant-id/{tenantId}")
    @MetricsEndpoint(endpoint = "company_get_by_tenant_id", operation = "Buscar empresa por TenantId")
    @Operation(summary = "Buscar empresa por TenantId", description = "Retorna os dados de uma empresa pelo TenantId")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    public ResponseEntity<CompanyResponseDTO> getByTenantId(
            @Parameter(description = "TenantId da empresa") @PathVariable UUID tenantId) {
        log.info("Buscando empresa por TenantId: {}", tenantId);
        CompanyViewDTO view = companyPort.getByTenantId(tenantId);
        CompanyResponseDTO response = companyAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{codeCompany}")
    @MetricsEndpoint(endpoint = "company_get_by_code", operation = "Buscar empresa por CodeCompany")
    @Operation(summary = "Buscar empresa por CodeCompany", description = "Retorna os dados de uma empresa pelo CodeCompany")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    public ResponseEntity<CompanyResponseDTO> getByCodeCompany(
            @Parameter(description = "CodeCompany da empresa") @PathVariable UUID codeCompany) {
        log.info("Buscando empresa por CodeCompany: {}", codeCompany);
        CompanyViewDTO view = companyPort.getByCodeCompany(codeCompany);
        CompanyResponseDTO response = companyAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cnpj/{cnpj}")
    @MetricsEndpoint(endpoint = "company_get_by_cnpj", operation = "Buscar empresa por CNPJ")
    @Operation(summary = "Buscar empresa por CNPJ", description = "Retorna os dados de uma empresa pelo CNPJ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    public ResponseEntity<CompanyResponseDTO> getByCnpj(
            @Parameter(description = "CNPJ da empresa") @PathVariable String cnpj) {
        log.info("Buscando empresa por CNPJ: {}", cnpj);
        CompanyViewDTO view = companyPort.getByCnpj(cnpj);
        CompanyResponseDTO response = companyAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @MetricsEndpoint(endpoint = "company_search", operation = "Buscar empresas")
    @Operation(summary = "Buscar empresas", description = "Busca empresas com filtros")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de empresas retornada com sucesso")
    })
    public ResponseEntity<PageResultDTO<CompanyResponseDTO>> search(
            @Parameter(description = "Filtro por nome") @RequestParam(required = false) String name,
            @Parameter(description = "Filtro por razão social") @RequestParam(required = false) String legalName,
            @Parameter(description = "Filtro por CNPJ") @RequestParam(required = false) String cnpj,
            @Parameter(description = "Filtro por cidade") @RequestParam(required = false) String city,
            @Parameter(description = "Filtro por estado") @RequestParam(required = false) String state,
            @Parameter(description = "Filtro por status") @RequestParam(required = false) String status,
            @Parameter(description = "Página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campos para ordenação") @RequestParam(required = false) List<String> sortFields,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "ASC") String sortDirection) {
        log.info("Buscando empresas com filtros - nome: {}, CNPJ: {}, cidade: {}", name, cnpj, city);
        CompanySearchCriteriaDTO criteria = new CompanySearchCriteriaDTO(
            name, legalName, cnpj, city, state,
            status != null ? CompanyStatusEnum.valueOf(status.toUpperCase()) : null,
            page, size, sortFields, sortDirection
        );
        PageResultDTO<CompanyViewDTO> views = companyPort.search(criteria);
        PageResultDTO<CompanyResponseDTO> response = new PageResultDTO<>(
            views.items().stream()
                .map(companyAdapterMapper::toResponseDTO)
                .toList(),
            views.total(),
            views.page(),
            views.size()
        );
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    @MetricsEndpoint(endpoint = "company_delete", operation = "Remover empresa")
    @Operation(summary = "Remover empresa", description = "Remove uma empresa do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Empresa removida com sucesso"),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da empresa") @PathVariable UUID id) {
        log.info("Removendo empresa ID: {}", id);
        companyPort.delete(id);
        return ResponseEntity.noContent().build();
    }
}