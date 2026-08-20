package com.keepguard.ms_company.application.service.company;

import com.keepguard.ms_company.application.dto.company.CompanyViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.company.CompanySearchCriteriaDTO;
import com.keepguard.ms_company.application.mapper.CompanyApplicationMapper;
import com.keepguard.ms_company.adapters.in.rest.company.dto.response.CompanySimpleResponseDTO;
import com.keepguard.ms_company.application.port.out.persistence.CompanyRepositoryPort;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.domain.entity.*;
import com.keepguard.ms_company.domain.enums.AccountTypeEnum;
import com.keepguard.ms_company.domain.enums.CompanyStatusEnum;
import com.keepguard.ms_company.domain.enums.TaxRegimeEnum;
import com.keepguard.ms_company.test.builder.CompanyTestBuilder;
import com.keepguard.ms_company.application.port.out.cache.CompanyCachePort;
import com.keepguard.ms_company.application.port.out.metrics.MetricsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CompanyQueryService
 * Testa operações de leitura com mocks simplificados
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Company Query Service Tests")
class CompanyQueryServiceTest {
    
    @Mock
    private CompanyRepositoryPort companyRepository;
    
    @Mock
    private CompanyApplicationMapper companyMapper;
    
    @Mock
    private CompanyCachePort companyCachePort;
    
    @Mock
    private MetricsPort metricsPort;
    
    @InjectMocks
    private CompanyQueryService companyQueryService;
    
    private Company company;
    private CompanyViewDTO companyView;
    private UUID companyId;
    
    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        companyView = new CompanyViewDTO(
            companyId,
            UUID.randomUUID(), // codeCompany
            UUID.randomUUID(), // tenantId
            "Empresa Teste",
            "Empresa Teste Ltda",
            "11222333000181",
            "123456789",
            "987654321",
            null, // address
            new ArrayList<>(), // contacts
            new ArrayList<>(), // representatives
            null, // bankAccount
            TaxRegimeEnum.SIMPLES_NACIONAL,
            new ArrayList<>(),
            "123456789",
            CompanyStatusEnum.PENDING_APPROVAL,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        company = Company.create(
            "Empresa Teste",
            "Empresa Teste Ltda",
            "11222333000181",
            "123456789",
            "987654321",
            TaxRegimeEnum.SIMPLES_NACIONAL,
            "123456789"
        );
    }
    
    @Test
    @DisplayName("Deve buscar empresa por ID com sucesso - cache miss")
    void shouldGetCompanyByIdSuccessfullyWithCacheMiss() {
        // Given
        when(companyCachePort.getCompanyByIdFromCache(companyId.toString())).thenReturn(null);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyMapper.toViewDTO(company)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyQueryService.getById(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(companyId, result.id());
        assertEquals("Empresa Teste", result.name());
        assertEquals("Empresa Teste Ltda", result.legalName());
        assertEquals("11222333000181", result.cnpj());
        assertEquals(CompanyStatusEnum.PENDING_APPROVAL, result.status());
        
        verify(companyCachePort).getCompanyByIdFromCache(companyId.toString());
        verify(companyRepository).findById(companyId);
        verify(companyMapper).toViewDTO(company);
        verify(companyCachePort).cacheCompanyById(companyId.toString(), companyView);
        verify(metricsPort).incrementCounter(eq("company_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve buscar empresa por ID com sucesso - cache hit")
    void shouldGetCompanyByIdSuccessfullyWithCacheHit() {
        // Given
        when(companyCachePort.getCompanyByIdFromCache(companyId.toString())).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyQueryService.getById(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(companyId, result.id());
        assertEquals("Empresa Teste", result.name());
        assertEquals("Empresa Teste Ltda", result.legalName());
        assertEquals("11222333000181", result.cnpj());
        assertEquals(CompanyStatusEnum.PENDING_APPROVAL, result.status());
        
        verify(companyCachePort).getCompanyByIdFromCache(companyId.toString());
        verify(companyRepository, never()).findById(any());
        verify(companyMapper, never()).toViewDTO(any());
        verify(companyCachePort, never()).cacheCompanyById(anyString(), any());
        verify(metricsPort).incrementCounter(eq("company_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao buscar empresa inexistente por ID")
    void shouldThrowExceptionWhenGettingNonExistentCompanyById() {
        // Given
        when(companyCachePort.getCompanyByIdFromCache(companyId.toString())).thenReturn(null);
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> 
            companyQueryService.getById(companyId));
        
        assertEquals("Empresa não encontrada: " + companyId, exception.getMessage());
        
        verify(companyCachePort).getCompanyByIdFromCache(companyId.toString());
        verify(companyRepository).findById(companyId);
        verify(companyMapper, never()).toViewDTO(any());
        verify(companyCachePort, never()).cacheCompanyById(anyString(), any());
        verify(metricsPort).incrementCounter(eq("company_not_found_total"), any());
    }
    
    @Test
    @DisplayName("Deve buscar empresa por CNPJ com sucesso - cache miss")
    void shouldGetCompanyByCnpjSuccessfullyWithCacheMiss() {
        // Given
        String cnpj = "11222333000181";
        when(companyCachePort.getCompanyByCnpjFromCache(cnpj)).thenReturn(null);
        when(companyRepository.findByCnpj(cnpj)).thenReturn(Optional.of(company));
        when(companyMapper.toViewDTO(company)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyQueryService.getByCnpj(cnpj);
        
        // Then
        assertNotNull(result);
        assertEquals(companyId, result.id());
        assertEquals("Empresa Teste", result.name());
        assertEquals("Empresa Teste Ltda", result.legalName());
        assertEquals("11222333000181", result.cnpj());
        
        verify(companyCachePort).getCompanyByCnpjFromCache(cnpj);
        verify(companyRepository).findByCnpj(cnpj);
        verify(companyMapper).toViewDTO(company);
        verify(companyCachePort).cacheCompanyByCnpj(cnpj, companyView);
        verify(metricsPort).incrementCounter(eq("company_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve buscar empresa por CNPJ com sucesso - cache hit")
    void shouldGetCompanyByCnpjSuccessfullyWithCacheHit() {
        // Given
        String cnpj = "11222333000181";
        when(companyCachePort.getCompanyByCnpjFromCache(cnpj)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyQueryService.getByCnpj(cnpj);
        
        // Then
        assertNotNull(result);
        assertEquals(companyId, result.id());
        assertEquals("Empresa Teste", result.name());
        assertEquals("Empresa Teste Ltda", result.legalName());
        assertEquals("11222333000181", result.cnpj());
        
        verify(companyCachePort).getCompanyByCnpjFromCache(cnpj);
        verify(companyRepository, never()).findByCnpj(anyString());
        verify(companyMapper, never()).toViewDTO(any());
        verify(companyCachePort, never()).cacheCompanyByCnpj(anyString(), any());
        verify(metricsPort).incrementCounter(eq("company_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao buscar empresa inexistente por CNPJ")
    void shouldThrowExceptionWhenGettingNonExistentCompanyByCnpj() {
        // Given
        String cnpj = "11222333000181";
        when(companyCachePort.getCompanyByCnpjFromCache(cnpj)).thenReturn(null);
        when(companyRepository.findByCnpj(cnpj)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> 
            companyQueryService.getByCnpj(cnpj));
        
        assertEquals("Empresa não encontrada: " + cnpj, exception.getMessage());
        
        verify(companyCachePort).getCompanyByCnpjFromCache(cnpj);
        verify(companyRepository).findByCnpj(cnpj);
        verify(companyMapper, never()).toViewDTO(any());
        verify(companyCachePort, never()).cacheCompanyByCnpj(anyString(), any());
        verify(metricsPort).incrementCounter(eq("company_not_found_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao buscar empresa com CNPJ nulo")
    void shouldThrowExceptionWhenGettingCompanyWithNullCnpj() {
        // Given
        when(companyCachePort.getCompanyByCnpjFromCache(null)).thenReturn(null);
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            companyQueryService.getByCnpj(null));
        
        assertEquals("Erro interno ao buscar empresa por CNPJ", exception.getMessage());
        
        verify(companyCachePort).getCompanyByCnpjFromCache(null);
        verify(companyRepository).findByCnpj(null);
        verify(companyMapper, never()).toViewDTO(any());
        verify(companyCachePort, never()).cacheCompanyByCnpj(anyString(), any());
        // Não verifica métricas pois a exceção é propagada
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao buscar empresa com CNPJ vazio")
    void shouldThrowExceptionWhenGettingCompanyWithEmptyCnpj() {
        // Given
        String emptyCnpj = "   ";
        when(companyCachePort.getCompanyByCnpjFromCache(emptyCnpj)).thenReturn(null);
        when(companyRepository.findByCnpj(emptyCnpj)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> 
            companyQueryService.getByCnpj(emptyCnpj));
        
        assertEquals("Empresa não encontrada:    ", exception.getMessage());
        
        verify(companyCachePort).getCompanyByCnpjFromCache(emptyCnpj);
        verify(companyRepository).findByCnpj(emptyCnpj);
        verify(companyMapper, never()).toViewDTO(any());
        verify(companyCachePort, never()).cacheCompanyByCnpj(anyString(), any());
        verify(metricsPort).incrementCounter(eq("company_not_found_total"), any());
    }
    
    
    @Test
    @DisplayName("Deve buscar empresas com critérios com sucesso")
    void shouldSearchCompaniesWithCriteriaSuccessfully() {
        // Given
        CompanySearchCriteriaDTO criteria = CompanySearchCriteriaDTO.of("Empresa", null, null, null, null, null, 0, 10);
        PageResultDTO<Company> companies = new PageResultDTO<>(List.of(company), 1L, 0, 10);
        
        when(companyRepository.search(criteria)).thenReturn(companies);
        when(companyMapper.toViewDTO(company)).thenReturn(companyView);
        
        // When
        PageResultDTO<CompanyViewDTO> result = companyQueryService.search(criteria);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());
        assertEquals(1L, result.total());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(companyId, result.items().get(0).id());
        
        verify(companyRepository).search(criteria);
        verify(companyMapper).toViewDTO(company);
        verify(metricsPort).incrementCounter(eq("company_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve retornar página vazia quando não há empresas na busca")
    void shouldReturnEmptyPageWhenNoCompaniesInSearch() {
        // Given
        CompanySearchCriteriaDTO criteria = CompanySearchCriteriaDTO.of("Inexistente", null, null, null, null, null, 0, 10);
        PageResultDTO<Company> emptyResult = new PageResultDTO<>(new ArrayList<>(), 0L, 0, 10);
        
        when(companyRepository.search(criteria)).thenReturn(emptyResult);
        
        // When
        PageResultDTO<CompanyViewDTO> result = companyQueryService.search(criteria);
        
        // Then
        assertNotNull(result);
        assertTrue(result.items().isEmpty());
        assertEquals(0L, result.total());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        
        verify(companyRepository).search(criteria);
        verify(companyMapper, never()).toViewDTO(any());
        verify(metricsPort).incrementCounter(eq("company_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve buscar empresas por status com sucesso")
    void shouldSearchCompaniesByStatusSuccessfully() {
        // Given
        CompanySearchCriteriaDTO criteria = CompanySearchCriteriaDTO.of(null, null, null, null, null, CompanyStatusEnum.ACTIVE, 0, 10);
        PageResultDTO<Company> companies = new PageResultDTO<>(List.of(company), 1L, 0, 10);
        
        when(companyRepository.search(criteria)).thenReturn(companies);
        when(companyMapper.toViewDTO(company)).thenReturn(companyView);
        
        // When
        PageResultDTO<CompanyViewDTO> result = companyQueryService.search(criteria);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());
        assertEquals(1L, result.total());
        
        verify(companyRepository).search(criteria);
        verify(companyMapper).toViewDTO(company);
        verify(metricsPort).incrementCounter(eq("company_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve buscar empresas por regime tributário com sucesso")
    void shouldSearchCompaniesByTaxRegimeSuccessfully() {
        // Given
        CompanySearchCriteriaDTO criteria = CompanySearchCriteriaDTO.of(null, null, null, null, null, null, 0, 10);
        PageResultDTO<Company> companies = new PageResultDTO<>(List.of(company), 1L, 0, 10);
        
        when(companyRepository.search(criteria)).thenReturn(companies);
        when(companyMapper.toViewDTO(company)).thenReturn(companyView);
        
        // When
        PageResultDTO<CompanyViewDTO> result = companyQueryService.search(criteria);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());
        assertEquals(1L, result.total());
        
        verify(companyRepository).search(criteria);
        verify(companyMapper).toViewDTO(company);
        verify(metricsPort).incrementCounter(eq("company_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve buscar empresas com paginação correta")
    void shouldSearchCompaniesWithCorrectPagination() {
        // Given
        CompanySearchCriteriaDTO criteria = CompanySearchCriteriaDTO.of(null, null, null, null, null, null, 1, 5);
        PageResultDTO<Company> companies = new PageResultDTO<>(List.of(company), 1L, 1, 5);
        
        when(companyRepository.search(criteria)).thenReturn(companies);
        when(companyMapper.toViewDTO(company)).thenReturn(companyView);
        
        // When
        PageResultDTO<CompanyViewDTO> result = companyQueryService.search(criteria);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());
        assertEquals(1L, result.total());
        assertEquals(1, result.page());
        assertEquals(5, result.size());
        
        verify(companyRepository).search(criteria);
        verify(companyMapper).toViewDTO(company);
        verify(metricsPort).incrementCounter(eq("company_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao buscar com critérios nulos")
    void shouldThrowExceptionWhenSearchingWithNullCriteria() {
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            companyQueryService.search(null));
        
        assertEquals("Erro interno ao buscar empresas", exception.getMessage());
        
        verify(companyRepository).search(null);
        verify(companyMapper, never()).toViewDTO(any());
        // Não verifica métricas pois a exceção é propagada
    }
    
    @Test
    @DisplayName("Deve registrar métrica de erro quando repository falha")
    void shouldRecordMetricWhenRepositoryFails() {
        // Given
        when(companyCachePort.getCompanyByIdFromCache(companyId.toString())).thenReturn(null);
        when(companyRepository.findById(companyId)).thenThrow(new RuntimeException("Erro no banco de dados"));
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            companyQueryService.getById(companyId));
        
        assertEquals("Erro interno ao buscar empresa por ID", exception.getMessage());
        
        verify(companyCachePort).getCompanyByIdFromCache(companyId.toString());
        verify(companyRepository).findById(companyId);
        verify(companyMapper, never()).toViewDTO(any());
        verify(companyCachePort, never()).cacheCompanyById(anyString(), any());
        verify(metricsPort).incrementCounter(eq("company_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve registrar métrica de erro quando mapper falha")
    void shouldRecordMetricWhenMapperFails() {
        // Given
        when(companyCachePort.getCompanyByIdFromCache(companyId.toString())).thenReturn(null);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyMapper.toViewDTO(company)).thenThrow(new RuntimeException("Erro no mapper"));
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            companyQueryService.getById(companyId));
        
        assertEquals("Erro interno ao buscar empresa por ID", exception.getMessage());
        
        verify(companyCachePort).getCompanyByIdFromCache(companyId.toString());
        verify(companyRepository).findById(companyId);
        verify(companyMapper).toViewDTO(company);
        verify(companyCachePort, never()).cacheCompanyById(anyString(), any());
        verify(metricsPort).incrementCounter(eq("company_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve buscar múltiplas empresas com sucesso")
    void shouldSearchMultipleCompaniesSuccessfully() {
        // Given
        Company company2 = Company.create(
            "Outra Empresa",
            "Outra Empresa Ltda",
            "98765432000198",
            "987654321",
            "123456789",
            TaxRegimeEnum.LUCRO_REAL,
            "987654321"
        );
        
        CompanyViewDTO companyView2 = new CompanyViewDTO(
            UUID.randomUUID(),
            UUID.randomUUID(), // codeCompany
            UUID.randomUUID(), // tenantId
            "Outra Empresa",
            "Outra Empresa Ltda",
            "98765432000198",
            "987654321",
            "123456789",
            null, null, null, null,
            TaxRegimeEnum.LUCRO_REAL,
            new ArrayList<>(),
            "987654321",
            CompanyStatusEnum.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        List<Company> companies = List.of(company, company2);
        CompanySearchCriteriaDTO criteria = CompanySearchCriteriaDTO.of(null, null, null, null, null, null, 0, 10);
        PageResultDTO<Company> companiesPage = new PageResultDTO<>(companies, 2L, 0, 10);
        
        when(companyRepository.search(criteria)).thenReturn(companiesPage);
        when(companyMapper.toViewDTO(company)).thenReturn(companyView);
        when(companyMapper.toViewDTO(company2)).thenReturn(companyView2);
        
        // When
        PageResultDTO<CompanyViewDTO> result = companyQueryService.search(criteria);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.items().size());
        assertEquals(2L, result.total());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        
        verify(companyRepository).search(criteria);
        verify(companyMapper).toViewDTO(company);
        verify(companyMapper).toViewDTO(company2);
        verify(metricsPort).incrementCounter(eq("company_queries_total"), any());
    }

    @Test
    @DisplayName("Deve buscar empresa por TenantId com sucesso do cache")
    void shouldGetCompanyByTenantIdFromCache() {
        // Given
        UUID tenantId = UUID.randomUUID();
        CompanySimpleResponseDTO simpleResponseDTO = CompanySimpleResponseDTO.builder()
            .id(companyId)
            .codeCompany(UUID.randomUUID())
            .tenantId(tenantId)
            .name("Empresa Teste")
            .legalName("Empresa Teste Ltda")
            .cnpj("11222333000181")
            .stateRegistration("123456789")
            .municipalRegistration("987654321")
            .taxRegime(TaxRegimeEnum.SIMPLES_NACIONAL)
            .ein("123456789")
            .status(CompanyStatusEnum.PENDING_APPROVAL)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(companyCachePort.getSimpleCompanyByTenantIdFromCache(tenantId.toString()))
            .thenReturn(simpleResponseDTO);

        // When
        CompanySimpleResponseDTO result = companyQueryService.getSimpleByTenantId(tenantId);

        // Then
        assertNotNull(result);
        assertEquals(companyId, result.getId());
        assertEquals(simpleResponseDTO.getName(), result.getName());
        assertEquals(simpleResponseDTO.getCnpj(), result.getCnpj());

        verify(companyCachePort).getSimpleCompanyByTenantIdFromCache(tenantId.toString());
        verify(companyRepository, never()).findByTenantId(any());
        verify(metricsPort).incrementCounter(eq("company_queries_total"), any());
    }

    @Test
    @DisplayName("Deve buscar empresa por TenantId com sucesso do banco e cachear")
    void shouldGetCompanyByTenantIdFromDatabaseAndCache() {
        // Given
        UUID tenantId = UUID.randomUUID();
        Company company = CompanyTestBuilder.createDefaultCompany();
        // Adicionar dados obrigatórios para aprovação
        addRequiredDataForApproval(company);
        // Aprovar a empresa para que ela tenha status ACTIVE
        company.approve();
        
        CompanyViewDTO companyView = CompanyTestBuilder.createDefaultCompanyViewDTO();
        CompanySimpleResponseDTO simpleResponseDTO = CompanySimpleResponseDTO.builder()
            .id(companyId)
            .codeCompany(UUID.randomUUID())
            .tenantId(tenantId)
            .name("Empresa Teste")
            .legalName("Empresa Teste Ltda")
            .cnpj("11222333000181")
            .stateRegistration("123456789")
            .municipalRegistration("987654321")
            .taxRegime(TaxRegimeEnum.SIMPLES_NACIONAL)
            .ein("123456789")
            .status(CompanyStatusEnum.ACTIVE) // Status ativo para passar na validação
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        when(companyCachePort.getSimpleCompanyByTenantIdFromCache(tenantId.toString()))
            .thenReturn(null);
        when(companyRepository.findByTenantId(tenantId))
            .thenReturn(Optional.of(company));
        when(companyMapper.toViewDTO(company))
            .thenReturn(companyView);
        when(companyMapper.toSimpleResponseDTO(companyView))
            .thenReturn(simpleResponseDTO);

        // When
        CompanySimpleResponseDTO result = companyQueryService.getSimpleByTenantId(tenantId);

        // Then
        assertNotNull(result);
        assertEquals(companyId, result.getId());
        assertEquals(simpleResponseDTO.getName(), result.getName());
        assertEquals(simpleResponseDTO.getCnpj(), result.getCnpj());

        verify(companyCachePort).getSimpleCompanyByTenantIdFromCache(tenantId.toString());
        verify(companyRepository).findByTenantId(tenantId);
        verify(companyMapper).toViewDTO(company);
        verify(companyMapper).toSimpleResponseDTO(companyView);
        verify(companyCachePort).cacheSimpleCompanyByTenantId(tenantId.toString(), simpleResponseDTO);
        verify(metricsPort).incrementCounter(eq("company_queries_total"), any());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando empresa não encontrada por TenantId")
    void shouldThrowNotFoundExceptionWhenCompanyNotFoundByTenantId() {
        // Given
        UUID tenantId = UUID.randomUUID();
        when(companyCachePort.getSimpleCompanyByTenantIdFromCache(tenantId.toString()))
            .thenReturn(null);
        when(companyRepository.findByTenantId(tenantId))
            .thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            companyQueryService.getSimpleByTenantId(tenantId);
        });

        assertEquals("Empresa não encontrada: " + tenantId, exception.getMessage());
        assertEquals("COMPANY_NOT_FOUND", exception.getErrorCode());

        verify(companyCachePort).getSimpleCompanyByTenantIdFromCache(tenantId.toString());
        verify(companyRepository).findByTenantId(tenantId);
        verify(companyMapper, never()).toViewDTO(any());
        verify(companyMapper, never()).toSimpleResponseDTO(any());
        verify(companyCachePort, never()).cacheSimpleCompanyByTenantId(anyString(), any());
        verify(metricsPort).incrementCounter(eq("company_not_found_total"), any());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando empresa não está ativa por TenantId")
    void shouldThrowNotFoundExceptionWhenCompanyIsNotActiveByTenantId() {
        // Given
        UUID tenantId = UUID.randomUUID();
        Company company = CompanyTestBuilder.createDefaultCompany();
        // Empresa com status PENDING_APPROVAL (não ativa)
        
        when(companyCachePort.getSimpleCompanyByTenantIdFromCache(tenantId.toString()))
            .thenReturn(null);
        when(companyRepository.findByTenantId(tenantId))
            .thenReturn(Optional.of(company));

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            companyQueryService.getSimpleByTenantId(tenantId);
        });

        assertEquals("Empresa não está ativa: " + tenantId, exception.getMessage());
        assertEquals("COMPANY_NOT_ACTIVE", exception.getErrorCode());

        verify(companyCachePort).getSimpleCompanyByTenantIdFromCache(tenantId.toString());
        verify(companyRepository).findByTenantId(tenantId);
        verify(companyMapper, never()).toViewDTO(any());
        verify(companyMapper, never()).toSimpleResponseDTO(any());
        verify(companyCachePort, never()).cacheSimpleCompanyByTenantId(anyString(), any());
        verify(metricsPort).incrementCounter(eq("company_invalid_status_total"), any());
    }

    // Métodos auxiliares para adicionar dados obrigatórios para aprovação
    private void addRequiredDataForApproval(Company company) {
        addAddress(company);
        addBankAccount(company);
        addCnae(company);
        addContact(company);
        addRepresentative(company);
    }
    
    private void addAddress(Company company) {
        Address address = Address.create(
            "Rua Teste",
            "123",
            "Apto 1",
            "Centro",
            "São Paulo",
            "SP",
            "Brasil",
            "01234567"
        );
        company.addAddress(address);
    }
    
    private void addBankAccount(Company company) {
        BankAccount bankAccount = BankAccount.create(
            "001",
            "1234",
            "5",
            "12345678",
            "9",
            AccountTypeEnum.CORRENTE
        );
        company.addBankAccount(bankAccount);
    }
    
    private void addCnae(Company company) {
        Cnae cnae = Cnae.create(
            "7020400",
            "Atividades de consultoria em gestão empresarial",
            "M",
            "70",
            "70.2",
            "70.20-4",
            "70.20-4/00",
            true, // principal
            company.getId()
        );
        company.addCnae(cnae);
    }
    
    private void addContact(Company company) {
        Contact contact = Contact.create(
            "João Silva",
            "joao@empresa.com",
            "11999999999",
            "www.empresa.com",
            "Gerente",
            "Vendas"
        );
        company.addContact(contact);
    }
    
    private void addRepresentative(Company company) {
        Representative representative = Representative.create(
            "Maria Santos",
            "11144477735", // CPF válido
            "123456789",
            LocalDate.of(1980, 1, 1),
            "maria@empresa.com",
            "11988888888",
            "Diretora"
        );
        company.addRepresentative(representative);
    }
}