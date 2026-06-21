package com.keepguard.ms_company.application.service.company;

import com.keepguard.ms_company.application.dto.company.CompanyCreateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.company.CompanySearchCriteriaDTO;
import com.keepguard.ms_company.test.builder.CompanyTestBuilder;
import com.keepguard.ms_company.domain.enums.TaxRegimeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CompanyUseCaseService
 * Testa a orquestração entre CommandService e QueryService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Company Use Case Service Tests")
class CompanyUseCaseServiceTest {
    
    @Mock
    private CompanyCommandService commandService;
    
    @Mock
    private CompanyQueryService queryService;
    
    @InjectMocks
    private CompanyUseCaseService companyUseCaseService;
    
    private CompanyViewDTO companyView;
    private UUID companyId;
    private CompanyCreateCommandDTO createCommand;
    private CompanyUpdateCommandDTO updateCommand;
    
    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        
        // Criar view de teste usando builder
        companyView = CompanyTestBuilder.builder()
            .withId(companyId)
            .buildView();
        
        createCommand = new CompanyCreateCommandDTO(
            "Nova Empresa",
            "Nova Empresa Ltda",
            "98765432000198",
            "987654321",
            "123456789",
            TaxRegimeEnum.LUCRO_REAL,
            "987654321"
        );
        
        updateCommand = new CompanyUpdateCommandDTO(
            "Empresa Atualizada",
            "Empresa Atualizada Ltda",
            "987654321",
            "123456789",
            null,
            "987654321"
        );
    }
    
    // === TESTES DE COMANDO ===
    
    @Test
    @DisplayName("Deve criar empresa delegando para CommandService")
    void shouldCreateCompanyDelegatingToCommandService() {
        // Given
        when(commandService.create(createCommand)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyUseCaseService.create(createCommand);
        
        // Then
        assertNotNull(result);
        assertEquals(companyId, result.id());
        verify(commandService).create(createCommand);
        verifyNoInteractions(queryService);
    }
    
    @Test
    @DisplayName("Deve atualizar empresa delegando para CommandService")
    void shouldUpdateCompanyDelegatingToCommandService() {
        // Given
        when(commandService.update(companyId, updateCommand)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyUseCaseService.update(companyId, updateCommand);
        
        // Then
        assertNotNull(result);
        assertEquals(companyId, result.id());
        verify(commandService).update(companyId, updateCommand);
        verifyNoInteractions(queryService);
    }
    
    @Test
    @DisplayName("Deve aprovar empresa delegando para CommandService")
    void shouldApproveCompanyDelegatingToCommandService() {
        // Given
        when(commandService.approve(companyId)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyUseCaseService.approve(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(companyId, result.id());
        verify(commandService).approve(companyId);
        verifyNoInteractions(queryService);
    }
    
    @Test
    @DisplayName("Deve rejeitar empresa delegando para CommandService")
    void shouldRejectCompanyDelegatingToCommandService() {
        // Given
        when(commandService.reject(companyId)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyUseCaseService.reject(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(companyId, result.id());
        verify(commandService).reject(companyId);
        verifyNoInteractions(queryService);
    }
    
    @Test
    @DisplayName("Deve ativar empresa delegando para CommandService")
    void shouldActivateCompanyDelegatingToCommandService() {
        // Given
        when(commandService.activate(companyId)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyUseCaseService.activate(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(companyId, result.id());
        verify(commandService).activate(companyId);
        verifyNoInteractions(queryService);
    }
    
    @Test
    @DisplayName("Deve desativar empresa delegando para CommandService")
    void shouldDeactivateCompanyDelegatingToCommandService() {
        // Given
        when(commandService.deactivate(companyId)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyUseCaseService.deactivate(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(companyId, result.id());
        verify(commandService).deactivate(companyId);
        verifyNoInteractions(queryService);
    }
    
    @Test
    @DisplayName("Deve suspender empresa delegando para CommandService")
    void shouldSuspendCompanyDelegatingToCommandService() {
        // Given
        when(commandService.suspend(companyId)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyUseCaseService.suspend(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(companyId, result.id());
        verify(commandService).suspend(companyId);
        verifyNoInteractions(queryService);
    }
    
    @Test
    @DisplayName("Deve bloquear empresa delegando para CommandService")
    void shouldBlockCompanyDelegatingToCommandService() {
        // Given
        when(commandService.block(companyId)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyUseCaseService.block(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(companyId, result.id());
        verify(commandService).block(companyId);
        verifyNoInteractions(queryService);
    }
    
    @Test
    @DisplayName("Deve deletar empresa delegando para CommandService")
    void shouldDeleteCompanyDelegatingToCommandService() {
        // Given
        doNothing().when(commandService).delete(companyId);
        
        // When
        companyUseCaseService.delete(companyId);
        
        // Then
        verify(commandService).delete(companyId);
        verifyNoInteractions(queryService);
    }
    
    // === TESTES DE CONSULTA ===
    
    @Test
    @DisplayName("Deve buscar empresa por ID delegando para QueryService")
    void shouldGetCompanyByIdDelegatingToQueryService() {
        // Given
        when(queryService.getById(companyId)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyUseCaseService.getById(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(companyId, result.id());
        verify(queryService).getById(companyId);
        verifyNoInteractions(commandService);
    }
    
    @Test
    @DisplayName("Deve buscar empresa por CNPJ delegando para QueryService")
    void shouldGetCompanyByCnpjDelegatingToQueryService() {
        // Given
        String cnpj = "11222333000181";
        when(queryService.getByCnpj(cnpj)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyUseCaseService.getByCnpj(cnpj);
        
        // Then
        assertNotNull(result);
        assertEquals(companyId, result.id());
        verify(queryService).getByCnpj(cnpj);
        verifyNoInteractions(commandService);
    }
    
    
    @Test
    @DisplayName("Deve buscar empresas com critérios delegando para QueryService")
    void shouldSearchCompaniesDelegatingToQueryService() {
        // Given
        CompanySearchCriteriaDTO criteria = new CompanySearchCriteriaDTO(
            "Empresa", null, null, null, null, null, 0, 10, null, "ASC"
        );
        PageResultDTO<CompanyViewDTO> pageResult = new PageResultDTO<>(
            List.of(companyView), 1L, 0, 10
        );
        when(queryService.search(criteria)).thenReturn(pageResult);
        
        // When
        PageResultDTO<CompanyViewDTO> result = companyUseCaseService.search(criteria);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());
        assertEquals(companyId, result.items().get(0).id());
        verify(queryService).search(criteria);
        verifyNoInteractions(commandService);
    }
}
