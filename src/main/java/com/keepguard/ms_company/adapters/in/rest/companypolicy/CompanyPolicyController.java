package com.keepguard.ms_company.adapters.in.rest.companypolicy;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.ms_company.adapters.in.rest.companypolicy.dto.CompanyPolicyResponse;
import com.keepguard.ms_company.adapters.in.rest.companypolicy.dto.CreateCompanyPolicyRequest;
import com.keepguard.ms_company.adapters.in.rest.companypolicy.dto.UpdateCompanyPolicyRequest;
import com.keepguard.ms_company.application.dto.companypolicy.CompanyPolicyViewDTO;
import com.keepguard.ms_company.application.dto.companypolicy.CreateCompanyPolicyCommandDTO;
import com.keepguard.ms_company.application.dto.companypolicy.DeactivateCompanyPolicyCommandDTO;
import com.keepguard.ms_company.application.dto.companypolicy.GetActiveCompanyPoliciesQueryDTO;
import com.keepguard.ms_company.application.dto.companypolicy.GetCompanyPoliciesQueryDTO;
import com.keepguard.ms_company.application.dto.companypolicy.UpdateCompanyPolicyCommandDTO;
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

    @PostMapping
    @MetricsEndpoint(endpoint = "company_policy_create", operation = "Criar política da empresa")
    @Operation(summary = "Criar nova política da empresa", description = "Cria uma nova política para a empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Política criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Política com código já existe"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<CompanyPolicyResponse> create(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId,
            @Valid @RequestBody CreateCompanyPolicyRequest request) {

        log.info("Criando política para empresa: {}", companyId);

        var command = new CreateCompanyPolicyCommandDTO(
            companyId,
            request.getCode(),
            request.getDescription(),
            request.getStatus(),
            request.getEffectiveFrom(),
            request.getEffectiveTo(),
            request.getCreatedBy()
        );

        CompanyPolicyViewDTO result = companyPolicyPort.create(command);
        CompanyPolicyResponse response = CompanyPolicyResponse.from(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    public ResponseEntity<CompanyPolicyResponse> update(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId,
            @Parameter(description = "ID da política") @PathVariable UUID policyId,
            @Valid @RequestBody UpdateCompanyPolicyRequest request) {

        log.info("Atualizando política {} da empresa: {}", policyId, companyId);

        var command = new UpdateCompanyPolicyCommandDTO(
            policyId,
            request.getDescription(),
            request.getStatus(),
            request.getEffectiveTo(),
            request.getUpdatedBy()
        );

        CompanyPolicyViewDTO result = companyPolicyPort.update(command);
        CompanyPolicyResponse response = CompanyPolicyResponse.from(result);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{policyId}")
    @MetricsEndpoint(endpoint = "company_policy_deactivate", operation = "Desativar política da empresa")
    @Operation(summary = "Desativar política da empresa", description = "Desativa uma política da empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Política desativada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Política não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<CompanyPolicyResponse> deactivate(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId,
            @Parameter(description = "ID da política") @PathVariable UUID policyId,
            @RequestParam String updatedBy) {

        log.info("Desativando política {} da empresa: {}", policyId, companyId);

        var command = new DeactivateCompanyPolicyCommandDTO(policyId, updatedBy);

        CompanyPolicyViewDTO result = companyPolicyPort.deactivate(command);
        CompanyPolicyResponse response = CompanyPolicyResponse.from(result);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @MetricsEndpoint(endpoint = "company_policy_list", operation = "Listar políticas da empresa")
    @Operation(summary = "Listar políticas da empresa", description = "Lista todas as políticas de uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de políticas retornada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<CompanyPolicyResponse>> list(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId) {

        log.info("Listando políticas da empresa: {}", companyId);

        var query = new GetCompanyPoliciesQueryDTO(companyId);
        List<CompanyPolicyViewDTO> result = companyPolicyPort.getPolicies(query);
        List<CompanyPolicyResponse> response = result.stream()
                .map(CompanyPolicyResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @MetricsEndpoint(endpoint = "company_policy_list_active", operation = "Listar políticas ativas da empresa")
    @Operation(summary = "Listar políticas ativas da empresa", description = "Lista apenas as políticas ativas de uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de políticas ativas retornada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<List<CompanyPolicyResponse>> listActive(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId) {

        log.info("Listando políticas ativas da empresa: {}", companyId);

        var query = new GetActiveCompanyPoliciesQueryDTO(companyId);
        List<CompanyPolicyViewDTO> result = companyPolicyPort.getActivePolicies(query);
        List<CompanyPolicyResponse> response = result.stream()
                .map(CompanyPolicyResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }
}
