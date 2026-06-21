package com.keepguard.ms_company.application.service.address;

import com.keepguard.ms_company.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_company.application.dto.address.AddressSearchCriteriaDTO;
import com.keepguard.ms_company.application.dto.address.AddressViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.mapper.AddressApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.AddressRepositoryPort;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.domain.entity.Address;
import com.keepguard.ms_company.application.port.out.cache.AddressCachePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para AddressQueryService
 * Inclui verificações de métricas usando o serviço genérico MetricsService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Address Query Service Tests")
class AddressQueryServiceTest {
    
    @Mock
    private AddressRepositoryPort addressRepository;
    
    @Mock
    private AddressApplicationMapper addressMapper;
    
    @Mock
    private AddressCachePort addressCachePort;
    
    @Mock
    private MetricsPort metricsPort;
    
    @InjectMocks
    private AddressQueryService addressQueryService;
    
    private Address address;
    private AddressViewDTO addressView;
    private UUID addressId;
    private UUID companyId;
    
    @BeforeEach
    void setUp() {
        addressId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        
        // Criar endereço de teste
        address = Address.create(
            "Rua das Flores",
            "123",
            "Sala 1",
            "Centro",
            "São Paulo",
            "SP",
            "Brasil",
            "01234567"
        );
        
        // Criar view de teste
        addressView = new AddressViewDTO(
            addressId,
            companyId,
            "Rua das Flores",
            "123",
            "Sala 1",
            "Centro",
            "São Paulo",
            "SP",
            "Brasil",
            "01234567",
            true
        );
    }
    
    @Test
    @DisplayName("Deve buscar endereço por ID com sucesso e registrar métricas")
    void shouldGetAddressByIdSuccessfully() {
        // Given
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(addressRepository.findCompanyIdByAddressId(any(UUID.class))).thenReturn(Optional.of(companyId));
        when(addressMapper.toViewDTO(address, companyId)).thenReturn(addressView);
        
        // When
        AddressViewDTO result = addressQueryService.getById(addressId);
        
        // Then
        assertNotNull(result);
        assertEquals(addressId, result.id());
        assertEquals(companyId, result.companyId());
        assertEquals("Rua das Flores", result.street());
        
        verify(addressRepository).findById(addressId);
        verify(addressRepository).findCompanyIdByAddressId(any(UUID.class));
        verify(addressMapper).toViewDTO(address, companyId);
        verify(metricsPort).incrementCounter(eq("address_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção e registrar métrica ao buscar endereço inexistente por ID")
    void shouldThrowExceptionAndRecordMetricWhenGettingNonExistentAddressById() {
        // Given
        when(addressRepository.findById(addressId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            addressQueryService.getById(addressId);
        });
        
        assertEquals("Endereço não encontrado: " + addressId, exception.getMessage());
        
        verify(addressRepository).findById(addressId);
        verify(addressMapper, never()).toViewDTO(any(), any());
        verify(metricsPort).incrementCounter(eq("address_not_found_total"), any());
    }
    
    @Test
    @DisplayName("Deve listar endereços por empresa com sucesso - cache miss")
    void shouldListAddressesByCompanySuccessfullyWithCacheMiss() {
        // Given
        List<Address> addresses = List.of(address);
        List<AddressViewDTO> expectedViews = List.of(addressView);
        
        when(addressCachePort.getAddressesByCompanyIdFromCache(companyId.toString())).thenReturn(null);
        when(addressRepository.findByCompanyId(companyId)).thenReturn(addresses);
        when(addressMapper.toViewDTO(address, companyId)).thenReturn(addressView);
        
        // When
        List<AddressViewDTO> result = addressQueryService.listByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(addressId, result.get(0).id());
        assertEquals(companyId, result.get(0).companyId());
        
        verify(addressCachePort).getAddressesByCompanyIdFromCache(companyId.toString());
        verify(addressRepository).findByCompanyId(companyId);
        verify(addressMapper).toViewDTO(address, companyId);
        verify(addressCachePort).cacheAddressesByCompanyId(companyId.toString(), expectedViews);
        verify(metricsPort).incrementCounter(eq("address_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve listar endereços por empresa com sucesso - cache hit")
    void shouldListAddressesByCompanySuccessfullyWithCacheHit() {
        // Given
        List<AddressViewDTO> cachedViews = List.of(addressView);
        
        when(addressCachePort.getAddressesByCompanyIdFromCache(companyId.toString())).thenReturn(cachedViews);
        
        // When
        List<AddressViewDTO> result = addressQueryService.listByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(addressId, result.get(0).id());
        assertEquals(companyId, result.get(0).companyId());
        
        verify(addressCachePort).getAddressesByCompanyIdFromCache(companyId.toString());
        verify(addressRepository, never()).findByCompanyId(any());
        verify(addressMapper, never()).toViewDTO(any(), any());
        verify(addressCachePort, never()).cacheAddressesByCompanyId(anyString(), any());
        verify(metricsPort).incrementCounter(eq("address_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve buscar endereço ativo por empresa com sucesso - cache miss")
    void shouldGetActiveAddressByCompanySuccessfullyWithCacheMiss() {
        // Given
        when(addressCachePort.getActiveAddressByCompanyIdFromCache(companyId.toString())).thenReturn(null);
        when(addressRepository.findActiveByCompanyId(companyId)).thenReturn(Optional.of(address));
        when(addressMapper.toViewDTO(address, companyId)).thenReturn(addressView);
        
        // When
        AddressViewDTO result = addressQueryService.getActiveByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(addressId, result.id());
        assertEquals(companyId, result.companyId());
        assertTrue(result.active());
        
        verify(addressCachePort).getActiveAddressByCompanyIdFromCache(companyId.toString());
        verify(addressRepository).findActiveByCompanyId(companyId);
        verify(addressMapper).toViewDTO(address, companyId);
        verify(addressCachePort).cacheActiveAddressByCompanyId(companyId.toString(), addressView);
        verify(metricsPort).incrementCounter(eq("address_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve buscar endereço ativo por empresa com sucesso - cache hit")
    void shouldGetActiveAddressByCompanySuccessfullyWithCacheHit() {
        // Given
        when(addressCachePort.getActiveAddressByCompanyIdFromCache(companyId.toString())).thenReturn(addressView);
        
        // When
        AddressViewDTO result = addressQueryService.getActiveByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(addressId, result.id());
        assertEquals(companyId, result.companyId());
        assertTrue(result.active());
        
        verify(addressCachePort).getActiveAddressByCompanyIdFromCache(companyId.toString());
        verify(addressRepository, never()).findActiveByCompanyId(any());
        verify(addressMapper, never()).toViewDTO(any(), any());
        verify(addressCachePort, never()).cacheActiveAddressByCompanyId(anyString(), any());
        verify(metricsPort).incrementCounter(eq("address_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção e registrar métrica ao buscar endereço ativo inexistente por empresa")
    void shouldThrowExceptionAndRecordMetricWhenGettingNonExistentActiveAddressByCompany() {
        // Given
        when(addressCachePort.getActiveAddressByCompanyIdFromCache(companyId.toString())).thenReturn(null);
        when(addressRepository.findActiveByCompanyId(companyId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            addressQueryService.getActiveByCompanyId(companyId);
        });
        
        assertEquals("Endereço ativo não encontrado para a empresa: " + companyId, exception.getMessage());
        
        verify(addressCachePort).getActiveAddressByCompanyIdFromCache(companyId.toString());
        verify(addressRepository).findActiveByCompanyId(companyId);
        verify(addressMapper, never()).toViewDTO(any(), any());
        verify(addressCachePort, never()).cacheActiveAddressByCompanyId(anyString(), any());
        verify(metricsPort).incrementCounter(eq("address_not_found_total"), any());
    }
    
    @Test
    @DisplayName("Deve listar todos os endereços com sucesso e registrar métricas")
    void shouldListAllAddressesSuccessfully() {
        // Given
        List<Address> addresses = List.of(address);
        
        when(addressRepository.findAll()).thenReturn(addresses);
        when(addressRepository.findCompanyIdByAddressId(any(UUID.class))).thenReturn(Optional.of(companyId));
        when(addressMapper.toViewDTO(address, companyId)).thenReturn(addressView);
        
        // When
        List<AddressViewDTO> result = addressQueryService.listAll();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(addressId, result.get(0).id());
        
        verify(addressRepository).findAll();
        verify(addressRepository).findCompanyIdByAddressId(any(UUID.class));
        verify(addressMapper).toViewDTO(address, companyId);
        verify(metricsPort).incrementCounter(eq("address_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve buscar endereços com critérios com sucesso e registrar métricas")
    void shouldSearchAddressesWithCriteriaSuccessfully() {
        // Given
        AddressSearchCriteriaDTO criteria = new AddressSearchCriteriaDTO(
            companyId, "São Paulo", "SP", "01234567", true, 0, 20, null, "ASC"
        );
        
        List<Address> addresses = List.of(address);
        PageResultDTO<Address> addressPage = new PageResultDTO<>(addresses, 1L, 0, 20);
        
        when(addressRepository.search(criteria)).thenReturn(addressPage);
        when(addressRepository.findCompanyIdByAddressId(any(UUID.class))).thenReturn(Optional.of(companyId));
        when(addressMapper.toViewDTO(address, companyId)).thenReturn(addressView);
        
        // When
        PageResultDTO<AddressViewDTO> result = addressQueryService.search(criteria);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());
        assertEquals(1L, result.total());
        assertEquals(0, result.page());
        assertEquals(20, result.size());
        assertEquals(addressId, result.items().get(0).id());
        
        verify(addressRepository).search(criteria);
        verify(addressRepository).findCompanyIdByAddressId(any(UUID.class));
        verify(addressMapper).toViewDTO(address, companyId);
        verify(metricsPort).incrementCounter(eq("address_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve retornar lista vazia quando não há endereços")
    void shouldReturnEmptyListWhenNoAddresses() {
        // Given
        when(addressCachePort.getAddressesByCompanyIdFromCache(companyId.toString())).thenReturn(null);
        when(addressRepository.findByCompanyId(companyId)).thenReturn(List.of());
        
        // When
        List<AddressViewDTO> result = addressQueryService.listByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(addressCachePort).getAddressesByCompanyIdFromCache(companyId.toString());
        verify(addressRepository).findByCompanyId(companyId);
        verify(addressCachePort).cacheAddressesByCompanyId(companyId.toString(), result);
        verify(addressMapper, never()).toViewDTO(any(), any());
        verify(metricsPort).incrementCounter(eq("address_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve registrar métrica de erro do sistema quando ocorre exceção")
    void shouldRecordSystemErrorMetricWhenExceptionOccurs() {
        // Given
        when(addressRepository.findById(addressId)).thenThrow(new RuntimeException("Database error"));
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            addressQueryService.getById(addressId);
        });
        
        assertEquals("Falha ao buscar endereço", exception.getMessage());
        
        verify(addressRepository).findById(addressId);
        verify(addressMapper, never()).toViewDTO(any(), any());
        verify(metricsPort).incrementCounter(eq("address_system_errors_total"), any());
    }
}
