package com.keepguard.ms_company.application.service.company;

import com.keepguard.ms_company.application.dto.company.CompanyCreateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyViewDTO;
import com.keepguard.ms_company.application.mapper.CompanyApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.CompanyRepositoryPort;
import com.keepguard.ms_company.application.port.out.persistence.CompanyPolicyRepositoryPort;
import com.keepguard.ms_company.application.service.exception.AlreadyExistsException;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.application.service.exception.InvalidStatusForOperationException;
import com.keepguard.ms_company.domain.entity.*;
import com.keepguard.ms_company.domain.enums.AccountTypeEnum;
import com.keepguard.ms_company.domain.enums.CompanyStatusEnum;
import com.keepguard.ms_company.domain.enums.TaxRegimeEnum;
import com.keepguard.ms_company.application.port.out.cache.CompanyCachePort;
import com.keepguard.ms_company.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_company.application.port.out.auth.AuthRoleProvisionPort;
import com.keepguard.ms_company.test.builder.CompanyTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CompanyCommandService
 * Inclui verificações de métricas usando o serviço genérico MetricsService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Company Command Service Tests")
class CompanyCommandServiceTest {
    
    @Mock
    private CompanyRepositoryPort companyRepository;
    
    @Mock
    private CompanyPolicyRepositoryPort companyPolicyRepository;
    
    @Mock
    private CompanyApplicationMapper companyMapper;
    
    @Mock
    private CompanyCachePort companyCachePort;
    
    @Mock
    private MetricsPort metricsPort;

    @Mock
    private AuthRoleProvisionPort authRoleProvisionPort;
    
    @InjectMocks
    private CompanyCommandService companyCommandService;
    
    private Company company;
    private CompanyViewDTO companyView;
    private UUID companyId;
    
    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        
        // Criar empresa de teste usando builder
        company = CompanyTestBuilder.builder()
            .withId(companyId)
            .buildDomain();
        
        // Criar view de teste usando builder
        companyView = CompanyTestBuilder.builder()
            .withId(companyId)
            .buildView();
    }
    
    @Test
    @DisplayName("Deve criar empresa com sucesso e registrar métricas")
    void shouldCreateCompanySuccessfully() {
        // Given
        CompanyCreateCommandDTO command = CompanyTestBuilder.builder()
            .withName("Nova Empresa")
            .withLegalName("Nova Empresa Ltda")
            .withCnpj("98765432000198")
            .withStateRegistration("987654321")
            .withMunicipalRegistration("123456789")
            .withTaxRegime(TaxRegimeEnum.LUCRO_REAL)
            .buildCreateCommand();
        
        when(companyRepository.existsByCnpj(anyString())).thenReturn(false);
        when(companyMapper.toDomain(command)).thenReturn(company);
        when(companyRepository.save(any(Company.class))).thenReturn(company);
        when(companyMapper.toViewDTO(company)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyCommandService.create(command);
        
        // Then
        assertNotNull(result);
        assertEquals(companyId, result.id());
        assertEquals("Empresa Teste", result.name());
        
        verify(companyRepository).existsByCnpj("98765432000198");
        verify(companyMapper).toDomain(command);
        verify(companyRepository).save(company);
        verify(authRoleProvisionPort).provisionCompanyRoles(any(UUID.class));
        verify(companyMapper).toViewDTO(company);
        verify(metricsPort).incrementCounter(eq("company_created_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção e registrar métrica de erro ao criar empresa com CNPJ duplicado")
    void shouldThrowExceptionAndRecordMetricWhenCreatingCompanyWithDuplicateCnpj() {
        // Given
        CompanyCreateCommandDTO command = CompanyTestBuilder.builder()
            .withName("Nova Empresa")
            .withLegalName("Nova Empresa Ltda")
            .withCnpj("11222333000181")
            .withStateRegistration("987654321")
            .withMunicipalRegistration("123456789")
            .withTaxRegime(TaxRegimeEnum.LUCRO_REAL)
            .buildCreateCommand();
        
        when(companyRepository.existsByCnpj(anyString())).thenReturn(true);
        
        // When & Then
        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, () -> {
            companyCommandService.create(command);
        });
        
        assertEquals("CNPJ já cadastrado: 11222333000181", exception.getMessage());
        
        verify(companyRepository).existsByCnpj("11222333000181");
        verify(companyMapper, never()).toDomain(any());
        verify(companyRepository, never()).save(any());
        verify(metricsPort).incrementCounter(eq("company_business_errors_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar empresa com dados inválidos")
    void shouldThrowExceptionWhenCreatingCompanyWithInvalidData() {
        // Given - comando válido mas empresa já existe
        CompanyCreateCommandDTO validCommand = CompanyTestBuilder.builder()
            .withName("Nova Empresa")
            .withLegalName("Nova Empresa Ltda")
            .withCnpj("11222333000181") // CNPJ já existe
            .withStateRegistration("987654321")
            .withMunicipalRegistration("123456789")
            .withTaxRegime(TaxRegimeEnum.LUCRO_REAL)
            .buildCreateCommand();
        
        when(companyRepository.existsByCnpj(anyString())).thenReturn(true);
        
        // When & Then
        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, () -> {
            companyCommandService.create(validCommand);
        });
        
        assertEquals("CNPJ já cadastrado: 11222333000181", exception.getMessage());
        
        verify(companyRepository).existsByCnpj("11222333000181");
        verify(companyMapper, never()).toDomain(any());
        verify(companyRepository, never()).save(any());
        verify(metricsPort).incrementCounter(eq("company_business_errors_total"), any());
    }
    
    @Test
    @DisplayName("Deve atualizar empresa com sucesso e registrar métricas")
    void shouldUpdateCompanySuccessfully() {
        // Given
        CompanyUpdateCommandDTO command = CompanyTestBuilder.builder()
            .withName("Empresa Atualizada")
            .withLegalName("Empresa Atualizada Ltda")
            .withStateRegistration("111111111")
            .withMunicipalRegistration("222222222")
            .withTaxRegime(TaxRegimeEnum.LUCRO_REAL)
            .buildUpdateCommand();
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyMapper.toDomain(command, company)).thenReturn(company);
        when(companyRepository.save(company)).thenReturn(company);
        when(companyMapper.toViewDTO(company)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyCommandService.update(companyId, command);
        
        // Then
        assertNotNull(result);
        assertEquals(companyId, result.id());
        
        verify(companyRepository).findById(companyId);
        verify(companyMapper).toDomain(command, company);
        verify(companyRepository).save(company);
        verify(companyMapper).toViewDTO(company);
        verify(metricsPort).incrementCounter(eq("company_updated_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção e registrar métrica ao atualizar empresa inexistente")
    void shouldThrowExceptionAndRecordMetricWhenUpdatingNonExistentCompany() {
        // Given
        CompanyUpdateCommandDTO command = CompanyTestBuilder.builder()
            .withName("Empresa Atualizada")
            .withLegalName("Empresa Atualizada Ltda")
            .withStateRegistration("111111111")
            .withMunicipalRegistration("222222222")
            .withTaxRegime(TaxRegimeEnum.LUCRO_REAL)
            .buildUpdateCommand();
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            companyCommandService.update(companyId, command);
        });
        
        assertEquals("Empresa não encontrada: " + companyId, exception.getMessage());
        
        verify(companyRepository).findById(companyId);
        verify(companyMapper, never()).toDomain(any(), any());
        verify(companyRepository, never()).save(any());
        verify(metricsPort).incrementCounter(eq("company_not_found_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao atualizar empresa com dados inválidos")
    void shouldThrowExceptionWhenUpdatingCompanyWithInvalidData() {
        // Given - comando válido mas empresa não existe
        CompanyUpdateCommandDTO validCommand = CompanyTestBuilder.builder()
            .withName("Empresa Atualizada")
            .withLegalName("Empresa Atualizada Ltda")
            .withStateRegistration("111111111")
            .withMunicipalRegistration("222222222")
            .withTaxRegime(TaxRegimeEnum.LUCRO_REAL)
            .buildUpdateCommand();
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            companyCommandService.update(companyId, validCommand);
        });
        
        assertEquals("Empresa não encontrada: " + companyId, exception.getMessage());
        
        verify(companyRepository).findById(companyId);
        verify(companyMapper, never()).toDomain(any(), any());
        verify(companyRepository, never()).save(any());
        verify(metricsPort).incrementCounter(eq("company_not_found_total"), any());
    }
    
    @Test
    @DisplayName("Deve aprovar empresa com sucesso e registrar métricas")
    void shouldApproveCompanySuccessfully() {
        // Given
        addRequiredDataForApproval(); // Adiciona dados obrigatórios
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyPolicyRepository.existsActivePolicyByCompanyId(companyId)).thenReturn(true);
        when(companyRepository.save(company)).thenReturn(company);
        when(companyMapper.toViewDTO(company)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyCommandService.approve(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(CompanyStatusEnum.ACTIVE, company.getStatus());
        
        verify(companyRepository).findById(companyId);
        verify(companyPolicyRepository).existsActivePolicyByCompanyId(companyId);
        verify(companyRepository).save(company);
        verify(companyMapper).toViewDTO(company);
        verify(metricsPort).incrementCounter(eq("company_approved_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção e registrar métrica ao aprovar empresa inexistente")
    void shouldThrowExceptionAndRecordMetricWhenApprovingNonExistentCompany() {
        // Given
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            companyCommandService.approve(companyId);
        });
        
        assertEquals("Empresa não encontrada: " + companyId, exception.getMessage());
        
        verify(companyRepository).findById(companyId);
        verify(companyRepository, never()).save(any());
        verify(metricsPort).incrementCounter(eq("company_not_found_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao tentar aprovar empresa sem política ativa")
    void shouldThrowExceptionWhenApprovingCompanyWithoutActivePolicy() {
        // Given
        addRequiredDataForApproval(); // Adiciona dados obrigatórios
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyPolicyRepository.existsActivePolicyByCompanyId(companyId)).thenReturn(false);
        
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class, 
            () -> companyCommandService.approve(companyId)
        );
        
        assertEquals("Empresa deve ter pelo menos uma política ativa para ser aprovada", exception.getMessage());
        
        verify(companyRepository).findById(companyId);
        verify(companyPolicyRepository).existsActivePolicyByCompanyId(companyId);
        verify(companyRepository, never()).save(any());
        verify(metricsPort).incrementCounter(eq("company_business_errors_total"), any());
    }
    
    @Test
    @DisplayName("Deve rejeitar empresa com sucesso e registrar métricas")
    void shouldRejectCompanySuccessfully() {
        // Given
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRepository.save(company)).thenReturn(company);
        when(companyMapper.toViewDTO(company)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyCommandService.reject(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(CompanyStatusEnum.BLOCKED, company.getStatus());
        
        verify(companyRepository).findById(companyId);
        verify(companyRepository).save(company);
        verify(companyMapper).toViewDTO(company);
        verify(metricsPort).incrementCounter(eq("company_rejected_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção e registrar métrica ao rejeitar empresa inexistente")
    void shouldThrowExceptionAndRecordMetricWhenRejectingNonExistentCompany() {
        // Given
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            companyCommandService.reject(companyId);
        });
        
        assertEquals("Empresa não encontrada: " + companyId, exception.getMessage());
        
        verify(companyRepository).findById(companyId);
        verify(companyRepository, never()).save(any());
        verify(metricsPort).incrementCounter(eq("company_not_found_total"), any());
    }
    
    @Test
    @DisplayName("Deve ativar empresa com sucesso e registrar métricas")
    void shouldActivateCompanySuccessfully() {
        // Given
        addRequiredDataForApproval(); // Adiciona dados obrigatórios
        company.approve();
        company.deactivate(); // Primeiro desativa para poder ativar
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRepository.save(company)).thenReturn(company);
        when(companyMapper.toViewDTO(company)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyCommandService.activate(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(CompanyStatusEnum.ACTIVE, company.getStatus());
        
        verify(companyRepository).findById(companyId);
        verify(companyRepository).save(company);
        verify(companyMapper).toViewDTO(company);
        verify(metricsPort).incrementCounter(eq("company_activated_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção e registrar métrica ao ativar empresa inexistente")
    void shouldThrowExceptionAndRecordMetricWhenActivatingNonExistentCompany() {
        // Given
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            companyCommandService.activate(companyId);
        });
        
        assertEquals("Empresa não encontrada: " + companyId, exception.getMessage());
        
        verify(companyRepository).findById(companyId);
        verify(companyRepository, never()).save(any());
        verify(metricsPort).incrementCounter(eq("company_not_found_total"), any());
    }
    
    @Test
    @DisplayName("Deve desativar empresa com sucesso e registrar métricas")
    void shouldDeactivateCompanySuccessfully() {
        // Given
        addRequiredDataForApproval(); // Adiciona dados obrigatórios
        company.approve(); // Primeiro aprova para poder desativar
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRepository.save(company)).thenReturn(company);
        when(companyMapper.toViewDTO(company)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyCommandService.deactivate(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(CompanyStatusEnum.INACTIVE, company.getStatus());
        
        verify(companyRepository).findById(companyId);
        verify(companyRepository).save(company);
        verify(companyMapper).toViewDTO(company);
        verify(metricsPort).incrementCounter(eq("company_deactivated_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção e registrar métrica ao desativar empresa inexistente")
    void shouldThrowExceptionAndRecordMetricWhenDeactivatingNonExistentCompany() {
        // Given
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            companyCommandService.deactivate(companyId);
        });
        
        assertEquals("Empresa não encontrada: " + companyId, exception.getMessage());
        
        verify(companyRepository).findById(companyId);
        verify(companyRepository, never()).save(any());
        verify(metricsPort).incrementCounter(eq("company_not_found_total"), any());
    }
    
    @Test
    @DisplayName("Deve suspender empresa com sucesso e registrar métricas")
    void shouldSuspendCompanySuccessfully() {
        // Given
        addRequiredDataForApproval(); // Adiciona dados obrigatórios
        company.approve(); // Primeiro aprova para poder suspender
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRepository.save(company)).thenReturn(company);
        when(companyMapper.toViewDTO(company)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyCommandService.suspend(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(CompanyStatusEnum.SUSPENDED, company.getStatus());
        
        verify(companyRepository).findById(companyId);
        verify(companyRepository).save(company);
        verify(companyMapper).toViewDTO(company);
        verify(metricsPort).incrementCounter(eq("company_suspended_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção e registrar métrica ao suspender empresa inexistente")
    void shouldThrowExceptionAndRecordMetricWhenSuspendingNonExistentCompany() {
        // Given
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            companyCommandService.suspend(companyId);
        });
        
        assertEquals("Empresa não encontrada: " + companyId, exception.getMessage());
        
        verify(companyRepository).findById(companyId);
        verify(companyRepository, never()).save(any());
        verify(metricsPort).incrementCounter(eq("company_not_found_total"), any());
    }
    
    @Test
    @DisplayName("Deve bloquear empresa com sucesso e registrar métricas")
    void shouldBlockCompanySuccessfully() {
        // Given
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRepository.save(company)).thenReturn(company);
        when(companyMapper.toViewDTO(company)).thenReturn(companyView);
        
        // When
        CompanyViewDTO result = companyCommandService.block(companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(CompanyStatusEnum.BLOCKED, company.getStatus());
        
        verify(companyRepository).findById(companyId);
        verify(companyRepository).save(company);
        verify(companyMapper).toViewDTO(company);
        verify(metricsPort).incrementCounter(eq("company_blocked_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção e registrar métrica ao bloquear empresa inexistente")
    void shouldThrowExceptionAndRecordMetricWhenBlockingNonExistentCompany() {
        // Given
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            companyCommandService.block(companyId);
        });
        
        assertEquals("Empresa não encontrada: " + companyId, exception.getMessage());
        
        verify(companyRepository).findById(companyId);
        verify(companyRepository, never()).save(any());
        verify(metricsPort).incrementCounter(eq("company_not_found_total"), any());
    }
    
    @Test
    @DisplayName("Deve deletar empresa com sucesso e registrar métricas")
    void shouldDeleteCompanySuccessfully() {
        // Given
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        
        // When
        companyCommandService.delete(companyId);
        
        // Then
        verify(companyRepository).findById(companyId);
        verify(companyRepository).deleteById(companyId);
        verify(metricsPort).incrementCounter(eq("company_deleted_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção e registrar métrica ao deletar empresa inexistente")
    void shouldThrowExceptionAndRecordMetricWhenDeletingNonExistentCompany() {
        // Given
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            companyCommandService.delete(companyId);
        });
        
        assertEquals("Empresa não encontrada: " + companyId, exception.getMessage());
        
        verify(companyRepository).findById(companyId);
        verify(companyRepository, never()).deleteById(any());
        verify(metricsPort).incrementCounter(eq("company_not_found_total"), any());
    }
    
    @Test
    @DisplayName("Deve registrar métrica de erro quando mapper falha")
    void shouldRecordMetricWhenMapperFails() {
        // Given
        CompanyCreateCommandDTO command = CompanyTestBuilder.builder()
            .withName("Nova Empresa")
            .withLegalName("Nova Empresa Ltda")
            .withCnpj("98765432000198")
            .withStateRegistration("987654321")
            .withMunicipalRegistration("123456789")
            .withTaxRegime(TaxRegimeEnum.LUCRO_REAL)
            .buildCreateCommand();
        
        when(companyRepository.existsByCnpj(anyString())).thenReturn(false);
        when(companyMapper.toDomain(command)).thenThrow(new RuntimeException("Erro no mapper"));
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            companyCommandService.create(command);
        });
        
        assertEquals("Erro no mapper", exception.getMessage());
        
        verify(companyRepository).existsByCnpj("98765432000198");
        verify(companyMapper).toDomain(command);
        verify(companyRepository, never()).save(any());
        // Não verifica métricas pois o mapper falha antes de chegar ao serviço
    }
    
    @Test
    @DisplayName("Deve registrar métrica de erro quando repository falha")
    void shouldRecordMetricWhenRepositoryFails() {
        // Given
        CompanyCreateCommandDTO command = CompanyTestBuilder.builder()
            .withName("Nova Empresa")
            .withLegalName("Nova Empresa Ltda")
            .withCnpj("98765432000198")
            .withStateRegistration("987654321")
            .withMunicipalRegistration("123456789")
            .withTaxRegime(TaxRegimeEnum.LUCRO_REAL)
            .buildCreateCommand();
        
        when(companyRepository.existsByCnpj(anyString())).thenReturn(false);
        when(companyMapper.toDomain(command)).thenReturn(company);
        when(companyRepository.save(any(Company.class))).thenThrow(new RuntimeException("Erro no banco de dados"));
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            companyCommandService.create(command);
        });
        
        assertEquals("Erro no banco de dados", exception.getMessage());
        
        verify(companyRepository).existsByCnpj("98765432000198");
        verify(companyMapper).toDomain(command);
        verify(companyRepository).save(company);
        // Não verifica métricas pois o repository falha e a exceção é propagada
    }
    
    // Métodos auxiliares para adicionar dados obrigatórios para aprovação
    private void addRequiredDataForApproval() {
        addAddress();
        addBankAccount();
        addCnae();
        addContact();
        addRepresentative();
    }
    
    private void addAddress() {
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
    
    private void addBankAccount() {
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
    
    private void addCnae() {
        Cnae cnae = Cnae.create(
            "7020400",
            "Atividades de consultoria em gestão empresarial",
            "M",
            "70",
            "70.2",
            "70.20-4",
            "70.20-4/00",
            true, // principal
            companyId
        );
        company.addCnae(cnae);
    }
    
    private void addContact() {
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
    
    private void addRepresentative() {
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
    
    // ==================== TESTES PARA VALIDAÇÃO DE STATUS EM OPERAÇÕES ====================
    
    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar empresa com status BLOCKED")
    void shouldThrowExceptionWhenUpdatingCompanyWithBlockedStatus() {
        // Given - empresa bloqueada
        company.block();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        
        CompanyUpdateCommandDTO updateCommand = CompanyTestBuilder.builder()
            .withName("Empresa Atualizada")
            .withLegalName("Empresa Atualizada Ltda")
            .withStateRegistration("987654321")
            .withMunicipalRegistration("111222333")
            .withTaxRegime(TaxRegimeEnum.LUCRO_REAL)
            .buildUpdateCommand();
        
        // When & Then
        InvalidStatusForOperationException exception = assertThrows(InvalidStatusForOperationException.class, 
            () -> companyCommandService.update(companyId, updateCommand));
        
        assertTrue(exception.getMessage().contains("Não é possível realizar operações na empresa com status 'Bloqueada'"));
        
        verify(companyRepository).findById(companyId);
        verify(companyRepository, never()).save(any());
        verify(companyMapper, never()).toDomain(any(), any());
        verify(companyMapper, never()).toViewDTO(any());
        verify(metricsPort, never()).incrementCounter(eq("company_updated_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar empresa com status SUSPENDED")
    void shouldThrowExceptionWhenUpdatingCompanyWithSuspendedStatus() {
        // Given - empresa suspensa (primeiro aprova para poder suspender)
        addRequiredDataForApproval();
        company.approve();
        company.suspend();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        
        CompanyUpdateCommandDTO updateCommand = CompanyTestBuilder.builder()
            .withName("Empresa Atualizada")
            .withLegalName("Empresa Atualizada Ltda")
            .withStateRegistration("987654321")
            .withMunicipalRegistration("111222333")
            .withTaxRegime(TaxRegimeEnum.LUCRO_REAL)
            .buildUpdateCommand();
        
        // When & Then
        InvalidStatusForOperationException exception = assertThrows(InvalidStatusForOperationException.class, 
            () -> companyCommandService.update(companyId, updateCommand));
        
        assertTrue(exception.getMessage().contains("Não é possível realizar operações na empresa com status 'Suspensa'"));
        
        verify(companyRepository).findById(companyId);
        verify(companyRepository, never()).save(any());
        verify(companyMapper, never()).toDomain(any(), any());
        verify(companyMapper, never()).toViewDTO(any());
        verify(metricsPort, never()).incrementCounter(eq("company_updated_total"), any());
    }
    
    @Test
    @DisplayName("Deve permitir atualização quando empresa está ACTIVE")
    void shouldAllowUpdateWhenCompanyIsActive() {
        // Given - empresa ativa
        addRequiredDataForApproval();
        company.approve();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRepository.save(company)).thenReturn(company);
        when(companyMapper.toDomain(any(), any())).thenReturn(company);
        when(companyMapper.toViewDTO(company)).thenReturn(companyView);
        
        CompanyUpdateCommandDTO updateCommand = CompanyTestBuilder.builder()
            .withName("Empresa Atualizada")
            .withLegalName("Empresa Atualizada Ltda")
            .withStateRegistration("987654321")
            .withMunicipalRegistration("111222333")
            .withTaxRegime(TaxRegimeEnum.LUCRO_REAL)
            .buildUpdateCommand();
        
        // When
        CompanyViewDTO result = companyCommandService.update(companyId, updateCommand);
        
        // Then
        assertNotNull(result);
        verify(companyRepository).findById(companyId);
        verify(companyRepository).save(company);
        verify(companyMapper).toDomain(updateCommand, company);
        verify(companyMapper).toViewDTO(company);
        verify(metricsPort).incrementCounter(eq("company_updated_total"), any());
    }
    
    @Test
    @DisplayName("Deve permitir atualização quando empresa está INACTIVE")
    void shouldAllowUpdateWhenCompanyIsInactive() {
        // Given - empresa inativa
        addRequiredDataForApproval();
        company.approve();
        company.deactivate();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRepository.save(company)).thenReturn(company);
        when(companyMapper.toDomain(any(), any())).thenReturn(company);
        when(companyMapper.toViewDTO(company)).thenReturn(companyView);
        
        CompanyUpdateCommandDTO updateCommand = CompanyTestBuilder.builder()
            .withName("Empresa Atualizada")
            .withLegalName("Empresa Atualizada Ltda")
            .withStateRegistration("987654321")
            .withMunicipalRegistration("111222333")
            .withTaxRegime(TaxRegimeEnum.LUCRO_REAL)
            .buildUpdateCommand();
        
        // When
        CompanyViewDTO result = companyCommandService.update(companyId, updateCommand);
        
        // Then
        assertNotNull(result);
        verify(companyRepository).findById(companyId);
        verify(companyRepository).save(company);
        verify(companyMapper).toDomain(updateCommand, company);
        verify(companyMapper).toViewDTO(company);
        verify(metricsPort).incrementCounter(eq("company_updated_total"), any());
    }
    
    @Test
    @DisplayName("Deve permitir atualização quando empresa está PENDING_APPROVAL")
    void shouldAllowUpdateWhenCompanyIsPendingApproval() {
        // Given - empresa com status PENDING_APPROVAL por padrão
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRepository.save(company)).thenReturn(company);
        when(companyMapper.toDomain(any(), any())).thenReturn(company);
        when(companyMapper.toViewDTO(company)).thenReturn(companyView);
        
        CompanyUpdateCommandDTO updateCommand = CompanyTestBuilder.builder()
            .withName("Empresa Atualizada")
            .withLegalName("Empresa Atualizada Ltda")
            .withStateRegistration("987654321")
            .withMunicipalRegistration("111222333")
            .withTaxRegime(TaxRegimeEnum.LUCRO_REAL)
            .buildUpdateCommand();
        
        // When
        CompanyViewDTO result = companyCommandService.update(companyId, updateCommand);
        
        // Then
        assertNotNull(result);
        verify(companyRepository).findById(companyId);
        verify(companyRepository).save(company);
        verify(companyMapper).toDomain(updateCommand, company);
        verify(companyMapper).toViewDTO(company);
        verify(metricsPort).incrementCounter(eq("company_updated_total"), any());
    }
}