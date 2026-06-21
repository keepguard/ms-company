package com.keepguard.ms_company.application.service.cnae;

import com.keepguard.ms_company.application.dto.cnae.CnaeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeViewDTO;
import com.keepguard.ms_company.application.mapper.CnaeApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.CnaeRepositoryPort;
import com.keepguard.ms_company.application.port.out.persistence.CompanyRepositoryPort;
import com.keepguard.ms_company.domain.entity.Cnae;
import com.keepguard.ms_company.domain.entity.Company;
import com.keepguard.ms_company.domain.enums.TaxRegimeEnum;
import com.keepguard.ms_company.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_company.test.builder.CnaeTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CnaeCommandService
 * Inclui verificações de métricas usando o serviço genérico MetricsService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Cnae Command Service Tests")
class CnaeCommandServiceTest {
    
    @Mock
    private CnaeRepositoryPort cnaeRepository;
    
    @Mock
    private CompanyRepositoryPort companyRepository;
    
    @Mock
    private CnaeApplicationMapper cnaeMapper;
    
    @Mock
    private MetricsPort metricsPort;
    
    @InjectMocks
    private CnaeCommandService cnaeCommandService;
    
    private Cnae cnae;
    private CnaeViewDTO cnaeView;
    private UUID cnaeId;
    private UUID companyId;
    
    @BeforeEach
    void setUp() {
        cnaeId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        
        // Criar CNAE de teste usando builder
        cnae = CnaeTestBuilder.builder()
            .withId(cnaeId)
            .withCompanyId(companyId)
            .buildDomain();
        
        // Criar view de teste usando builder
        cnaeView = CnaeTestBuilder.builder()
            .withId(cnaeId)
            .withCompanyId(companyId)
            .buildView();
    }
    
    @Test
    @DisplayName("Deve criar CNAE com sucesso")
    void shouldCreateCnaeSuccessfully() {
        // Given
        CnaeCreateCommandDTO command = CnaeTestBuilder.builder()
            .withCompanyId(companyId)
            .buildCreateCommand();
        
        Company company = Company.create(
            "Empresa Teste",
            "Empresa Teste LTDA",
            "12345678000195",
            "123456789",
            "987654321",
            TaxRegimeEnum.SIMPLES_NACIONAL,
            "123456789"
        );
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(cnaeRepository.existsByCompanyIdAndCode(companyId, command.code())).thenReturn(false);
        when(cnaeRepository.save(any(Cnae.class))).thenReturn(cnae);
        when(cnaeMapper.toViewDTO(any(Cnae.class))).thenReturn(cnaeView);
        
        // When
        CnaeViewDTO result = cnaeCommandService.create(companyId, command);
        
        // Then
        assertNotNull(result);
        assertEquals(cnaeView.id(), result.id());
        assertEquals(cnaeView.code(), result.code());
        assertEquals(cnaeView.description(), result.description());
        
        verify(companyRepository).findById(companyId);
        verify(cnaeRepository).existsByCompanyIdAndCode(companyId, command.code());
        verify(cnaeRepository).save(any(Cnae.class));
        verify(cnaeMapper).toViewDTO(any(Cnae.class));
        verify(metricsPort).incrementCounter(eq("cnae_created_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando CNAE já existe")
    void shouldThrowExceptionWhenCnaeAlreadyExists() {
        // Given
        CnaeCreateCommandDTO command = CnaeTestBuilder.builder()
            .withCompanyId(companyId)
            .buildCreateCommand();
        
        Company company = Company.create(
            "Empresa Teste",
            "Empresa Teste LTDA",
            "12345678000195",
            "123456789",
            "987654321",
            TaxRegimeEnum.SIMPLES_NACIONAL,
            "123456789"
        );
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(cnaeRepository.existsByCompanyIdAndCode(companyId, command.code())).thenReturn(true);
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cnaeCommandService.create(companyId, command);
        });
        
        assertEquals("CNAE já existe para esta empresa: " + command.code(), exception.getMessage());
        
        verify(companyRepository).findById(companyId);
        verify(cnaeRepository).existsByCompanyIdAndCode(companyId, command.code());
        verify(cnaeRepository, never()).save(any(Cnae.class));
        verify(metricsPort).incrementCounter(eq("cnae_business_errors_total"), any());
    }
    
    @Test
    @DisplayName("Deve atualizar CNAE com sucesso")
    void shouldUpdateCnaeSuccessfully() {
        // Given
        CnaeUpdateCommandDTO command = CnaeTestBuilder.builder()
            .withCode("7654321")
            .withDescription("Nova atividade de software")
            .withSection("K")
            .withDivision("63")
            .withGroupCode("630")
            .withClassCode("6301")
            .withSubclassCode("63015")
            .buildUpdateCommand();
        
        Company company = Company.create(
            "Empresa Teste",
            "Empresa Teste LTDA",
            "12345678000195",
            "123456789",
            "987654321",
            TaxRegimeEnum.SIMPLES_NACIONAL,
            "123456789"
        );
        
        when(cnaeRepository.findById(cnaeId)).thenReturn(Optional.of(cnae));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(cnaeRepository.save(any(Cnae.class))).thenReturn(cnae);
        when(cnaeMapper.toViewDTO(any(Cnae.class))).thenReturn(cnaeView);
        
        // When
        CnaeViewDTO result = cnaeCommandService.update(cnaeId, command);
        
        // Then
        assertNotNull(result);
        assertEquals(cnaeView.id(), result.id());
        
        verify(cnaeRepository).findById(cnaeId);
        verify(companyRepository).findById(companyId);
        verify(cnaeRepository).save(any(Cnae.class));
        verify(cnaeMapper).toViewDTO(any(Cnae.class));
        verify(metricsPort).incrementCounter(eq("cnae_updated_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando CNAE não encontrado para atualização")
    void shouldThrowExceptionWhenCnaeNotFoundForUpdate() {
        // Given
        CnaeUpdateCommandDTO command = CnaeTestBuilder.builder()
            .withCode("7654321")
            .withDescription("Nova atividade de software")
            .buildUpdateCommand();
        
        when(cnaeRepository.findById(cnaeId)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cnaeCommandService.update(cnaeId, command);
        });
        
        assertEquals("CNAE não encontrado: " + cnaeId, exception.getMessage());
        
        verify(cnaeRepository).findById(cnaeId);
        verify(cnaeRepository, never()).save(any(Cnae.class));
        verify(metricsPort).incrementCounter(eq("cnae_business_errors_total"), any());
    }
    
    @Test
    @DisplayName("Deve remover CNAE com sucesso")
    void shouldRemoveCnaeSuccessfully() {
        // Given
        when(cnaeRepository.findById(cnaeId)).thenReturn(Optional.of(cnae));
        when(cnaeRepository.countActiveByCompanyId(companyId)).thenReturn(2L); // Mais de 1 CNAE ativo
        doNothing().when(cnaeRepository).deleteById(cnaeId);
        
        // When
        cnaeCommandService.delete(cnaeId);
        
        // Then
        verify(cnaeRepository).findById(cnaeId);
        verify(cnaeRepository).countActiveByCompanyId(companyId);
        verify(cnaeRepository).deleteById(cnaeId);
        verify(metricsPort).incrementCounter(eq("cnae_deleted_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando CNAE não encontrado para remoção")
    void shouldThrowExceptionWhenCnaeNotFoundForRemoval() {
        // Given
        when(cnaeRepository.findById(cnaeId)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cnaeCommandService.delete(cnaeId);
        });
        
        assertEquals("CNAE não encontrado: " + cnaeId, exception.getMessage());
        
        verify(cnaeRepository).findById(cnaeId);
        verify(cnaeRepository, never()).deleteById(any(UUID.class));
        verify(metricsPort).incrementCounter(eq("cnae_business_errors_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando tentar remover último CNAE ativo")
    void shouldThrowExceptionWhenRemovingLastActiveCnae() {
        // Given
        when(cnaeRepository.findById(cnaeId)).thenReturn(Optional.of(cnae));
        when(cnaeRepository.countActiveByCompanyId(companyId)).thenReturn(1L); // Apenas 1 CNAE ativo
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cnaeCommandService.delete(cnaeId);
        });
        
        assertEquals("Não é possível remover o último CNAE ativo da empresa", exception.getMessage());
        
        verify(cnaeRepository).findById(cnaeId);
        verify(cnaeRepository).countActiveByCompanyId(companyId);
        verify(cnaeRepository, never()).deleteById(any(UUID.class));
        verify(metricsPort).incrementCounter(eq("cnae_business_errors_total"), any());
    }
    
    @Test
    @DisplayName("Deve definir CNAE como principal com sucesso")
    void shouldSetCnaeAsPrincipalSuccessfully() {
        // Given
        when(cnaeRepository.findById(cnaeId)).thenReturn(Optional.of(cnae));
        when(cnaeRepository.findByCompanyId(companyId)).thenReturn(Arrays.asList());
        when(cnaeRepository.save(any(Cnae.class))).thenReturn(cnae);
        when(cnaeMapper.toViewDTO(any(Cnae.class))).thenReturn(cnaeView);
        
        // When
        CnaeViewDTO result = cnaeCommandService.setAsPrincipal(cnaeId);
        
        // Then
        assertNotNull(result);
        assertEquals(cnaeView.id(), result.id());
        
        verify(cnaeRepository).findById(cnaeId);
        verify(cnaeRepository).findByCompanyId(companyId);
        verify(cnaeRepository).save(any(Cnae.class));
        verify(cnaeMapper).toViewDTO(any(Cnae.class));
        verify(metricsPort).incrementCounter(eq("cnae_set_principal_total"), any());
    }
    
    @Test
    @DisplayName("Deve desativar CNAE principal existente ao definir novo como principal")
    void shouldDeactivateExistingPrincipalWhenSettingNewPrincipal() {
        // Given
        Cnae existingPrincipal = CnaeTestBuilder.builder()
            .withId(UUID.randomUUID())
            .withCompanyId(companyId)
            .withCode("1111111")
            .withDescription("CNAE principal existente")
            .asPrincipal()
            .buildDomain();
        
        when(cnaeRepository.findById(cnaeId)).thenReturn(Optional.of(cnae));
        when(cnaeRepository.findByCompanyId(companyId)).thenReturn(Arrays.asList(existingPrincipal));
        when(cnaeRepository.save(any(Cnae.class))).thenReturn(cnae);
        when(cnaeMapper.toViewDTO(any(Cnae.class))).thenReturn(cnaeView);
        
        // When
        CnaeViewDTO result = cnaeCommandService.setAsPrincipal(cnaeId);
        
        // Then
        assertNotNull(result);
        assertEquals(cnaeView.id(), result.id());
        
        verify(cnaeRepository).findById(cnaeId);
        verify(cnaeRepository).findByCompanyId(companyId);
        verify(cnaeRepository, times(2)).save(any(Cnae.class)); // Uma para desativar o existente, outra para ativar o novo
        verify(cnaeMapper).toViewDTO(any(Cnae.class));
        verify(metricsPort).incrementCounter(eq("cnae_set_principal_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando CNAE não encontrado para definir como principal")
    void shouldThrowExceptionWhenCnaeNotFoundForSetAsPrincipal() {
        // Given
        when(cnaeRepository.findById(cnaeId)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cnaeCommandService.setAsPrincipal(cnaeId);
        });
        
        assertEquals("CNAE não encontrado: " + cnaeId, exception.getMessage());
        
        verify(cnaeRepository).findById(cnaeId);
        verify(cnaeRepository, never()).save(any(Cnae.class));
        verify(metricsPort).incrementCounter(eq("cnae_business_errors_total"), any());
    }
    
    @Test
    @DisplayName("Deve desativar CNAE com sucesso")
    void shouldDeactivateCnaeSuccessfully() {
        // Given
        when(cnaeRepository.findById(cnaeId)).thenReturn(Optional.of(cnae));
        when(cnaeRepository.save(any(Cnae.class))).thenReturn(cnae);
        when(cnaeMapper.toViewDTO(any(Cnae.class))).thenReturn(cnaeView);
        
        // When
        CnaeViewDTO result = cnaeCommandService.deactivate(cnaeId);
        
        // Then
        assertNotNull(result);
        assertEquals(cnaeView.id(), result.id());
        
        verify(cnaeRepository).findById(cnaeId);
        verify(cnaeRepository).save(any(Cnae.class));
        verify(cnaeMapper).toViewDTO(any(Cnae.class));
        verify(metricsPort).incrementCounter(eq("cnae_deactivated_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando tentar desativar CNAE principal")
    void shouldThrowExceptionWhenDeactivatingPrincipalCnae() {
        // Given
        Cnae principalCnae = CnaeTestBuilder.builder()
            .withId(cnaeId)
            .withCompanyId(companyId)
            .withDescription("CNAE principal")
            .asPrincipal()
            .buildDomain();
        
        when(cnaeRepository.findById(cnaeId)).thenReturn(Optional.of(principalCnae));
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cnaeCommandService.deactivate(cnaeId);
        });
        
        assertEquals("Não é possível desativar o CNAE principal. Defina outro como principal primeiro.", exception.getMessage());
        
        verify(cnaeRepository).findById(cnaeId);
        verify(cnaeRepository, never()).save(any(Cnae.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando CNAE não encontrado para desativação")
    void shouldThrowExceptionWhenCnaeNotFoundForDeactivation() {
        // Given
        when(cnaeRepository.findById(cnaeId)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cnaeCommandService.deactivate(cnaeId);
        });
        
        assertEquals("CNAE não encontrado: " + cnaeId, exception.getMessage());
        
        verify(cnaeRepository).findById(cnaeId);
        verify(cnaeRepository, never()).save(any(Cnae.class));
        verify(metricsPort).incrementCounter(eq("cnae_business_errors_total"), any());
    }
}
