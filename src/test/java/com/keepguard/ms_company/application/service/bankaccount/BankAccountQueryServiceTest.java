package com.keepguard.ms_company.application.service.bankaccount;

import com.keepguard.ms_company.application.dto.bankaccount.BankAccountSearchCriteriaDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.mapper.BankAccountApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.BankAccountRepositoryPort;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.domain.entity.BankAccount;
import com.keepguard.ms_company.domain.enums.AccountTypeEnum;
import com.keepguard.ms_company.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_company.application.port.out.cache.BankAccountCachePort;
import com.keepguard.ms_company.test.builder.BankAccountTestBuilder;
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
 * Testes unitários para BankAccountQueryService
 * Testa operações de consulta com verificações de métricas
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BankAccount Query Service Tests")
class BankAccountQueryServiceTest {
    
    @Mock
    private BankAccountRepositoryPort bankAccountRepository;
    
    @Mock
    private BankAccountApplicationMapper bankAccountMapper;
    
    @Mock
    private MetricsPort metricsPort;
    
    @Mock
    private BankAccountCachePort bankAccountCachePort;
    
    @InjectMocks
    private BankAccountQueryService bankAccountQueryService;
    
    private BankAccount bankAccount;
    private BankAccountViewDTO bankAccountView;
    private UUID bankAccountId;
    private UUID companyId;
    
    @BeforeEach
    void setUp() {
        bankAccountId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        
        // Criar dados bancários de teste
        bankAccount = BankAccountTestBuilder.builder()
            .withId(bankAccountId)
            .withCompanyId(companyId)
            .buildDomain();
        
        // Criar view de teste
        bankAccountView = BankAccountTestBuilder.builder()
            .withId(bankAccountId)
            .withCompanyId(companyId)
            .buildView();
        
        // Configurar mocks comuns
        lenient().when(bankAccountMapper.toViewDTO(any(BankAccount.class), any(UUID.class))).thenReturn(bankAccountView);
        lenient().when(bankAccountCachePort.getBankAccountsByCompanyIdFromCache(anyString())).thenReturn(null);
        lenient().when(bankAccountCachePort.getActiveBankAccountByCompanyIdFromCache(anyString())).thenReturn(null);
    }
    
    @Test
    @DisplayName("Deve buscar dados bancários por ID com sucesso")
    void shouldGetBankAccountByIdSuccessfully() {
        // Given
        when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.findCompanyIdByBankAccountId(bankAccountId)).thenReturn(Optional.of(companyId));
        
        // When
        BankAccountViewDTO result = bankAccountQueryService.getById(bankAccountId);
        
        // Then
        assertNotNull(result);
        assertEquals(bankAccountId, result.id());
        assertEquals(companyId, result.companyId());
        
        verify(bankAccountRepository).findById(bankAccountId);
        verify(bankAccountRepository).findCompanyIdByBankAccountId(bankAccountId);
        verify(bankAccountMapper).toViewDTO(bankAccount, companyId);
        verify(metricsPort).incrementCounter(eq("bank_account_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao buscar dados bancários inexistentes por ID")
    void shouldThrowExceptionWhenGettingNonExistentBankAccountById() {
        // Given
        when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bankAccountQueryService.getById(bankAccountId);
        });
        
        assertEquals("Dados bancários não encontrados: " + bankAccountId, exception.getMessage());
        verify(metricsPort).incrementCounter(eq("bank_account_not_found_total"), any());
    }
    
    @Test
    @DisplayName("Deve listar dados bancários por empresa com sucesso (cache miss)")
    void shouldListBankAccountsByCompanySuccessfullyWithCacheMiss() {
        // Given
        List<BankAccount> bankAccounts = Arrays.asList(
            BankAccountTestBuilder.builder().withId(UUID.randomUUID()).withCompanyId(companyId).buildDomain(),
            BankAccountTestBuilder.builder().withId(UUID.randomUUID()).withCompanyId(companyId).buildDomain()
        );
        
        when(bankAccountCachePort.getBankAccountsByCompanyIdFromCache(companyId.toString())).thenReturn(null);
        when(bankAccountRepository.findByCompanyId(companyId)).thenReturn(bankAccounts);
        
        // When
        List<BankAccountViewDTO> result = bankAccountQueryService.listByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        verify(bankAccountCachePort).getBankAccountsByCompanyIdFromCache(companyId.toString());
        verify(bankAccountRepository).findByCompanyId(companyId);
        verify(bankAccountMapper, times(2)).toViewDTO(any(BankAccount.class), eq(companyId));
        verify(bankAccountCachePort).cacheBankAccountsByCompanyId(companyId.toString(), result);
        verify(metricsPort).incrementCounter(eq("bank_account_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve listar dados bancários por empresa com sucesso (cache hit)")
    void shouldListBankAccountsByCompanySuccessfullyWithCacheHit() {
        // Given
        List<BankAccountViewDTO> cachedBankAccounts = Arrays.asList(
            BankAccountTestBuilder.builder().withId(UUID.randomUUID()).withCompanyId(companyId).buildView(),
            BankAccountTestBuilder.builder().withId(UUID.randomUUID()).withCompanyId(companyId).buildView()
        );
        
        when(bankAccountCachePort.getBankAccountsByCompanyIdFromCache(companyId.toString())).thenReturn(cachedBankAccounts);
        
        // When
        List<BankAccountViewDTO> result = bankAccountQueryService.listByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(cachedBankAccounts, result);
        
        verify(bankAccountCachePort).getBankAccountsByCompanyIdFromCache(companyId.toString());
        verify(bankAccountRepository, never()).findByCompanyId(any());
        verify(bankAccountMapper, never()).toViewDTO(any(BankAccount.class), any(UUID.class));
        verify(bankAccountCachePort, never()).cacheBankAccountsByCompanyId(anyString(), any());
        verify(metricsPort).incrementCounter(eq("bank_account_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve retornar lista vazia quando empresa não tem dados bancários")
    void shouldReturnEmptyListWhenCompanyHasNoBankAccounts() {
        // Given
        when(bankAccountCachePort.getBankAccountsByCompanyIdFromCache(companyId.toString())).thenReturn(null);
        when(bankAccountRepository.findByCompanyId(companyId)).thenReturn(Arrays.asList());
        
        // When
        List<BankAccountViewDTO> result = bankAccountQueryService.listByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(bankAccountCachePort).getBankAccountsByCompanyIdFromCache(companyId.toString());
        verify(bankAccountRepository).findByCompanyId(companyId);
        verify(bankAccountCachePort).cacheBankAccountsByCompanyId(companyId.toString(), result);
        verify(metricsPort).incrementCounter(eq("bank_account_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve buscar dados bancários ativos por empresa com sucesso (cache miss)")
    void shouldGetActiveBankAccountByCompanySuccessfullyWithCacheMiss() {
        // Given
        when(bankAccountCachePort.getActiveBankAccountByCompanyIdFromCache(companyId.toString())).thenReturn(null);
        when(bankAccountRepository.findActiveByCompanyId(companyId)).thenReturn(Optional.of(bankAccount));
        
        // When
        BankAccountViewDTO result = bankAccountQueryService.getActiveByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(bankAccountId, result.id());
        assertEquals(companyId, result.companyId());
        
        verify(bankAccountCachePort).getActiveBankAccountByCompanyIdFromCache(companyId.toString());
        verify(bankAccountRepository).findActiveByCompanyId(companyId);
        verify(bankAccountMapper).toViewDTO(bankAccount, companyId);
        verify(bankAccountCachePort).cacheActiveBankAccountByCompanyId(companyId.toString(), result);
        verify(metricsPort).incrementCounter(eq("bank_account_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve buscar dados bancários ativos por empresa com sucesso (cache hit)")
    void shouldGetActiveBankAccountByCompanySuccessfullyWithCacheHit() {
        // Given
        when(bankAccountCachePort.getActiveBankAccountByCompanyIdFromCache(companyId.toString())).thenReturn(bankAccountView);
        
        // When
        BankAccountViewDTO result = bankAccountQueryService.getActiveByCompanyId(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(bankAccountId, result.id());
        assertEquals(companyId, result.companyId());
        assertEquals(bankAccountView, result);
        
        verify(bankAccountCachePort).getActiveBankAccountByCompanyIdFromCache(companyId.toString());
        verify(bankAccountRepository, never()).findActiveByCompanyId(any());
        verify(bankAccountMapper, never()).toViewDTO(any(BankAccount.class), any(UUID.class));
        verify(bankAccountCachePort, never()).cacheActiveBankAccountByCompanyId(anyString(), any());
        verify(metricsPort).incrementCounter(eq("bank_account_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao buscar dados bancários ativos inexistentes")
    void shouldThrowExceptionWhenGettingNonExistentActiveBankAccount() {
        // Given
        when(bankAccountCachePort.getActiveBankAccountByCompanyIdFromCache(companyId.toString())).thenReturn(null);
        when(bankAccountRepository.findActiveByCompanyId(companyId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bankAccountQueryService.getActiveByCompanyId(companyId);
        });
        
        assertEquals("Dados bancários ativos não encontrados para a empresa: " + companyId, exception.getMessage());
        verify(bankAccountCachePort).getActiveBankAccountByCompanyIdFromCache(companyId.toString());
        verify(bankAccountRepository).findActiveByCompanyId(companyId);
        verify(metricsPort).incrementCounter(eq("bank_account_not_found_total"), any());
    }
    
    @Test
    @DisplayName("Deve listar todos os dados bancários com sucesso")
    void shouldListAllBankAccountsSuccessfully() {
        // Given
        List<BankAccount> bankAccounts = Arrays.asList(
            BankAccountTestBuilder.builder().withId(UUID.randomUUID()).withCompanyId(UUID.randomUUID()).buildDomain(),
            BankAccountTestBuilder.builder().withId(UUID.randomUUID()).withCompanyId(UUID.randomUUID()).buildDomain()
        );
        
        when(bankAccountRepository.findAll()).thenReturn(bankAccounts);
        when(bankAccountRepository.findCompanyIdByBankAccountId(any(UUID.class))).thenReturn(Optional.of(companyId));
        
        // When
        List<BankAccountViewDTO> result = bankAccountQueryService.listAll();
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        verify(bankAccountRepository).findAll();
        verify(bankAccountMapper, times(2)).toViewDTO(any(BankAccount.class), eq(companyId));
        verify(metricsPort).incrementCounter(eq("bank_account_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve buscar dados bancários com critérios com sucesso")
    void shouldSearchBankAccountsWithCriteriaSuccessfully() {
        // Given
        BankAccountSearchCriteriaDTO criteria = new BankAccountSearchCriteriaDTO(
            companyId,
            "001",
            "CORRENTE",
            true,
            0,
            20,
            Arrays.asList("code"),
            "ASC"
        );
        
        List<BankAccount> bankAccounts = Arrays.asList(
            BankAccountTestBuilder.builder().withId(UUID.randomUUID()).withCompanyId(companyId).buildDomain()
        );
        
        PageResultDTO<BankAccount> pageResult = new PageResultDTO<>(
            bankAccounts,
            1L,
            0,
            20
        );
        
        when(bankAccountRepository.search(criteria)).thenReturn(pageResult);
        when(bankAccountRepository.findCompanyIdByBankAccountId(any(UUID.class))).thenReturn(Optional.of(companyId));
        
        // When
        PageResultDTO<BankAccountViewDTO> result = bankAccountQueryService.search(criteria);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());
        assertEquals(1L, result.total());
        assertEquals(0, result.page());
        assertEquals(20, result.size());
        
        verify(bankAccountRepository).search(criteria);
        verify(bankAccountMapper).toViewDTO(any(BankAccount.class), eq(companyId));
        verify(metricsPort).incrementCounter(eq("bank_account_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve retornar resultado vazio quando busca não encontra dados")
    void shouldReturnEmptyResultWhenSearchFindsNothing() {
        // Given
        BankAccountSearchCriteriaDTO criteria = new BankAccountSearchCriteriaDTO(
            companyId,
            "999",
            "POUPANCA",
            true,
            0,
            20,
            Arrays.asList("code"),
            "ASC"
        );
        
        PageResultDTO<BankAccount> pageResult = new PageResultDTO<>(
            Arrays.asList(),
            0L,
            0,
            20
        );
        
        when(bankAccountRepository.search(criteria)).thenReturn(pageResult);
        
        // When
        PageResultDTO<BankAccountViewDTO> result = bankAccountQueryService.search(criteria);
        
        // Then
        assertNotNull(result);
        assertTrue(result.items().isEmpty());
        assertEquals(0L, result.total());
        
        verify(bankAccountRepository).search(criteria);
        verify(metricsPort).incrementCounter(eq("bank_account_queries_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando empresa não encontrada para dados bancários")
    void shouldThrowExceptionWhenCompanyNotFoundForBankAccount() {
        // Given
        when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.findCompanyIdByBankAccountId(bankAccountId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bankAccountQueryService.getById(bankAccountId);
        });
        
        assertEquals("Empresa não encontrada para os dados bancários: " + bankAccountId, exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve tratar exceções de sistema corretamente")
    void shouldHandleSystemExceptionsCorrectly() {
        // Given
        when(bankAccountRepository.findById(bankAccountId)).thenThrow(new RuntimeException("Database error"));
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bankAccountQueryService.getById(bankAccountId);
        });
        
        assertEquals("Falha ao buscar dados bancários", exception.getMessage());
        verify(metricsPort).incrementCounter(eq("bank_account_system_errors_total"), any());
    }
    
    @Test
    @DisplayName("Deve buscar dados bancários com diferentes tipos de conta")
    void shouldSearchBankAccountsWithDifferentAccountTypes() {
        // Given
        BankAccountSearchCriteriaDTO criteria = new BankAccountSearchCriteriaDTO(
            companyId,
            null,
            "POUPANCA",
            null,
            0,
            20,
            null,
            "ASC"
        );
        
        BankAccount poupancaAccount = BankAccountTestBuilder.builder()
            .withId(UUID.randomUUID())
            .withCompanyId(companyId)
            .withPoupancaType()
            .buildDomain();
        
        List<BankAccount> bankAccounts = Arrays.asList(poupancaAccount);
        
        PageResultDTO<BankAccount> pageResult = new PageResultDTO<>(
            bankAccounts,
            1L,
            0,
            20
        );
        
        when(bankAccountRepository.search(criteria)).thenReturn(pageResult);
        when(bankAccountRepository.findCompanyIdByBankAccountId(any(UUID.class))).thenReturn(Optional.of(companyId));
        
        BankAccountViewDTO expectedView = BankAccountTestBuilder.builder()
            .withId(poupancaAccount.getId())
            .withCompanyId(companyId)
            .withPoupancaType()
            .buildView();
        when(bankAccountMapper.toViewDTO(poupancaAccount, companyId)).thenReturn(expectedView);
        
        // When
        PageResultDTO<BankAccountViewDTO> result = bankAccountQueryService.search(criteria);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.items().size());
        assertEquals(AccountTypeEnum.POUPANCA, result.items().get(0).accountType());
        
        verify(bankAccountRepository).search(criteria);
        verify(metricsPort).incrementCounter(eq("bank_account_queries_total"), any());
    }
}
