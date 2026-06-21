package com.keepguard.ms_company.application.service.contact;

import com.keepguard.ms_company.application.dto.contact.ContactCreateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.contact.ContactSearchCriteriaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ContactUseCaseService
 * Testa a orquestração entre ContactCommandService e ContactQueryService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Contact Use Case Service Tests")
class ContactUseCaseServiceTest {
    
    @Mock
    private ContactCommandService commandService;
    
    @Mock
    private ContactQueryService queryService;
    
    @InjectMocks
    private ContactUseCaseService contactUseCaseService;
    
    private ContactViewDTO contactView;
    private UUID contactId;
    private UUID companyId;
    private ContactCreateCommandDTO createCommand;
    private ContactUpdateCommandDTO updateCommand;
    
    @BeforeEach
    void setUp() {
        contactId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        
        contactView = new ContactViewDTO(
            contactId,
            companyId,
            "João Silva",
            "joao@empresa.com",
            "11999999999",
            "www.empresa.com",
            "Gerente",
            "Vendas",
            true
        );
        
        createCommand = new ContactCreateCommandDTO(
            "João Silva",
            "joao@empresa.com",
            "11999999999",
            "www.empresa.com",
            "Gerente",
            "Vendas"
        );
        
        updateCommand = new ContactUpdateCommandDTO(
            "João Silva Atualizado",
            "joao.atualizado@empresa.com",
            "11888888888",
            "www.empresa.com.br",
            "Supervisor",
            "Marketing"
        );
    }
    
    // === TESTES DE COMANDO ===
    
    @Test
    @DisplayName("Deve criar contato delegando para CommandService")
    void shouldCreateContactDelegatingToCommandService() {
        // Given
        when(commandService.create(companyId, createCommand)).thenReturn(contactView);
        
        // When
        ContactViewDTO result = contactUseCaseService.create(companyId, createCommand);
        
        // Then
        assertNotNull(result);
        assertEquals(contactId, result.id());
        verify(commandService).create(companyId, createCommand);
        verifyNoInteractions(queryService);
    }
    
    @Test
    @DisplayName("Deve atualizar contato delegando para CommandService")
    void shouldUpdateContactDelegatingToCommandService() {
        // Given
        when(commandService.update(contactId, updateCommand)).thenReturn(contactView);
        
        // When
        ContactViewDTO result = contactUseCaseService.update(contactId, updateCommand);
        
        // Then
        assertNotNull(result);
        assertEquals(contactId, result.id());
        verify(commandService).update(contactId, updateCommand);
        verifyNoInteractions(queryService);
    }
    
    @Test
    @DisplayName("Deve ativar contato delegando para CommandService")
    void shouldActivateContactDelegatingToCommandService() {
        // Given
        when(commandService.activate(contactId)).thenReturn(contactView);
        
        // When
        ContactViewDTO result = contactUseCaseService.activate(contactId);
        
        // Then
        assertNotNull(result);
        assertEquals(contactId, result.id());
        verify(commandService).activate(contactId);
        verifyNoInteractions(queryService);
    }
    
    @Test
    @DisplayName("Deve desativar contato delegando para CommandService")
    void shouldDeactivateContactDelegatingToCommandService() {
        // Given
        when(commandService.deactivate(contactId)).thenReturn(contactView);
        
        // When
        ContactViewDTO result = contactUseCaseService.deactivate(contactId);
        
        // Then
        assertNotNull(result);
        assertEquals(contactId, result.id());
        verify(commandService).deactivate(contactId);
        verifyNoInteractions(queryService);
    }
    
    @Test
    @DisplayName("Deve deletar contato delegando para CommandService")
    void shouldDeleteContactDelegatingToCommandService() {
        // Given
        doNothing().when(commandService).delete(contactId);
        
        // When
        contactUseCaseService.delete(contactId);
        
        // Then
        verify(commandService).delete(contactId);
        verifyNoInteractions(queryService);
    }
    
    // === TESTES DE CONSULTA ===
    
    @Test
    @DisplayName("Deve buscar contato por ID delegando para QueryService")
    void shouldGetContactByIdDelegatingToQueryService() {
        // Given
        when(queryService.getById(contactId)).thenReturn(contactView);
        
        // When
        ContactViewDTO result = contactUseCaseService.getById(contactId);
        
        // Then
        assertNotNull(result);
        assertEquals(contactId, result.id());
        verify(queryService).getById(contactId);
        verifyNoInteractions(commandService);
    }
    
    @Test
    @DisplayName("Deve listar contatos por empresa delegando para QueryService")
    void shouldListContactsByCompanyIdDelegatingToQueryService() {
        // Given
        List<ContactViewDTO> contacts = List.of(contactView);
        when(queryService.listByCompanyId(companyId)).thenReturn(contacts);
        
        // When
        List<ContactViewDTO> result = contactUseCaseService.listByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(contactId, result.get(0).id());
        verify(queryService).listByCompanyId(companyId);
        verifyNoInteractions(commandService);
    }
    
    @Test
    @DisplayName("Deve listar contatos ativos por empresa delegando para QueryService")
    void shouldListActiveContactsByCompanyIdDelegatingToQueryService() {
        // Given
        List<ContactViewDTO> contacts = List.of(contactView);
        when(queryService.listActiveByCompanyId(companyId)).thenReturn(contacts);
        
        // When
        List<ContactViewDTO> result = contactUseCaseService.listActiveByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(contactId, result.get(0).id());
        verify(queryService).listActiveByCompanyId(companyId);
        verifyNoInteractions(commandService);
    }
    
    @Test
    @DisplayName("Deve listar todos os contatos delegando para QueryService")
    void shouldListAllContactsDelegatingToQueryService() {
        // Given
        List<ContactViewDTO> contacts = List.of(contactView);
        when(queryService.listAll()).thenReturn(contacts);
        
        // When
        List<ContactViewDTO> result = contactUseCaseService.listAll();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(contactId, result.get(0).id());
        verify(queryService).listAll();
        verifyNoInteractions(commandService);
    }
    
    @Test
    @DisplayName("Deve buscar contatos com critérios delegando para QueryService")
    void shouldSearchContactsDelegatingToQueryService() {
        // Given
        ContactSearchCriteriaDTO criteria = new ContactSearchCriteriaDTO(
            null, "João", null, null, null, null, 0, 10, null, "ASC"
        );
        PageResultDTO<ContactViewDTO> pageResult = new PageResultDTO<>(
            List.of(contactView), 1L, 0, 10
        );
        when(queryService.search(criteria)).thenReturn(pageResult);
        
        // When
        PageResultDTO<ContactViewDTO> result = contactUseCaseService.search(criteria);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());
        assertEquals(contactId, result.items().get(0).id());
        verify(queryService).search(criteria);
        verifyNoInteractions(commandService);
    }
}
