package com.keepguard.ms_company.application.service.cnae;

import com.keepguard.ms_company.application.dto.cnae.CnaeViewDTO;
import com.keepguard.ms_company.application.mapper.CnaeApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.CnaeRepositoryPort;
import com.keepguard.ms_company.domain.entity.Cnae;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CnaeQueryService
 * Inclui verificações de métricas usando o serviço genérico MetricsService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Cnae Query Service Tests")
class CnaeQueryServiceTest {
    
    @Mock
    private CnaeRepositoryPort cnaeRepository;
    
    @Mock
    private CnaeApplicationMapper cnaeMapper;
    
    @Mock
    private MetricsPort metricsPort;
    
    @InjectMocks
    private CnaeQueryService cnaeQueryService;
    
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
    @DisplayName("Deve buscar CNAE por ID com sucesso")
    void shouldGetCnaeByIdSuccessfully() {
        // Given
        when(cnaeRepository.findById(cnaeId)).thenReturn(Optional.of(cnae));
        when(cnaeMapper.toViewDTO(cnae)).thenReturn(cnaeView);
        
        // When
        CnaeViewDTO result = cnaeQueryService.getById(cnaeId);
        
        // Then
        assertNotNull(result);
        assertEquals(cnaeView.id(), result.id());
        assertEquals(cnaeView.code(), result.code());
        assertEquals(cnaeView.description(), result.description());
        
        verify(cnaeRepository).findById(cnaeId);
        verify(cnaeMapper).toViewDTO(cnae);
        verify(metricsPort).incrementCounter(eq("cnae_queried_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando CNAE não encontrado por ID")
    void shouldThrowExceptionWhenCnaeNotFoundById() {
        // Given
        when(cnaeRepository.findById(cnaeId)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cnaeQueryService.getById(cnaeId);
        });
        
        assertEquals("CNAE não encontrado: " + cnaeId, exception.getMessage());
        
        verify(cnaeRepository).findById(cnaeId);
        verify(cnaeMapper, never()).toView(any(Cnae.class));
        verify(metricsPort).incrementCounter(eq("cnae_query_errors_total"), any());
    }
    
    @Test
    @DisplayName("Deve listar CNAEs por empresa com sucesso")
    void shouldListCnaesByCompanyIdSuccessfully() {
        // Given
        Cnae cnae2 = CnaeTestBuilder.builder()
            .withId(UUID.randomUUID())
            .withCompanyId(companyId)
            .withCode("7654321")
            .withDescription("Outra atividade")
            .asPrincipal()
            .buildDomain();
        
        CnaeViewDTO cnaeView2 = CnaeTestBuilder.builder()
            .withId(cnae2.getId())
            .withCompanyId(companyId)
            .withCode("7654321")
            .withDescription("Outra atividade")
            .asPrincipal()
            .buildView();
        
        List<Cnae> cnaes = Arrays.asList(cnae, cnae2);
        List<CnaeViewDTO> expectedViews = Arrays.asList(cnaeView, cnaeView2);
        
        when(cnaeRepository.findByCompanyId(companyId)).thenReturn(cnaes);
        when(cnaeMapper.toViewDTO(cnae)).thenReturn(cnaeView);
        when(cnaeMapper.toViewDTO(cnae2)).thenReturn(cnaeView2);
        
        // When
        List<CnaeViewDTO> result = cnaeQueryService.listByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertNotNull(result.get(0));
        assertNotNull(result.get(1));
        assertEquals(expectedViews.get(0).id(), result.get(0).id());
        assertEquals(expectedViews.get(1).id(), result.get(1).id());
        
        verify(cnaeRepository).findByCompanyId(companyId);
        verify(cnaeMapper, times(2)).toViewDTO(any(Cnae.class));
        verify(metricsPort).incrementCounter(eq("cnae_queried_total"), any());
    }
    
    @Test
    @DisplayName("Deve retornar lista vazia quando empresa não tem CNAEs")
    void shouldReturnEmptyListWhenCompanyHasNoCnaes() {
        // Given
        when(cnaeRepository.findByCompanyId(companyId)).thenReturn(Arrays.asList());
        
        // When
        List<CnaeViewDTO> result = cnaeQueryService.listByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(cnaeRepository).findByCompanyId(companyId);
        verify(cnaeMapper, never()).toView(any(Cnae.class));
        verify(metricsPort).incrementCounter(eq("cnae_queried_total"), any());
    }
    
    @Test
    @DisplayName("Deve listar CNAEs ativos por empresa com sucesso")
    void shouldListActiveCnaesByCompanyIdSuccessfully() {
        // Given
        Cnae activeCnae = CnaeTestBuilder.builder()
            .withId(UUID.randomUUID())
            .withCompanyId(companyId)
            .withCode("1111111")
            .withDescription("CNAE ativo")
            .buildDomain();
        
        CnaeViewDTO activeCnaeViewDTO = CnaeTestBuilder.builder()
            .withId(activeCnae.getId())
            .withCompanyId(companyId)
            .withCode("1111111")
            .withDescription("CNAE ativo")
            .buildView();
        
        List<Cnae> activeCnaes = Arrays.asList(activeCnae);
        
        when(cnaeRepository.findActiveByCompanyId(companyId)).thenReturn(activeCnaes);
        when(cnaeMapper.toViewDTO(activeCnae)).thenReturn(activeCnaeViewDTO);
        
        // When
        List<CnaeViewDTO> result = cnaeQueryService.listActiveByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0));
        assertEquals(activeCnaeViewDTO.id(), result.get(0).id());
        assertTrue(result.get(0).active());
        
        verify(cnaeRepository).findActiveByCompanyId(companyId);
        verify(cnaeMapper).toViewDTO(activeCnae);
        verify(metricsPort).incrementCounter(eq("cnae_queried_total"), any());
    }
    
    @Test
    @DisplayName("Deve retornar lista vazia quando empresa não tem CNAEs ativos")
    void shouldReturnEmptyListWhenCompanyHasNoActiveCnaes() {
        // Given
        when(cnaeRepository.findActiveByCompanyId(companyId)).thenReturn(Arrays.asList());
        
        // When
        List<CnaeViewDTO> result = cnaeQueryService.listActiveByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(cnaeRepository).findActiveByCompanyId(companyId);
        verify(cnaeMapper, never()).toViewDTO(any(Cnae.class));
        verify(metricsPort).incrementCounter(eq("cnae_queried_total"), any());
    }
    
    @Test
    @DisplayName("Deve buscar CNAE principal por empresa com sucesso")
    void shouldGetPrincipalCnaeByCompanyIdSuccessfully() {
        // Given
        Cnae principalCnae = CnaeTestBuilder.builder()
            .withId(UUID.randomUUID())
            .withCompanyId(companyId)
            .withCode("9999999")
            .withDescription("CNAE principal")
            .asPrincipal()
            .buildDomain();
        
        CnaeViewDTO principalCnaeViewDTO = CnaeTestBuilder.builder()
            .withId(principalCnae.getId())
            .withCompanyId(companyId)
            .withCode("9999999")
            .withDescription("CNAE principal")
            .asPrincipal()
            .buildView();
        
        when(cnaeRepository.findPrincipalByCompanyId(companyId)).thenReturn(Optional.of(principalCnae));
        when(cnaeMapper.toViewDTO(principalCnae)).thenReturn(principalCnaeViewDTO);
        
        // When
        CnaeViewDTO result = cnaeQueryService.getPrincipalByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(principalCnaeViewDTO.id(), result.id());
        assertTrue(result.principal());
        
        verify(cnaeRepository).findPrincipalByCompanyId(companyId);
        verify(cnaeMapper).toViewDTO(principalCnae);
        verify(metricsPort).incrementCounter(eq("cnae_queried_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando empresa não tem CNAE principal")
    void shouldThrowExceptionWhenCompanyHasNoPrincipalCnae() {
        // Given
        when(cnaeRepository.findPrincipalByCompanyId(companyId)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cnaeQueryService.getPrincipalByCompanyId(companyId);
        });
        
        assertEquals("CNAE principal não encontrado para empresa: " + companyId, exception.getMessage());
        
        verify(cnaeRepository).findPrincipalByCompanyId(companyId);
        verify(cnaeMapper, never()).toView(any(Cnae.class));
        verify(metricsPort).incrementCounter(eq("cnae_query_errors_total"), any());
    }
    
    @Test
    @DisplayName("Deve verificar se CNAE existe por empresa e código")
    void shouldCheckIfCnaeExistsByCompanyIdAndCode() {
        // Given
        String code = CnaeTestBuilder.builder().buildDomain().getCode();
        when(cnaeRepository.existsByCompanyIdAndCode(companyId, code)).thenReturn(true);
        
        // When
        boolean result = cnaeQueryService.existsByCompanyIdAndCode(companyId, code);
        
        // Then
        assertTrue(result);
        
        verify(cnaeRepository).existsByCompanyIdAndCode(companyId, code);
        verify(metricsPort).incrementCounter(eq("cnae_queried_total"), any());
    }
    
    @Test
    @DisplayName("Deve retornar false quando CNAE não existe por empresa e código")
    void shouldReturnFalseWhenCnaeDoesNotExistByCompanyIdAndCode() {
        // Given
        String code = "9999999";
        when(cnaeRepository.existsByCompanyIdAndCode(companyId, code)).thenReturn(false);
        
        // When
        boolean result = cnaeQueryService.existsByCompanyIdAndCode(companyId, code);
        
        // Then
        assertFalse(result);
        
        verify(cnaeRepository).existsByCompanyIdAndCode(companyId, code);
        verify(metricsPort).incrementCounter(eq("cnae_queried_total"), any());
    }
    
    
    @Test
    @DisplayName("Deve contar CNAEs ativos por empresa")
    void shouldCountActiveCnaesByCompanyId() {
        // Given
        long expectedCount = 3L;
        when(cnaeRepository.countActiveByCompanyId(companyId)).thenReturn(expectedCount);
        
        // When
        long result = cnaeQueryService.countActiveByCompanyId(companyId);
        
        // Then
        assertEquals(expectedCount, result);
        
        verify(cnaeRepository).countActiveByCompanyId(companyId);
        verify(metricsPort).incrementCounter(eq("cnae_queried_total"), any());
    }
}
