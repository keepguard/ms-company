package com.keepguard.ms_company.adapters.in.rest.contact;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactUpdateDTO;
import com.keepguard.ms_company.application.dto.contact.ContactCreateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.contact.ContactSearchCriteriaDTO;
import com.keepguard.ms_company.adapters.in.rest.contact.mapper.ContactAdapterMapper;
import com.keepguard.ms_company.application.port.in.ContactPort;
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
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Contact", description = "API para gerenciamento de contatos")
public class ContactController {

    private final ContactPort contactPort;
    private final ContactAdapterMapper contactAdapterMapper;

    @PostMapping("/company/{companyId}")
    @MetricsEndpoint(endpoint = "contact_create", operation = "Criar contato")
    @Operation(summary = "Criar novo contato", description = "Cria um novo contato para uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Contato criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Empresa não encontrada"),
        @ApiResponse(responseCode = "409", description = "Email já existe")
    })
    public ResponseEntity<ContactResponseDTO> create(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId,
            @Valid @RequestBody ContactCreateDTO dto) {
        log.info("Criando contato para empresa: {}", companyId);
        ContactCreateCommandDTO command = contactAdapterMapper.toCreateCommand(dto);
        ContactViewDTO view = contactPort.create(companyId, command);
        ContactResponseDTO response = contactAdapterMapper.toResponseDTO(view);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @MetricsEndpoint(endpoint = "contact_update", operation = "Atualizar contato")
    @Operation(summary = "Atualizar contato", description = "Atualiza um contato existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contato atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Contato não encontrado"),
        @ApiResponse(responseCode = "409", description = "Email já existe")
    })
    public ResponseEntity<ContactResponseDTO> update(
            @Parameter(description = "ID do contato") @PathVariable UUID id,
            @Valid @RequestBody ContactUpdateDTO dto) {
        log.info("Atualizando contato ID: {}", id);
        ContactUpdateCommandDTO command = contactAdapterMapper.toUpdateCommand(dto);
        ContactViewDTO view = contactPort.update(id, command);
        ContactResponseDTO response = contactAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/activate")
    @MetricsEndpoint(endpoint = "contact_activate", operation = "Ativar contato")
    @Operation(summary = "Ativar contato", description = "Ativa um contato")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contato ativado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Contato não encontrado")
    })
    public ResponseEntity<ContactResponseDTO> activate(
            @Parameter(description = "ID do contato") @PathVariable UUID id) {
        log.info("Ativando contato ID: {}", id);
        ContactViewDTO view = contactPort.activate(id);
        ContactResponseDTO response = contactAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/deactivate")
    @MetricsEndpoint(endpoint = "contact_deactivate", operation = "Desativar contato")
    @Operation(summary = "Desativar contato", description = "Desativa um contato")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contato desativado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Contato não encontrado")
    })
    public ResponseEntity<ContactResponseDTO> deactivate(
            @Parameter(description = "ID do contato") @PathVariable UUID id) {
        log.info("Desativando contato ID: {}", id);
        ContactViewDTO view = contactPort.deactivate(id);
        ContactResponseDTO response = contactAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @MetricsEndpoint(endpoint = "contact_delete", operation = "Remover contato")
    @Operation(summary = "Remover contato", description = "Remove um contato")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Contato removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Contato não encontrado")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do contato") @PathVariable UUID id) {
        log.info("Removendo contato ID: {}", id);
        contactPort.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @MetricsEndpoint(endpoint = "contact_get_by_id", operation = "Buscar contato por ID")
    @Operation(summary = "Buscar contato por ID", description = "Busca um contato pelo seu ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contato encontrado"),
        @ApiResponse(responseCode = "404", description = "Contato não encontrado")
    })
    public ResponseEntity<ContactResponseDTO> getById(
            @Parameter(description = "ID do contato") @PathVariable UUID id) {
        log.info("Buscando contato por ID: {}", id);
        ContactViewDTO view = contactPort.getById(id);
        ContactResponseDTO response = contactAdapterMapper.toResponseDTO(view);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/{companyId}")
    @MetricsEndpoint(endpoint = "contact_list_by_company", operation = "Listar contatos por empresa")
    @Operation(summary = "Listar contatos por empresa", description = "Lista todos os contatos de uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de contatos retornada com sucesso")
    })
    public ResponseEntity<List<ContactResponseDTO>> listByCompanyId(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId) {
        log.info("Listando contatos da empresa: {}", companyId);
        List<ContactViewDTO> views = contactPort.listByCompanyId(companyId);
        List<ContactResponseDTO> responses = views.stream()
            .map(contactAdapterMapper::toResponseDTO)
            .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/company/{companyId}/active")
    @MetricsEndpoint(endpoint = "contact_list_active_by_company", operation = "Listar contatos ativos por empresa")
    @Operation(summary = "Listar contatos ativos por empresa", description = "Lista todos os contatos ativos de uma empresa")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de contatos ativos retornada com sucesso")
    })
    public ResponseEntity<List<ContactResponseDTO>> listActiveByCompanyId(
            @Parameter(description = "ID da empresa") @PathVariable UUID companyId) {
        log.info("Listando contatos ativos da empresa: {}", companyId);
        List<ContactViewDTO> views = contactPort.listActiveByCompanyId(companyId);
        List<ContactResponseDTO> responses = views.stream()
            .map(contactAdapterMapper::toResponseDTO)
            .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    @MetricsEndpoint(endpoint = "contact_list_all", operation = "Listar todos os contatos")
    @Operation(summary = "Listar todos os contatos", description = "Lista todos os contatos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de contatos retornada com sucesso")
    })
    public ResponseEntity<List<ContactResponseDTO>> listAll() {
        log.info("Listando todos os contatos");
        List<ContactViewDTO> views = contactPort.listAll();
        List<ContactResponseDTO> responses = views.stream()
            .map(contactAdapterMapper::toResponseDTO)
            .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    @MetricsEndpoint(endpoint = "contact_search", operation = "Buscar contatos")
    @Operation(summary = "Buscar contatos", description = "Busca contatos com filtros e paginação")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Busca de contatos realizada com sucesso")
    })
    public ResponseEntity<PageResultDTO<ContactResponseDTO>> search(
            @Parameter(description = "ID da empresa") @RequestParam(required = false) UUID companyId,
            @Parameter(description = "Nome do contato") @RequestParam(required = false) String name,
            @Parameter(description = "Email do contato") @RequestParam(required = false) String email,
            @Parameter(description = "Cargo do contato") @RequestParam(required = false) String position,
            @Parameter(description = "Departamento do contato") @RequestParam(required = false) String department,
            @Parameter(description = "Status ativo") @RequestParam(required = false) Boolean active,
            @Parameter(description = "Página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campos para ordenação") @RequestParam(required = false) List<String> sortFields,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "ASC") String sortDirection) {
        log.info("Buscando contatos com filtros");
        ContactSearchCriteriaDTO criteria = new ContactSearchCriteriaDTO(
            companyId, name, email, position, department, active,
            page, size, sortFields, sortDirection
        );
        PageResultDTO<ContactViewDTO> views = contactPort.search(criteria);
        PageResultDTO<ContactResponseDTO> responses = new PageResultDTO<>(
            views.items().stream()
                .map(contactAdapterMapper::toResponseDTO)
                .toList(),
            views.total(),
            views.page(),
            views.size()
        );
        return ResponseEntity.ok(responses);
    }
}
