package com.keepguard.ms_company.application.service.bankaccount;

import com.keepguard.ms_company.application.dto.bankaccount.BankAccountCreateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountViewDTO;
import com.keepguard.ms_company.application.mapper.BankAccountApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.BankAccountRepositoryPort;
import com.keepguard.ms_company.application.port.out.persistence.CompanyRepositoryPort;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.domain.entity.BankAccount;
import com.keepguard.ms_company.domain.entity.Company;
import com.keepguard.ms_company.domain.enums.TaxRegimeEnum;
import com.keepguard.ms_company.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_company.test.builder.BankAccountTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Testes unitários para BankAccountCommandService
 * Inclui verificações de métricas usando o serviço genérico MetricsService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BankAccount Command Service Tests")
class BankAccountCommandServiceTest {
    
    @Mock
    private BankAccountRepositoryPort bankAccountRepository;
    
    @Mock
    private CompanyRepositoryPort companyRepository;
    
    @Mock
    private BankAccountApplicationMapper bankAccountMapper;
    
    @Mock
    private MetricsPort metricsPort;
    
    @InjectMocks
    private BankAccountCommandService bankAccountCommandService;
    
    private BankAccount bankAccount;
    private BankAccountViewDTO bankAccountView;
    private Company company;
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
        
        // Criar empresa de teste
        company = Company.create(
            "Empresa Teste",
            "Empresa Teste Ltda",
            "11222333000181",
            "123456789",
            "987654321",
            com.keepguard.ms_company.domain.enums.TaxRegimeEnum.SIMPLES_NACIONAL,
            "123456789"
        );
        
        // Criar view de teste
        bankAccountView = BankAccountTestBuilder.builder()
            .withId(bankAccountId)
            .withCompanyId(companyId)
            .buildView();
        
        // Configurar mocks comuns com lenient para evitar problemas de stubbing
        lenient().when(bankAccountRepository.save(any(BankAccount.class), any(UUID.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(bankAccountMapper.toViewDTO(any(BankAccount.class), any(UUID.class))).thenReturn(bankAccountView);
        lenient().when(companyRepository.save(any(Company.class))).thenReturn(company);
    }
    
    @Test
    @DisplayName("Deve criar dados bancários com sucesso")
    void shouldCreateBankAccountSuccessfully() {
        // Given
        BankAccountCreateCommandDTO command = BankAccountTestBuilder.builder()
            .withCompanyId(companyId)
            .buildCreateCommand();
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(bankAccountRepository.findActiveByCompanyId(companyId)).thenReturn(Optional.empty());
        when(bankAccountMapper.toDomain(command)).thenReturn(bankAccount);
        
        // When
        BankAccountViewDTO result = bankAccountCommandService.create(companyId, command);
        
        // Then
        assertNotNull(result);
        assertEquals(bankAccountId, result.id());
        assertEquals(companyId, result.companyId());
        
        verify(companyRepository).findById(companyId);
        verify(bankAccountRepository).findActiveByCompanyId(companyId);
        verify(bankAccountMapper).toDomain(command);
        verify(bankAccountRepository).save(bankAccount, companyId);
        verify(companyRepository).save(company);
        verify(bankAccountMapper).toViewDTO(bankAccount, companyId);
        verify(metricsPort).incrementCounter(eq("bank_account_created_total"), any());
    }
    
    @Test
    @DisplayName("Deve desativar dados bancários ativos existentes ao criar novos")
    void shouldDeactivateExistingActiveBankAccountWhenCreatingNew() {
        // Given
        BankAccount existingActiveBankAccount = BankAccountTestBuilder.builder()
            .withId(UUID.randomUUID())
            .withCompanyId(companyId)
            .buildDomain();
        
        BankAccountCreateCommandDTO command = BankAccountTestBuilder.builder()
            .withCompanyId(companyId)
            .buildCreateCommand();
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(bankAccountRepository.findActiveByCompanyId(companyId)).thenReturn(Optional.of(existingActiveBankAccount));
        when(bankAccountMapper.toDomain(command)).thenReturn(bankAccount);
        
        // When
        BankAccountViewDTO result = bankAccountCommandService.create(companyId, command);
        
        // Then
        assertNotNull(result);
        verify(bankAccountRepository).save(existingActiveBankAccount);
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando empresa não existe")
    void shouldThrowExceptionWhenCompanyNotFound() {
        // Given
        BankAccountCreateCommandDTO command = BankAccountTestBuilder.builder()
            .withCompanyId(companyId)
            .buildCreateCommand();
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bankAccountCommandService.create(companyId, command);
        });
        
        assertEquals("Empresa não encontrada: " + companyId, exception.getMessage());
        verify(metricsPort).incrementCounter(eq("bank_account_business_errors_total"), any());
    }
    
    @Test
    @DisplayName("Deve atualizar dados bancários com sucesso")
    void shouldUpdateBankAccountSuccessfully() {
        // Given
        BankAccountUpdateCommandDTO command = BankAccountTestBuilder.builder()
            .withCode("001")
            .withAgency("5678")
            .buildUpdateCommand();
        
        BankAccount updatedBankAccount = BankAccountTestBuilder.builder()
            .withId(bankAccountId)
            .withCode("001")
            .withAgency("5678")
            .buildDomain();
        
        Company company = Company.create(
            "Empresa Teste",
            "Empresa Teste LTDA",
            "12345678000195",
            "123456789",
            "987654321",
            TaxRegimeEnum.SIMPLES_NACIONAL,
            "123456789"
        );
        
        when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.findCompanyIdByBankAccountId(bankAccountId)).thenReturn(Optional.of(companyId));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(bankAccountMapper.toDomain(command, bankAccount)).thenReturn(updatedBankAccount);
        
        // When
        BankAccountViewDTO result = bankAccountCommandService.update(bankAccountId, command);
        
        // Then
        assertNotNull(result);
        assertEquals(bankAccountId, result.id());
        
        verify(bankAccountRepository).findById(bankAccountId);
        verify(bankAccountRepository).findCompanyIdByBankAccountId(bankAccountId);
        verify(companyRepository).findById(companyId);
        verify(bankAccountMapper).toDomain(command, bankAccount);
        verify(bankAccountRepository).save(updatedBankAccount, companyId);
        verify(bankAccountMapper).toViewDTO(updatedBankAccount, companyId);
        verify(metricsPort).incrementCounter(eq("bank_account_updated_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao atualizar dados bancários inexistentes")
    void shouldThrowExceptionWhenUpdatingNonExistentBankAccount() {
        // Given
        BankAccountUpdateCommandDTO command = BankAccountTestBuilder.builder()
            .withCode("001")
            .buildUpdateCommand();
        
        when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bankAccountCommandService.update(bankAccountId, command);
        });
        
        assertEquals("Dados bancários não encontrados: " + bankAccountId, exception.getMessage());
        verify(metricsPort).incrementCounter(eq("bank_account_not_found_total"), any());
    }
    
    @Test
    @DisplayName("Deve ativar dados bancários com sucesso")
    void shouldActivateBankAccountSuccessfully() {
        // Given
        BankAccount inactiveBankAccount = BankAccountTestBuilder.builder()
            .withId(bankAccountId)
            .withCompanyId(companyId)
            .inactive()
            .buildDomain();
        
        when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(inactiveBankAccount));
        when(bankAccountRepository.findCompanyIdByBankAccountId(bankAccountId)).thenReturn(Optional.of(companyId));
        when(bankAccountRepository.findActiveByCompanyId(companyId)).thenReturn(Optional.empty());
        
        // When
        BankAccountViewDTO result = bankAccountCommandService.activate(bankAccountId);
        
        // Then
        assertNotNull(result);
        verify(bankAccountRepository).save(inactiveBankAccount, companyId);
        verify(bankAccountMapper).toViewDTO(inactiveBankAccount, companyId);
        verify(metricsPort).incrementCounter(eq("bank_account_activated_total"), any());
    }
    
    @Test
    @DisplayName("Deve desativar outros dados bancários ativos ao ativar novos")
    void shouldDeactivateOtherActiveBankAccountsWhenActivating() {
        // Given
        BankAccount otherActiveBankAccount = BankAccountTestBuilder.builder()
            .withId(UUID.randomUUID())
            .withCompanyId(companyId)
            .buildDomain();
        
        when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.findCompanyIdByBankAccountId(bankAccountId)).thenReturn(Optional.of(companyId));
        when(bankAccountRepository.findActiveByCompanyId(companyId)).thenReturn(Optional.of(otherActiveBankAccount));
        
        // When
        BankAccountViewDTO result = bankAccountCommandService.activate(bankAccountId);
        
        // Then
        assertNotNull(result);
        verify(bankAccountRepository).save(otherActiveBankAccount);
        verify(bankAccountRepository).save(bankAccount, companyId);
    }
    
    @Test
    @DisplayName("Deve desativar dados bancários com sucesso")
    void shouldDeactivateBankAccountSuccessfully() {
        // Given
        when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.findCompanyIdByBankAccountId(bankAccountId)).thenReturn(Optional.of(companyId));
        
        // When
        BankAccountViewDTO result = bankAccountCommandService.deactivate(bankAccountId);
        
        // Then
        assertNotNull(result);
        verify(bankAccountRepository).save(bankAccount, companyId);
        verify(bankAccountMapper).toViewDTO(bankAccount, companyId);
        verify(metricsPort).incrementCounter(eq("bank_account_deactivated_total"), any());
    }
    
    @Test
    @DisplayName("Deve remover dados bancários com sucesso")
    void shouldDeleteBankAccountSuccessfully() {
        // Given
        when(bankAccountRepository.existsById(bankAccountId)).thenReturn(true);
        
        // When
        bankAccountCommandService.delete(bankAccountId);
        
        // Then
        verify(bankAccountRepository).existsById(bankAccountId);
        verify(bankAccountRepository).deleteById(bankAccountId);
        verify(metricsPort).incrementCounter(eq("bank_account_deleted_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao remover dados bancários inexistentes")
    void shouldThrowExceptionWhenDeletingNonExistentBankAccount() {
        // Given
        when(bankAccountRepository.existsById(bankAccountId)).thenReturn(false);
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bankAccountCommandService.delete(bankAccountId);
        });
        
        assertEquals("Dados bancários não encontrados: " + bankAccountId, exception.getMessage());
        verify(metricsPort).incrementCounter(eq("bank_account_not_found_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando empresa não encontrada para dados bancários")
    void shouldThrowExceptionWhenCompanyNotFoundForBankAccount() {
        // Given
        BankAccountUpdateCommandDTO command = BankAccountTestBuilder.builder()
            .withCode("001")
            .buildUpdateCommand();
        
        when(bankAccountRepository.findById(bankAccountId)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.findCompanyIdByBankAccountId(bankAccountId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bankAccountCommandService.update(bankAccountId, command);
        });
        
        assertEquals("Empresa não encontrada para os dados bancários: " + bankAccountId, exception.getMessage());
    }
}
