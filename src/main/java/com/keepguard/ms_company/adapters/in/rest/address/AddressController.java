package com.keepguard.ms_company.adapters.in.rest.address;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressUpdateDTO;
import com.keepguard.ms_company.application.dto.address.AddressCreateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.address.AddressSearchCriteriaDTO;
import com.keepguard.ms_company.adapters.in.rest.address.mapper.AddressAdapterMapper;
import com.keepguard.ms_company.application.port.in.AddressPort;
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
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Address", description = "API para gerenciamento de endereços")
public class AddressController {

    private final AddressPort addressPort;
    private final AddressAdapterMapper addressAdapterMapper;

    @PostMapping("/company/{companyId}")
    @MetricsEndpoint(endpoint = "address_create", operation = "Criar endereço")
    @Operation(summary = "Criar novo endereço", description = "Cria um novo endereço para uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Endereço criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada")
    })
    public ResponseEntity<AddressResponseDTO> create(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId,
            @Valid @RequestBody AddressCreateDTO dto) {
        log.info("Criando endereço para empresa: {}", companyId);
        AddressCreateCommandDTO command = addressAdapterMapper.toCreateCommand(dto);
        AddressViewDTO view = addressPort.create(companyId, command);
        AddressResponseDTO response = addressAdapterMapper.toResponseDTO(view);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @MetricsEndpoint(endpoint = "address_update", operation = "Atualizar endereço")
    @Operation(summary = "Atualizar endereço", description = "Atualiza os dados de um endereço existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Endereço atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    public ResponseEntity<AddressResponseDTO> update(
            @Parameter(description = "ID do endereço") @PathVariable UUID id,
            @Valid @RequestBody AddressUpdateDTO dto) {
        log.info("Atualizando endereço ID: {}", id);
        AddressUpdateCommandDTO command = addressAdapterMapper.toUpdateCommand(dto);
        AddressViewDTO view = addressPort.update(id, command);
        AddressResponseDTO response = addressAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/activate")
    @MetricsEndpoint(endpoint = "address_activate", operation = "Ativar endereço")
    @Operation(summary = "Ativar endereço", description = "Ativa um endereço")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Endereço ativado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    public ResponseEntity<AddressResponseDTO> activate(
            @Parameter(description = "ID do endereço") @PathVariable UUID id) {
        log.info("Ativando endereço ID: {}", id);
        AddressViewDTO view = addressPort.activate(id);
        AddressResponseDTO response = addressAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/deactivate")
    @MetricsEndpoint(endpoint = "address_deactivate", operation = "Desativar endereço")
    @Operation(summary = "Desativar endereço", description = "Desativa um endereço")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Endereço desativado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    public ResponseEntity<AddressResponseDTO> deactivate(
            @Parameter(description = "ID do endereço") @PathVariable UUID id) {
        log.info("Desativando endereço ID: {}", id);
        AddressViewDTO view = addressPort.deactivate(id);
        AddressResponseDTO response = addressAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @MetricsEndpoint(endpoint = "address_get_by_id", operation = "Buscar endereço por ID")
    @Operation(summary = "Buscar endereço por ID", description = "Retorna os dados de um endereço pelo ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Endereço encontrado"),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    public ResponseEntity<AddressResponseDTO> getById(
            @Parameter(description = "ID do endereço") @PathVariable UUID id) {
        log.info("Buscando endereço por ID: {}", id);
        AddressViewDTO view = addressPort.getById(id);
        AddressResponseDTO response = addressAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}")
    @MetricsEndpoint(endpoint = "address_list_by_company", operation = "Listar endereços por empresa")
    @Operation(summary = "Listar endereços por empresa", description = "Lista todos os endereços de uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de endereços retornada com sucesso")
    })
    public ResponseEntity<List<AddressResponseDTO>> listByCompanyId(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId) {
        log.info("Listando endereços da empresa: {}", companyId);
        List<AddressViewDTO> views = addressPort.listByCompanyId(companyId);
        List<AddressResponseDTO> response = views.stream()
            .map(addressAdapterMapper::toResponseDTO)
            .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}/active")
    @MetricsEndpoint(endpoint = "address_get_active_by_company", operation = "Buscar endereço ativo por empresa")
    @Operation(summary = "Buscar endereço ativo por empresa", description = "Retorna o endereço ativo de uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Endereço ativo encontrado"),
        @ApiResponse(responseCode = "404", description = "Endereço ativo não encontrado")
    })
    public ResponseEntity<AddressResponseDTO> getActiveByCompanyId(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId) {
        log.info("Buscando endereço ativo da empresa: {}", companyId);
        AddressViewDTO view = addressPort.getActiveByCompanyId(companyId);
        AddressResponseDTO response = addressAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @MetricsEndpoint(endpoint = "address_list", operation = "Listar endereços")
    @Operation(summary = "Listar endereços", description = "Lista todos os endereços com paginação")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de endereços retornada com sucesso")
    })
    public ResponseEntity<PageResultDTO<AddressResponseDTO>> list(
            @Parameter(description = "Página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size) {
        log.info("Listando endereços - página: {}, tamanho: {}", page, size);
        AddressSearchCriteriaDTO criteria = new AddressSearchCriteriaDTO(
            null, null, null, null, null, page, size, null, "ASC"
        );
        PageResultDTO<AddressViewDTO> views = addressPort.search(criteria);
        PageResultDTO<AddressResponseDTO> response = new PageResultDTO<>(
            views.items().stream()
                .map(addressAdapterMapper::toResponseDTO)
                .toList(),
            views.total(),
            views.page(),
            views.size()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @MetricsEndpoint(endpoint = "address_search", operation = "Buscar endereços")
    @Operation(summary = "Buscar endereços", description = "Busca endereços com filtros")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de endereços retornada com sucesso")
    })
    public ResponseEntity<PageResultDTO<AddressResponseDTO>> search(
            @Parameter(description = "Filtro por empresa") @RequestParam(required = false) UUID companyId,
            @Parameter(description = "Filtro por cidade") @RequestParam(required = false) String city,
            @Parameter(description = "Filtro por estado") @RequestParam(required = false) String state,
            @Parameter(description = "Filtro por CEP") @RequestParam(required = false) String zipCode,
            @Parameter(description = "Filtro por status ativo") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campos para ordenação") @RequestParam(required = false) List<String> sortFields,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "ASC") String sortDirection) {
        log.info("Buscando endereços com filtros - empresa: {}, cidade: {}", companyId, city);
        AddressSearchCriteriaDTO criteria = new AddressSearchCriteriaDTO(
            companyId, city, state, zipCode, active, page, size, sortFields, sortDirection
        );
        PageResultDTO<AddressViewDTO> views = addressPort.search(criteria);
        PageResultDTO<AddressResponseDTO> response = new PageResultDTO<>(
            views.items().stream()
                .map(addressAdapterMapper::toResponseDTO)
                .toList(),
            views.total(),
            views.page(),
            views.size()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @MetricsEndpoint(endpoint = "address_list_all", operation = "Listar todos os endereços")
    @Operation(summary = "Listar todos os endereços", description = "Retorna todos os endereços sem paginação")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de todos os endereços")
    })
    public ResponseEntity<List<AddressResponseDTO>> listAll() {
        log.info("Listando todos os endereços");
        List<AddressViewDTO> views = addressPort.listAll();
        List<AddressResponseDTO> response = views.stream()
            .map(addressAdapterMapper::toResponseDTO)
            .toList();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @MetricsEndpoint(endpoint = "address_delete", operation = "Remover endereço")
    @Operation(summary = "Remover endereço", description = "Remove um endereço do sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Endereço removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do endereço") @PathVariable UUID id) {
        log.info("Removendo endereço ID: {}", id);
        addressPort.delete(id);
        return ResponseEntity.noContent().build();
    }
}
