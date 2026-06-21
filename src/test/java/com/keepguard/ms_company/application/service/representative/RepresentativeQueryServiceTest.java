package com.keepguard.ms_company.application.service.representative;

import com.keepguard.ms_company.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_company.application.dto.representative.RepresentativeViewDTO;
import com.keepguard.ms_company.application.mapper.RepresentativeApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.RepresentativeRepositoryPort;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.domain.entity.Representative;
import com.keepguard.ms_company.application.port.out.cache.RepresentativeCachePort;
import com.keepguard.ms_company.test.builder.RepresentativeTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RepresentativeQueryService
 * Testa operações de leitura (get, list, search)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Representative Query Service Tests")
class RepresentativeQueryServiceTest {
    
    @Mock
    private RepresentativeRepositoryPort representativeRepository;
    
    @Mock
    private RepresentativeApplicationMapper representativeMapper;
    
    @Mock
    private MetricsPort metricsPort;
    
    @Mock
    private RepresentativeCachePort representativeCachePort;
    
    @InjectMocks
    private RepresentativeQueryService representativeQueryService;
    
    private UUID representativeId;
    private UUID companyId;
    private Representative representative;
    private RepresentativeViewDTO representativeView;
    
    @BeforeEach
    void setUp() {
        representativeId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        representative = RepresentativeTestBuilder.createDefaultRepresentative();
        representativeView = RepresentativeTestBuilder.createDefaultRepresentativeViewDTO();
        
        // Configurar mocks comuns
        lenient().when(representativeCachePort.getRepresentativesByCompanyIdFromCache(anyString())).thenReturn(null);
        lenient().when(representativeCachePort.getActiveRepresentativeByCompanyIdFromCache(anyString())).thenReturn(null);
    }
    
    @Test
    @DisplayName("Deve buscar representante por ID com sucesso")
    void shouldFindRepresentativeByIdSuccessfully() {
        // Given
        when(representativeRepository.findById(representativeId)).thenReturn(Optional.of(representative));
        when(representativeMapper.toViewDTO(representative)).thenReturn(representativeView);
        
        // When
        RepresentativeViewDTO result = representativeQueryService.findById(representativeId);
        
        // Then
        assertNotNull(result);
        assertEquals(representativeView.id(), result.id());
        assertEquals(representativeView.name(), result.name());
        assertEquals(representativeView.cpf(), result.cpf());
        
        verify(representativeRepository).findById(representativeId);
        verify(representativeMapper).toViewDTO(representative);
        verify(metricsPort).incrementCounter(eq("representative.found.by_id"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando representante não encontrado por ID")
    void shouldThrowExceptionWhenRepresentativeNotFoundById() {
        // Given
        when(representativeRepository.findById(representativeId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> representativeQueryService.findById(representativeId)
        );
        
        assertEquals("Representante não encontrado", exception.getMessage());
        
        verify(representativeRepository).findById(representativeId);
        verify(representativeMapper, never()).toViewDTO(any(Representative.class));
    }
    
    @Test
    @DisplayName("Deve listar todos os representantes")
    void shouldFindAllRepresentatives() {
        // Given
        List<Representative> representatives = List.of(representative);
        
        when(representativeRepository.findAll()).thenReturn(representatives);
        when(representativeMapper.toViewDTO(representative)).thenReturn(representativeView);
        
        // When
        List<RepresentativeViewDTO> result = representativeQueryService.findAll();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(representativeView.id(), result.get(0).id());
        assertEquals(representativeView.name(), result.get(0).name());
        
        verify(representativeRepository).findAll();
        verify(representativeMapper).toViewDTO(representative);
        verify(metricsPort).incrementCounter(eq("representative.found.all"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve listar representantes por empresa com sucesso (cache miss)")
    void shouldFindRepresentativesByCompanyIdWithCacheMiss() {
        // Given
        List<Representative> representatives = List.of(representative);
        
        when(representativeCachePort.getRepresentativesByCompanyIdFromCache(companyId.toString())).thenReturn(null);
        when(representativeRepository.findByCompanyId(companyId)).thenReturn(representatives);
        when(representativeMapper.toViewDTO(representative)).thenReturn(representativeView);
        
        // When
        List<RepresentativeViewDTO> result = representativeQueryService.findByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(representativeView.id(), result.get(0).id());
        assertEquals(representativeView.name(), result.get(0).name());
        
        verify(representativeCachePort).getRepresentativesByCompanyIdFromCache(companyId.toString());
        verify(representativeRepository).findByCompanyId(companyId);
        verify(representativeMapper).toViewDTO(representative);
        verify(representativeCachePort).cacheRepresentativesByCompanyId(companyId.toString(), result);
        verify(metricsPort).incrementCounter(eq("representative.found.by_company"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve listar representantes por empresa com sucesso (cache hit)")
    void shouldFindRepresentativesByCompanyIdWithCacheHit() {
        // Given
        List<RepresentativeViewDTO> cachedRepresentatives = List.of(representativeView);
        
        when(representativeCachePort.getRepresentativesByCompanyIdFromCache(companyId.toString())).thenReturn(cachedRepresentatives);
        
        // When
        List<RepresentativeViewDTO> result = representativeQueryService.findByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(representativeView.id(), result.get(0).id());
        assertEquals(representativeView.name(), result.get(0).name());
        assertEquals(cachedRepresentatives, result);
        
        verify(representativeCachePort).getRepresentativesByCompanyIdFromCache(companyId.toString());
        verify(representativeRepository, never()).findByCompanyId(any());
        verify(representativeMapper, never()).toViewDTO(any(Representative.class));
        verify(representativeCachePort, never()).cacheRepresentativesByCompanyId(anyString(), any());
        verify(metricsPort).incrementCounter(eq("representative.found.by_company"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve buscar representante ativo por empresa com sucesso (cache miss)")
    void shouldFindActiveRepresentativeByCompanyIdSuccessfullyWithCacheMiss() {
        // Given
        when(representativeCachePort.getActiveRepresentativeByCompanyIdFromCache(companyId.toString())).thenReturn(null);
        when(representativeRepository.findActiveByCompanyId(companyId)).thenReturn(Optional.of(representative));
        when(representativeMapper.toViewDTO(representative)).thenReturn(representativeView);
        
        // When
        RepresentativeViewDTO result = representativeQueryService.findActiveByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(representativeView.id(), result.id());
        assertEquals(representativeView.name(), result.name());
        assertTrue(result.active());
        
        verify(representativeCachePort).getActiveRepresentativeByCompanyIdFromCache(companyId.toString());
        verify(representativeRepository).findActiveByCompanyId(companyId);
        verify(representativeMapper).toViewDTO(representative);
        verify(representativeCachePort).cacheActiveRepresentativeByCompanyId(companyId.toString(), result);
        verify(metricsPort).incrementCounter(eq("representative.found.active_by_company"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve buscar representante ativo por empresa com sucesso (cache hit)")
    void shouldFindActiveRepresentativeByCompanyIdSuccessfullyWithCacheHit() {
        // Given
        when(representativeCachePort.getActiveRepresentativeByCompanyIdFromCache(companyId.toString())).thenReturn(representativeView);
        
        // When
        RepresentativeViewDTO result = representativeQueryService.findActiveByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(representativeView.id(), result.id());
        assertEquals(representativeView.name(), result.name());
        assertTrue(result.active());
        assertEquals(representativeView, result);
        
        verify(representativeCachePort).getActiveRepresentativeByCompanyIdFromCache(companyId.toString());
        verify(representativeRepository, never()).findActiveByCompanyId(any());
        verify(representativeMapper, never()).toViewDTO(any(Representative.class));
        verify(representativeCachePort, never()).cacheActiveRepresentativeByCompanyId(anyString(), any());
        verify(metricsPort).incrementCounter(eq("representative.found.active_by_company"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando representante ativo não encontrado por empresa")
    void shouldThrowExceptionWhenActiveRepresentativeNotFoundByCompanyId() {
        // Given
        when(representativeCachePort.getActiveRepresentativeByCompanyIdFromCache(companyId.toString())).thenReturn(null);
        when(representativeRepository.findActiveByCompanyId(companyId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> representativeQueryService.findActiveByCompanyId(companyId)
        );
        
        assertEquals("Representante ativo não encontrado para esta empresa", exception.getMessage());
        
        verify(representativeCachePort).getActiveRepresentativeByCompanyIdFromCache(companyId.toString());
        verify(representativeRepository).findActiveByCompanyId(companyId);
        verify(representativeMapper, never()).toViewDTO(any(Representative.class));
    }
    
    @Test
    @DisplayName("Deve listar todos os representantes ativos")
    void shouldFindAllActiveRepresentatives() {
        // Given
        List<Representative> representatives = List.of(representative);
        
        when(representativeRepository.findAllActive()).thenReturn(representatives);
        when(representativeMapper.toViewDTO(representative)).thenReturn(representativeView);
        
        // When
        List<RepresentativeViewDTO> result = representativeQueryService.findAllActive();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(representativeView.id(), result.get(0).id());
        assertEquals(representativeView.name(), result.get(0).name());
        assertTrue(result.get(0).active());
        
        verify(representativeRepository).findAllActive();
        verify(representativeMapper).toViewDTO(representative);
        verify(metricsPort).incrementCounter(eq("representative.found.all_active"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve buscar representante por CPF com sucesso")
    void shouldFindRepresentativeByCpfSuccessfully() {
        // Given
        String cpf = "11144477735";
        
        when(representativeRepository.findByCpf(cpf)).thenReturn(Optional.of(representative));
        when(representativeMapper.toViewDTO(representative)).thenReturn(representativeView);
        
        // When
        RepresentativeViewDTO result = representativeQueryService.findByCpf(cpf);
        
        // Then
        assertNotNull(result);
        assertEquals(representativeView.id(), result.id());
        assertEquals(representativeView.name(), result.name());
        assertEquals(cpf, result.cpf());
        
        verify(representativeRepository).findByCpf(cpf);
        verify(representativeMapper).toViewDTO(representative);
        verify(metricsPort).incrementCounter(eq("representative.found.by_cpf"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando representante não encontrado por CPF")
    void shouldThrowExceptionWhenRepresentativeNotFoundByCpf() {
        // Given
        String cpf = "11144477735";
        
        when(representativeRepository.findByCpf(cpf)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> representativeQueryService.findByCpf(cpf)
        );
        
        assertEquals("Representante não encontrado com este CPF", exception.getMessage());
        
        verify(representativeRepository).findByCpf(cpf);
        verify(representativeMapper, never()).toViewDTO(any(Representative.class));
    }
    
    @Test
    @DisplayName("Deve buscar representante por email com sucesso")
    void shouldFindRepresentativeByEmailSuccessfully() {
        // Given
        String email = "joao.silva@empresa.com";
        
        when(representativeRepository.findByEmail(email)).thenReturn(Optional.of(representative));
        when(representativeMapper.toViewDTO(representative)).thenReturn(representativeView);
        
        // When
        RepresentativeViewDTO result = representativeQueryService.findByEmail(email);
        
        // Then
        assertNotNull(result);
        assertEquals(representativeView.id(), result.id());
        assertEquals(representativeView.name(), result.name());
        assertEquals(email, result.email());
        
        verify(representativeRepository).findByEmail(email);
        verify(representativeMapper).toViewDTO(representative);
        verify(metricsPort).incrementCounter(eq("representative.found.by_email"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando representante não encontrado por email")
    void shouldThrowExceptionWhenRepresentativeNotFoundByEmail() {
        // Given
        String email = "joao.silva@empresa.com";
        
        when(representativeRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> representativeQueryService.findByEmail(email)
        );
        
        assertEquals("Representante não encontrado com este email", exception.getMessage());
        
        verify(representativeRepository).findByEmail(email);
        verify(representativeMapper, never()).toViewDTO(any(Representative.class));
    }
    
    @Test
    @DisplayName("Deve buscar representantes por nome")
    void shouldFindRepresentativesByNameContaining() {
        // Given
        String name = "João";
        List<Representative> representatives = List.of(representative);
        
        when(representativeRepository.findByNameContainingIgnoreCase(name)).thenReturn(representatives);
        when(representativeMapper.toViewDTO(representative)).thenReturn(representativeView);
        
        // When
        List<RepresentativeViewDTO> result = representativeQueryService.findByNameContaining(name);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(representativeView.id(), result.get(0).id());
        assertEquals(representativeView.name(), result.get(0).name());
        
        verify(representativeRepository).findByNameContainingIgnoreCase(name);
        verify(representativeMapper).toViewDTO(representative);
        verify(metricsPort).incrementCounter(eq("representative.found.by_name"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve buscar representantes por cargo")
    void shouldFindRepresentativesByRoleContaining() {
        // Given
        String role = "Diretor";
        List<Representative> representatives = List.of(representative);
        
        when(representativeRepository.findByRoleContainingIgnoreCase(role)).thenReturn(representatives);
        when(representativeMapper.toViewDTO(representative)).thenReturn(representativeView);
        
        // When
        List<RepresentativeViewDTO> result = representativeQueryService.findByRoleContaining(role);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(representativeView.id(), result.get(0).id());
        assertEquals(representativeView.name(), result.get(0).name());
        assertEquals(role, result.get(0).role());
        
        verify(representativeRepository).findByRoleContainingIgnoreCase(role);
        verify(representativeMapper).toViewDTO(representative);
        verify(metricsPort).incrementCounter(eq("representative.found.by_role"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve verificar se representante existe")
    void shouldCheckIfRepresentativeExists() {
        // Given
        when(representativeRepository.existsById(representativeId)).thenReturn(true);
        
        // When
        boolean exists = representativeQueryService.existsById(representativeId);
        
        // Then
        assertTrue(exists);
        
        verify(representativeRepository).existsById(representativeId);
    }
    
    @Test
    @DisplayName("Deve retornar false quando representante não existe")
    void shouldReturnFalseWhenRepresentativeDoesNotExist() {
        // Given
        when(representativeRepository.existsById(representativeId)).thenReturn(false);
        
        // When
        boolean exists = representativeQueryService.existsById(representativeId);
        
        // Then
        assertFalse(exists);
        
        verify(representativeRepository).existsById(representativeId);
    }
    
    @Test
    @DisplayName("Deve contar representantes ativos por empresa")
    void shouldCountActiveRepresentativesByCompanyId() {
        // Given
        long count = 2L;
        
        when(representativeRepository.countActiveByCompanyId(companyId)).thenReturn(count);
        
        // When
        long result = representativeQueryService.countActiveByCompanyId(companyId);
        
        // Then
        assertEquals(count, result);
        
        verify(representativeRepository).countActiveByCompanyId(companyId);
    }
    
    @Test
    @DisplayName("Deve buscar representante com dados de Maria Silva")
    void shouldFindRepresentativeWithMariaSilvaData() {
        // Given
        Representative mariaRepresentative = RepresentativeTestBuilder.createMariaSilvaRepresentative();
        RepresentativeViewDTO mariaView = RepresentativeTestBuilder.createMariaSilvaRepresentativeViewDTO();
        
        when(representativeRepository.findById(representativeId)).thenReturn(Optional.of(mariaRepresentative));
        when(representativeMapper.toViewDTO(mariaRepresentative)).thenReturn(mariaView);
        
        // When
        RepresentativeViewDTO result = representativeQueryService.findById(representativeId);
        
        // Then
        assertEquals("Maria Silva", result.name());
        assertEquals("98765432100", result.cpf());
        assertEquals("maria.silva@empresa.com", result.email());
        assertEquals("Gerente", result.role());
        
        verify(metricsPort).incrementCounter(eq("representative.found.by_id"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve buscar representante com dados de Pedro Santos")
    void shouldFindRepresentativeWithPedroSantosData() {
        // Given
        Representative pedroRepresentative = RepresentativeTestBuilder.createPedroSantosRepresentative();
        RepresentativeViewDTO pedroView = RepresentativeTestBuilder.createPedroSantosRepresentativeViewDTO();
        
        when(representativeRepository.findByCpf("12345678909")).thenReturn(Optional.of(pedroRepresentative));
        when(representativeMapper.toViewDTO(pedroRepresentative)).thenReturn(pedroView);
        
        // When
        RepresentativeViewDTO result = representativeQueryService.findByCpf("12345678909");
        
        // Then
        assertEquals("Pedro Santos", result.name());
        assertEquals("12345678909", result.cpf());
        assertEquals("pedro.santos@empresa.com", result.email());
        assertEquals("Supervisor", result.role());
        
        verify(metricsPort).incrementCounter(eq("representative.found.by_cpf"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve buscar representante com dados de Ana Costa")
    void shouldFindRepresentativeWithAnaCostaData() {
        // Given
        Representative anaRepresentative = RepresentativeTestBuilder.createAnaCostaRepresentative();
        RepresentativeViewDTO anaView = RepresentativeTestBuilder.createAnaCostaRepresentativeViewDTO();
        
        when(representativeRepository.findByEmail("ana.costa@empresa.com")).thenReturn(Optional.of(anaRepresentative));
        when(representativeMapper.toViewDTO(anaRepresentative)).thenReturn(anaView);
        
        // When
        RepresentativeViewDTO result = representativeQueryService.findByEmail("ana.costa@empresa.com");
        
        // Then
        assertEquals("Ana Costa", result.name());
        assertEquals("55566677720", result.cpf());
        assertEquals("ana.costa@empresa.com", result.email());
        assertEquals("Coordenadora", result.role());
        
        verify(metricsPort).incrementCounter(eq("representative.found.by_email"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve buscar representante com dados de Carlos Oliveira")
    void shouldFindRepresentativeWithCarlosOliveiraData() {
        // Given
        Representative carlosRepresentative = RepresentativeTestBuilder.createCarlosOliveiraRepresentative();
        RepresentativeViewDTO carlosView = RepresentativeTestBuilder.createCarlosOliveiraRepresentativeViewDTO();
        
        when(representativeRepository.findActiveByCompanyId(companyId)).thenReturn(Optional.of(carlosRepresentative));
        when(representativeMapper.toViewDTO(carlosRepresentative)).thenReturn(carlosView);
        
        // When
        RepresentativeViewDTO result = representativeQueryService.findActiveByCompanyId(companyId);
        
        // Then
        assertEquals("Carlos Oliveira", result.name());
        assertEquals("99988877714", result.cpf());
        assertEquals("carlos.oliveira@empresa.com", result.email());
        assertEquals("Presidente", result.role());
        
        verify(metricsPort).incrementCounter(eq("representative.found.active_by_company"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve retornar lista vazia quando não há representantes")
    void shouldReturnEmptyListWhenNoRepresentativesFound() {
        // Given
        when(representativeRepository.findAll()).thenReturn(List.of());
        
        // When
        List<RepresentativeViewDTO> result = representativeQueryService.findAll();
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(representativeRepository).findAll();
        verify(representativeMapper, never()).toViewDTO(any(Representative.class));
        verify(metricsPort).incrementCounter(eq("representative.found.all"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve retornar lista vazia quando não há representantes ativos")
    void shouldReturnEmptyListWhenNoActiveRepresentativesFound() {
        // Given
        when(representativeRepository.findAllActive()).thenReturn(List.of());
        
        // When
        List<RepresentativeViewDTO> result = representativeQueryService.findAllActive();
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(representativeRepository).findAllActive();
        verify(representativeMapper, never()).toViewDTO(any(Representative.class));
        verify(metricsPort).incrementCounter(eq("representative.found.all_active"), any(Map.class));
    }
}
