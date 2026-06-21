package com.keepguard.ms_company.adapters.in.rest.representative;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.ms_company.adapters.in.rest.representative.dto.RepresentativeCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.representative.dto.RepresentativeResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.representative.dto.RepresentativeUpdateDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeViewDTO;
import com.keepguard.ms_company.adapters.in.rest.representative.mapper.RepresentativeAdapterMapper;
import com.keepguard.ms_company.application.port.in.RepresentativePort;
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
@RequestMapping("/api/v1/representatives")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Representative", description = "API para gerenciamento de representantes legais")
public class RepresentativeController {

    private final RepresentativePort representativePort;
    private final RepresentativeAdapterMapper representativeAdapterMapper;

    @PostMapping("/company/{companyId}")
    @MetricsEndpoint(endpoint = "representative_create", operation = "Criar representante")
    @Operation(summary = "Criar novo representante", description = "Cria um novo representante legal para uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Representante criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada"),
        @ApiResponse(responseCode = "409", description = "Representante com este CPF já existe para esta empresa")
    })
    public ResponseEntity<RepresentativeResponseDTO> create(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId,
            @Valid @RequestBody RepresentativeCreateDTO dto) {
        log.info("Criando representante para empresa: {}", companyId);
        RepresentativeCreateCommandDTO command = representativeAdapterMapper.toCreateCommand(dto, companyId);
        RepresentativeViewDTO view = representativePort.create(companyId, command);
        RepresentativeResponseDTO response = representativeAdapterMapper.toResponseDTO(view);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @MetricsEndpoint(endpoint = "representative_update", operation = "Atualizar representante")
    @Operation(summary = "Atualizar representante", description = "Atualiza os dados de um representante existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Representante atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Representante não encontrado")
    })
    public ResponseEntity<RepresentativeResponseDTO> update(
            @Parameter(description = "ID do representante") @PathVariable UUID id,
            @Valid @RequestBody RepresentativeUpdateDTO dto) {
        log.info("Atualizando representante: {}", id);
        RepresentativeUpdateCommandDTO command = representativeAdapterMapper.toUpdateCommand(dto);
        RepresentativeViewDTO view = representativePort.update(id, command);
        RepresentativeResponseDTO response = representativeAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/activate")
    @MetricsEndpoint(endpoint = "representative_activate", operation = "Ativar representante")
    @Operation(summary = "Ativar representante", description = "Ativa um representante")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Representante ativado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Representante não encontrado")
    })
    public ResponseEntity<RepresentativeResponseDTO> activate(
            @Parameter(description = "ID do representante") @PathVariable UUID id) {
        log.info("Ativando representante: {}", id);
        RepresentativeViewDTO view = representativePort.activate(id);
        RepresentativeResponseDTO response = representativeAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/deactivate")
    @MetricsEndpoint(endpoint = "representative_deactivate", operation = "Desativar representante")
    @Operation(summary = "Desativar representante", description = "Desativa um representante")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Representante desativado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Representante não encontrado")
    })
    public ResponseEntity<RepresentativeResponseDTO> deactivate(
            @Parameter(description = "ID do representante") @PathVariable UUID id) {
        log.info("Desativando representante: {}", id);
        RepresentativeViewDTO view = representativePort.deactivate(id);
        RepresentativeResponseDTO response = representativeAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @MetricsEndpoint(endpoint = "representative_delete", operation = "Remover representante")
    @Operation(summary = "Remover representante", description = "Remove um representante")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Representante removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Representante não encontrado")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do representante") @PathVariable UUID id) {
        log.info("Removendo representante: {}", id);
        representativePort.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @MetricsEndpoint(endpoint = "representative_get", operation = "Buscar representante por ID")
    @Operation(summary = "Buscar representante por ID", description = "Busca um representante pelo seu ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Representante encontrado"),
        @ApiResponse(responseCode = "404", description = "Representante não encontrado")
    })
    public ResponseEntity<RepresentativeResponseDTO> findById(
            @Parameter(description = "ID do representante") @PathVariable UUID id) {
        log.info("Buscando representante: {}", id);
        RepresentativeViewDTO view = representativePort.findById(id);
        RepresentativeResponseDTO response = representativeAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @MetricsEndpoint(endpoint = "representative_list", operation = "Listar representantes")
    @Operation(summary = "Listar representantes", description = "Lista todos os representantes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de representantes")
    })
    public ResponseEntity<List<RepresentativeResponseDTO>> findAll() {
        log.info("Listando representantes");
        List<RepresentativeViewDTO> views = representativePort.findAll();
        List<RepresentativeResponseDTO> responses = views.stream()
            .map(representativeAdapterMapper::toResponseDTO)
            .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/company/{companyId}")
    @MetricsEndpoint(endpoint = "representative_list_by_company", operation = "Listar representantes por empresa")
    @Operation(summary = "Listar representantes por empresa", description = "Lista todos os representantes de uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de representantes da empresa")
    })
    public ResponseEntity<List<RepresentativeResponseDTO>> findByCompanyId(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId) {
        log.info("Listando representantes da empresa: {}", companyId);
        List<RepresentativeViewDTO> views = representativePort.findByCompanyId(companyId);
        List<RepresentativeResponseDTO> responses = views.stream()
            .map(representativeAdapterMapper::toResponseDTO)
            .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/company/{companyId}/active")
    @MetricsEndpoint(endpoint = "representative_get_active_by_company", operation = "Buscar representante ativo por empresa")
    @Operation(summary = "Buscar representante ativo por empresa", description = "Busca o representante ativo de uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Representante ativo encontrado"),
        @ApiResponse(responseCode = "404", description = "Representante ativo não encontrado para esta empresa")
    })
    public ResponseEntity<RepresentativeResponseDTO> findActiveByCompanyId(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId) {
        log.info("Buscando representante ativo da empresa: {}", companyId);
        RepresentativeViewDTO view = representativePort.findActiveByCompanyId(companyId);
        RepresentativeResponseDTO response = representativeAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @MetricsEndpoint(endpoint = "representative_list_active", operation = "Listar representantes ativos")
    @Operation(summary = "Listar representantes ativos", description = "Lista todos os representantes ativos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de representantes ativos")
    })
    public ResponseEntity<List<RepresentativeResponseDTO>> findAllActive() {
        log.info("Listando representantes ativos");
        List<RepresentativeViewDTO> views = representativePort.findAllActive();
        List<RepresentativeResponseDTO> responses = views.stream()
            .map(representativeAdapterMapper::toResponseDTO)
            .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search/cpf/{cpf}")
    @MetricsEndpoint(endpoint = "representative_search_by_cpf", operation = "Buscar representante por CPF")
    @Operation(summary = "Buscar representante por CPF", description = "Busca um representante pelo CPF")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Representante encontrado"),
        @ApiResponse(responseCode = "404", description = "Representante não encontrado com este CPF")
    })
    public ResponseEntity<RepresentativeResponseDTO> findByCpf(
            @Parameter(description = "CPF do representante") @PathVariable String cpf) {
        log.info("Buscando representante por CPF: {}", cpf);
        RepresentativeViewDTO view = representativePort.findByCpf(cpf);
        RepresentativeResponseDTO response = representativeAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/email/{email}")
    @MetricsEndpoint(endpoint = "representative_search_by_email", operation = "Buscar representante por email")
    @Operation(summary = "Buscar representante por email", description = "Busca um representante pelo email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Representante encontrado"),
        @ApiResponse(responseCode = "404", description = "Representante não encontrado com este email")
    })
    public ResponseEntity<RepresentativeResponseDTO> findByEmail(
            @Parameter(description = "Email do representante") @PathVariable String email) {
        log.info("Buscando representante por email: {}", email);
        RepresentativeViewDTO view = representativePort.findByEmail(email);
        RepresentativeResponseDTO response = representativeAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }
}
