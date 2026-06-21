package com.keepguard.ms_company.adapters.in.rest.cnae;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.ms_company.adapters.in.rest.cnae.dto.CnaeCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.cnae.dto.CnaeResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.cnae.dto.CnaeUpdateDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeViewDTO;
import com.keepguard.ms_company.adapters.in.rest.cnae.mapper.CnaeAdapterMapper;
import com.keepguard.ms_company.application.port.in.CnaePort;
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
@RequestMapping("/api/v1/companies/{companyId}/cnaes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CNAE", description = "API para gerenciamento de CNAEs")
public class CnaeController {

    private final CnaePort cnaePort;
    private final CnaeAdapterMapper cnaeAdapterMapper;

    @PostMapping
    @MetricsEndpoint(endpoint = "cnae_create", operation = "Criar CNAE")
    @Operation(summary = "Criar novo CNAE", description = "Cria um novo CNAE para uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "CNAE criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    public ResponseEntity<CnaeResponseDTO> create(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId,
            @Valid @RequestBody CnaeCreateDTO dto) {
        log.info("Criando CNAE para empresa: {}", companyId);
        CnaeCreateCommandDTO command = cnaeAdapterMapper.toCreateCommand(dto, companyId);
        CnaeViewDTO view = cnaePort.create(companyId, command);
        CnaeResponseDTO response = cnaeAdapterMapper.toResponseDTO(view);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @MetricsEndpoint(endpoint = "cnae_update", operation = "Atualizar CNAE")
    @Operation(summary = "Atualizar CNAE", description = "Atualiza os dados de um CNAE existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "CNAE atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "CNAE não encontrado")
    })
    public ResponseEntity<CnaeResponseDTO> update(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId,
            @Parameter(description = "ID do CNAE") @PathVariable UUID id,
            @Valid @RequestBody CnaeUpdateDTO dto) {
        log.info("Atualizando CNAE ID: {}", id);

        CnaeUpdateCommandDTO command = cnaeAdapterMapper.toUpdateCommand(dto);
        CnaeViewDTO view = cnaePort.update(id, command);
        CnaeResponseDTO response = cnaeAdapterMapper.toResponseDTO(view);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/activate")
    @MetricsEndpoint(endpoint = "cnae_activate", operation = "Ativar CNAE")
    @Operation(summary = "Ativar CNAE", description = "Ativa um CNAE")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "CNAE ativado com sucesso"),
        @ApiResponse(responseCode = "404", description = "CNAE não encontrado")
    })
    public ResponseEntity<CnaeResponseDTO> activate(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId,
            @Parameter(description = "ID do CNAE") @PathVariable UUID id) {
        log.info("Ativando CNAE ID: {}", id);

        CnaeViewDTO view = cnaePort.activate(id);
        CnaeResponseDTO response = cnaeAdapterMapper.toResponseDTO(view);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/deactivate")
    @MetricsEndpoint(endpoint = "cnae_deactivate", operation = "Desativar CNAE")
    @Operation(summary = "Desativar CNAE", description = "Desativa um CNAE")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "CNAE desativado com sucesso"),
        @ApiResponse(responseCode = "404", description = "CNAE não encontrado")
    })
    public ResponseEntity<CnaeResponseDTO> deactivate(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId,
            @Parameter(description = "ID do CNAE") @PathVariable UUID id) {
        log.info("Desativando CNAE ID: {}", id);

        CnaeViewDTO view = cnaePort.deactivate(id);
        CnaeResponseDTO response = cnaeAdapterMapper.toResponseDTO(view);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/set-principal")
    @MetricsEndpoint(endpoint = "cnae_set_principal", operation = "Definir CNAE como principal")
    @Operation(summary = "Definir CNAE como principal", description = "Define um CNAE como principal da empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "CNAE definido como principal com sucesso"),
        @ApiResponse(responseCode = "404", description = "CNAE não encontrado")
    })
    public ResponseEntity<CnaeResponseDTO> setAsPrincipal(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId,
            @Parameter(description = "ID do CNAE") @PathVariable UUID id) {
        log.info("Definindo CNAE ID: {} como principal", id);

        CnaeViewDTO view = cnaePort.setAsPrincipal(id);
        CnaeResponseDTO response = cnaeAdapterMapper.toResponseDTO(view);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @MetricsEndpoint(endpoint = "cnae_get_by_id", operation = "Buscar CNAE por ID")
    @Operation(summary = "Buscar CNAE por ID", description = "Retorna os dados de um CNAE pelo ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "CNAE encontrado"),
        @ApiResponse(responseCode = "404", description = "CNAE não encontrado")
    })
    public ResponseEntity<CnaeResponseDTO> getById(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId,
            @Parameter(description = "ID do CNAE") @PathVariable UUID id) {
        log.info("Buscando CNAE por ID: {}", id);

        CnaeViewDTO view = cnaePort.getById(id);
        CnaeResponseDTO response = cnaeAdapterMapper.toResponseDTO(view);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @MetricsEndpoint(endpoint = "cnae_list_by_company", operation = "Listar CNAEs por empresa")
    @Operation(summary = "Listar CNAEs por empresa", description = "Lista todos os CNAEs de uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de CNAEs retornada com sucesso")
    })
    public ResponseEntity<List<CnaeResponseDTO>> listByCompanyId(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId) {
        log.info("Listando CNAEs da empresa: {}", companyId);

        List<CnaeViewDTO> views = cnaePort.listByCompanyId(companyId);
        List<CnaeResponseDTO> response = views.stream()
            .map(cnaeAdapterMapper::toResponseDTO)
            .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/principal")
    @MetricsEndpoint(endpoint = "cnae_get_principal_by_company", operation = "Buscar CNAE principal por empresa")
    @Operation(summary = "Buscar CNAE principal por empresa", description = "Retorna o CNAE principal de uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "CNAE principal encontrado"),
        @ApiResponse(responseCode = "404", description = "CNAE principal não encontrado")
    })
    public ResponseEntity<CnaeResponseDTO> getPrincipalByCompanyId(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId) {
        log.info("Buscando CNAE principal da empresa: {}", companyId);

        CnaeViewDTO view = cnaePort.getPrincipalByCompanyId(companyId);
        CnaeResponseDTO response = cnaeAdapterMapper.toResponseDTO(view);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @MetricsEndpoint(endpoint = "cnae_list_active_by_company", operation = "Listar CNAEs ativos por empresa")
    @Operation(summary = "Listar CNAEs ativos por empresa", description = "Lista todos os CNAEs ativos de uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de CNAEs ativos retornada com sucesso")
    })
    public ResponseEntity<List<CnaeResponseDTO>> listActiveByCompanyId(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId) {
        log.info("Listando CNAEs ativos da empresa: {}", companyId);

        List<CnaeViewDTO> views = cnaePort.listActiveByCompanyId(companyId);
        List<CnaeResponseDTO> response = views.stream()
            .map(cnaeAdapterMapper::toResponseDTO)
            .toList();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @MetricsEndpoint(endpoint = "cnae_delete", operation = "Remover CNAE")
    @Operation(summary = "Remover CNAE", description = "Remove um CNAE do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "CNAE removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "CNAE não encontrado")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId,
            @Parameter(description = "ID do CNAE") @PathVariable UUID id) {
        log.info("Removendo CNAE ID: {}", id);

        cnaePort.delete(id);

        return ResponseEntity.noContent().build();
    }
}
