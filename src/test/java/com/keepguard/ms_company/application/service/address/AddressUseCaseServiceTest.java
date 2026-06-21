package com.keepguard.ms_company.application.service.address;

import com.keepguard.ms_company.application.dto.address.AddressCreateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.address.AddressSearchCriteriaDTO;
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
 * Testes unitários para AddressUseCaseService
 * Testa a orquestração entre AddressCommandService e AddressQueryService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Address Use Case Service Tests")
class AddressUseCaseServiceTest {
    
    @Mock
    private AddressCommandService commandService;
    
    @Mock
    private AddressQueryService queryService;
    
    @InjectMocks
    private AddressUseCaseService addressUseCaseService;
    
    private AddressViewDTO addressView;
    private UUID addressId;
    private UUID companyId;
    private AddressCreateCommandDTO createCommand;
    private AddressUpdateCommandDTO updateCommand;
    
    @BeforeEach
    void setUp() {
        addressId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        
        addressView = new AddressViewDTO(
            addressId,
            companyId,
            "Rua das Flores",
            "123",
            "Apto 45",
            "Centro",
            "São Paulo",
            "SP",
            "Brasil",
            "01234-567",
            true
        );
        
        createCommand = new AddressCreateCommandDTO(
            "Rua das Flores",
            "123",
            "Apto 45",
            "Centro",
            "São Paulo",
            "SP",
            "Brasil",
            "01234-567"
        );
        
        updateCommand = new AddressUpdateCommandDTO(
            "Rua das Flores",
            "456",
            "Apto 50",
            "Centro",
            "São Paulo",
            "SP",
            "Brasil",
            "01234-567"
        );
    }
    
    // === TESTES DE COMANDO ===
    
    @Test
    @DisplayName("Deve criar endereço delegando para CommandService")
    void shouldCreateAddressDelegatingToCommandService() {
        // Given
        when(commandService.create(companyId, createCommand)).thenReturn(addressView);
        
        // When
        AddressViewDTO result = addressUseCaseService.create(companyId, createCommand);
        
        // Then
        assertNotNull(result);
        assertEquals(addressId, result.id());
        verify(commandService).create(companyId, createCommand);
        verifyNoInteractions(queryService);
    }
    
    @Test
    @DisplayName("Deve atualizar endereço delegando para CommandService")
    void shouldUpdateAddressDelegatingToCommandService() {
        // Given
        when(commandService.update(addressId, updateCommand)).thenReturn(addressView);
        
        // When
        AddressViewDTO result = addressUseCaseService.update(addressId, updateCommand);
        
        // Then
        assertNotNull(result);
        assertEquals(addressId, result.id());
        verify(commandService).update(addressId, updateCommand);
        verifyNoInteractions(queryService);
    }
    
    @Test
    @DisplayName("Deve ativar endereço delegando para CommandService")
    void shouldActivateAddressDelegatingToCommandService() {
        // Given
        when(commandService.activate(addressId)).thenReturn(addressView);
        
        // When
        AddressViewDTO result = addressUseCaseService.activate(addressId);
        
        // Then
        assertNotNull(result);
        assertEquals(addressId, result.id());
        verify(commandService).activate(addressId);
        verifyNoInteractions(queryService);
    }
    
    @Test
    @DisplayName("Deve desativar endereço delegando para CommandService")
    void shouldDeactivateAddressDelegatingToCommandService() {
        // Given
        when(commandService.deactivate(addressId)).thenReturn(addressView);
        
        // When
        AddressViewDTO result = addressUseCaseService.deactivate(addressId);
        
        // Then
        assertNotNull(result);
        assertEquals(addressId, result.id());
        verify(commandService).deactivate(addressId);
        verifyNoInteractions(queryService);
    }
    
    @Test
    @DisplayName("Deve deletar endereço delegando para CommandService")
    void shouldDeleteAddressDelegatingToCommandService() {
        // Given
        doNothing().when(commandService).delete(addressId);
        
        // When
        addressUseCaseService.delete(addressId);
        
        // Then
        verify(commandService).delete(addressId);
        verifyNoInteractions(queryService);
    }
    
    // === TESTES DE CONSULTA ===
    
    @Test
    @DisplayName("Deve buscar endereço por ID delegando para QueryService")
    void shouldGetAddressByIdDelegatingToQueryService() {
        // Given
        when(queryService.getById(addressId)).thenReturn(addressView);
        
        // When
        AddressViewDTO result = addressUseCaseService.getById(addressId);
        
        // Then
        assertNotNull(result);
        assertEquals(addressId, result.id());
        verify(queryService).getById(addressId);
        verifyNoInteractions(commandService);
    }
    
    @Test
    @DisplayName("Deve listar endereços por empresa delegando para QueryService")
    void shouldListAddressesByCompanyIdDelegatingToQueryService() {
        // Given
        List<AddressViewDTO> addresses = List.of(addressView);
        when(queryService.listByCompanyId(companyId)).thenReturn(addresses);
        
        // When
        List<AddressViewDTO> result = addressUseCaseService.listByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(addressId, result.get(0).id());
        verify(queryService).listByCompanyId(companyId);
        verifyNoInteractions(commandService);
    }
    
    @Test
    @DisplayName("Deve buscar endereço ativo por empresa delegando para QueryService")
    void shouldGetActiveAddressByCompanyIdDelegatingToQueryService() {
        // Given
        when(queryService.getActiveByCompanyId(companyId)).thenReturn(addressView);
        
        // When
        AddressViewDTO result = addressUseCaseService.getActiveByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(addressId, result.id());
        verify(queryService).getActiveByCompanyId(companyId);
        verifyNoInteractions(commandService);
    }
    
    @Test
    @DisplayName("Deve listar todos os endereços delegando para QueryService")
    void shouldListAllAddressesDelegatingToQueryService() {
        // Given
        List<AddressViewDTO> addresses = List.of(addressView);
        when(queryService.listAll()).thenReturn(addresses);
        
        // When
        List<AddressViewDTO> result = addressUseCaseService.listAll();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(addressId, result.get(0).id());
        verify(queryService).listAll();
        verifyNoInteractions(commandService);
    }
    
    @Test
    @DisplayName("Deve buscar endereços com critérios delegando para QueryService")
    void shouldSearchAddressesDelegatingToQueryService() {
        // Given
        AddressSearchCriteriaDTO criteria = new AddressSearchCriteriaDTO(
            companyId, "São Paulo", null, null, null, 0, 10, null, "ASC"
        );
        PageResultDTO<AddressViewDTO> pageResult = new PageResultDTO<>(
            List.of(addressView), 1L, 0, 10
        );
        when(queryService.search(criteria)).thenReturn(pageResult);
        
        // When
        PageResultDTO<AddressViewDTO> result = addressUseCaseService.search(criteria);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());
        assertEquals(addressId, result.items().get(0).id());
        verify(queryService).search(criteria);
        verifyNoInteractions(commandService);
    }
}
