package com.keepguard.ms_company.adapters.in.rest.companypolicy;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.ms_company.adapters.in.rest.companypolicy.dto.request.CreateCompanyPolicyRequestDTO;
import com.keepguard.ms_company.adapters.in.rest.companypolicy.dto.request.UpdateCompanyPolicyRequestDTO;
import com.keepguard.ms_company.adapters.in.rest.companypolicy.dto.response.CompanyPolicyResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.companypolicy.mapper.CompanyPolicyAdapterMapper;
import com.keepguard.ms_company.application.dto.companypolicy.CompanyPolicyViewDTO;
import com.keepguard.ms_company.application.port.in.CompanyPolicyPort;
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
@RequestMapping("/api/v1/companies/{companyId}/policies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Company Policy", description = "API para gerenciamento de políticas da empresa")
public class CompanyPolicyController {

    private final CompanyPolicyPort companyPolicyPort;
    private final CompanyPolicyAdapterMapper companyPolicyAdapterMapper;

    @PostMapping
    @MetricsEndpoint(endpoint = "company_policy_create", operation = "Criar política da empresa")
    @Operation(summary = "Criar nova política da empresa", description = "Cria uma nova política para a empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Política criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Política com código já existe"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<CompanyPolicyResponseDTO> create(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId,
            @Valid @RequestBody CreateCompanyPolicyRequestDTO request) {

        log.info("Criando política para empresa: {}", companyId);

        CompanyPolicyViewDTO result = companyPolicyPort.create(
            companyPolicyAdapterMapper.toCreateCommand(companyId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(companyPolicyAdapterMapper.toResponseDTO(result));
    }

    @PutMapping("/{policyId}")
    @MetricsEndpoint(endpoint = "company_policy_update", operation = "Atualizar política da empresa")
    @Operation(summary = "Atualizar política da empresa", description = "Atualiza uma política existente da empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Política atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Política não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<CompanyPolicyResponseDTO> update(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId,
            @Parameter(description = "ID da política") @PathVariable UUID policyId,
            @Valid @RequestBody UpdateCompanyPolicyRequestDTO request) {

        log.info("Atualizando política {} da empresa: {}", policyId, companyId);

        CompanyPolicyViewDTO result = companyPolicyPort.update(
            companyPolicyAdapterMapper.toUpdateCommand(policyId, request));
        return ResponseEntity.ok(companyPolicyAdapterMapper.toResponseDTO(result));
    }

    @DeleteMapping("/{policyId}")
    @MetricsEndpoint(endpoint = "company_policy_deactivate", operation = "Desativar política da empresa")
    @Operation(summary = "Desativar política da empresa", description = "Desativa uma política da empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Política desativada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Política não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<CompanyPolicyResponseDTO> deactivate(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId,
            @Parameter(description = "ID da política") @PathVariable UUID policyId,
            @RequestParam String updatedBy) {

        log.info("Desativando política {} da empresa: {}", policyId, companyId);

        CompanyPolicyViewDTO result = companyPolicyPort.deactivate(
            companyPolicyAdapterMapper.toDeactivateCommand(policyId, updatedBy));
        return ResponseEntity.ok(companyPolicyAdapterMapper.toResponseDTO(result));
    }

    @GetMapping
    @MetricsEndpoint(endpoint = "company_policy_list", operation = "Listar políticas da empresa")
    @Operation(summary = "Listar políticas da empresa", description = "Lista todas as políticas de uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de políticas retornada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<CompanyPolicyResponseDTO>> list(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId) {

        log.info("Listando políticas da empresa: {}", companyId);

        List<CompanyPolicyViewDTO> result = companyPolicyPort.getPolicies(
            companyPolicyAdapterMapper.toGetPoliciesQuery(companyId));
        return ResponseEntity.ok(result.stream().map(companyPolicyAdapterMapper::toResponseDTO).toList());
    }

    @GetMapping("/active")
    @MetricsEndpoint(endpoint = "company_policy_list_active", operation = "Listar políticas ativas da empresa")
    @Operation(summary = "Listar políticas ativas da empresa", description = "Lista apenas as políticas ativas de uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de políticas ativas retornada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<CompanyPolicyResponseDTO>> listActive(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId) {

        log.info("Listando políticas ativas da empresa: {}", companyId);

        List<CompanyPolicyViewDTO> result = companyPolicyPort.getActivePolicies(
            companyPolicyAdapterMapper.toGetActivePoliciesQuery(companyId));
        return ResponseEntity.ok(result.stream().map(companyPolicyAdapterMapper::toResponseDTO).toList());
    }
}
