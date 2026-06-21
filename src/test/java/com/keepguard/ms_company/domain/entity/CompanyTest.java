package com.keepguard.ms_company.domain.entity;

import com.keepguard.lib_common.exception.ValidationException;
import com.keepguard.ms_company.application.service.exception.InvalidStatusForOperationException;
import com.keepguard.ms_company.domain.enums.AccountTypeEnum;
import com.keepguard.ms_company.domain.enums.CompanyStatusEnum;
import com.keepguard.ms_company.domain.enums.TaxRegimeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a entidade Company
 */
class CompanyTest {
    
    private Company company;
    private UUID companyId;
    private UUID codeCompany;
    
    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        codeCompany = UUID.randomUUID();
        
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
    @DisplayName("Deve criar empresa com dados válidos")
    void shouldCreateCompanyWithValidData() {
        // Then
        assertNotNull(company.getId());
        assertNotNull(company.getCodeCompany());
        assertNotNull(company.getXApplication());
        assertEquals("Empresa Teste", company.getName());
        assertEquals("Empresa Teste Ltda", company.getLegalName());
        assertEquals("11222333000181", company.getCnpj());
        assertEquals("123456789", company.getStateRegistration());
        assertEquals("987654321", company.getMunicipalRegistration());
        assertEquals(TaxRegimeEnum.SIMPLES_NACIONAL, company.getTaxRegime());
        assertTrue(company.getCnaes().isEmpty());
        assertEquals("123456789", company.getEin());
        assertEquals(CompanyStatusEnum.PENDING_APPROVAL, company.getStatus());
        assertTrue(company.getAddresses().isEmpty());
    }
    
    @Test
    @DisplayName("Deve criar empresa com ID específico")
    void shouldCreateCompanyWithSpecificId() {
        // Given
        UUID xApplication = UUID.randomUUID();
        
        // When
        Company company = Company.of(
                companyId,
                codeCompany,
                xApplication,
                "Empresa Teste",
                "Empresa Teste Ltda",
                "11222333000181",
                "123456789",
                "987654321",
                TaxRegimeEnum.SIMPLES_NACIONAL,
                "123456789",
                CompanyStatusEnum.PENDING_APPROVAL,
                null,
                null
        );
        
        // Then
        assertEquals(companyId, company.getId());
        assertEquals(codeCompany, company.getCodeCompany());
        assertEquals(xApplication, company.getXApplication());
    }
    
    @Test
    @DisplayName("Deve adicionar CNAE principal")
    void shouldAddPrincipalCnae() {
        // Given
        Cnae cnae = Cnae.create(
            "1234567",
            "Desenvolvimento de software",
            "J", "62", "620", "6201", "62015",
            true,
            company.getId()
        );
        
        // When
        company.addCnae(cnae);
        
        // Then
        assertTrue(company.getPrincipalCnae().isPresent());
        assertEquals(cnae.getCode(), company.getPrincipalCnae().get().getCode());
    }
    
    @Test
    @DisplayName("Deve adicionar CNAEs secundários")
    void shouldAddSecondaryCnaes() {
        // Given
        Cnae cnae1 = Cnae.create(
            "1111111",
            "Atividade secundária 1",
            null, null, null, null, null,
            false,
            company.getId()
        );
        Cnae cnae2 = Cnae.create(
            "2222222",
            "Atividade secundária 2",
            null, null, null, null, null,
            false,
            company.getId()
        );
        
        // When
        company.addCnae(cnae1);
        company.addCnae(cnae2);
        
        // Then
        assertEquals(2, company.getSecondaryCnaes().size());
        assertTrue(company.getSecondaryCnaes().contains(cnae1));
        assertTrue(company.getSecondaryCnaes().contains(cnae2));
    }
    
    @Test
    @DisplayName("Deve definir CNAE como principal")
    void shouldSetCnaeAsPrincipal() {
        // Given
        Cnae cnae1 = Cnae.create(
            "1111111",
            "Atividade principal",
            null, null, null, null, null,
            false,
            company.getId()
        );
        Cnae cnae2 = Cnae.create(
            "2222222",
            "Nova atividade principal",
            null, null, null, null, null,
            false,
            company.getId()
        );
        
        company.addCnae(cnae1);
        company.addCnae(cnae2);
        
        // When
        company.setPrincipalCnae(cnae2.getId());
        
        // Then
        assertTrue(cnae2.isPrincipal());
        assertFalse(cnae1.isPrincipal());
        assertEquals(cnae2.getId(), company.getPrincipalCnae().get().getId());
    }
    
    @Test
    @DisplayName("Deve obter CNAEs ativos")
    void shouldGetActiveCnaes() {
        // Given
        Cnae cnae1 = Cnae.create(
            "1111111",
            "Atividade ativa",
            null, null, null, null, null,
            false,
            company.getId()
        );
        Cnae cnae2 = Cnae.create(
            "2222222",
            "Atividade inativa",
            null, null, null, null, null,
            false,
            company.getId()
        );
        cnae2.deactivate();
        
        company.addCnae(cnae1);
        company.addCnae(cnae2);
        
        // When
        List<Cnae> activeCnaes = company.getActiveCnaes();
        
        // Then
        assertEquals(1, activeCnaes.size());
        assertTrue(activeCnaes.contains(cnae1));
        assertFalse(activeCnaes.contains(cnae2));
    }
    
    @Test
    @DisplayName("Deve remover CNAE")
    void shouldRemoveCnae() {
        // Given
        Cnae cnae = Cnae.create(
            "1111111",
            "Atividade para remover",
            null, null, null, null, null,
            false,
            company.getId()
        );
        company.addCnae(cnae);
        assertEquals(1, company.getCnaes().size());
        
        // When
        company.removeCnae(cnae.getId());
        
        // Then
        assertTrue(company.getCnaes().isEmpty());
    }
    
    @Test
    @DisplayName("Deve aceitar lista vazia de CNAEs")
    void shouldAcceptEmptyCnaesList() {
        // Given & When
        List<Cnae> cnaes = company.getCnaes();
        
        // Then
        assertTrue(cnaes.isEmpty());
    }
    
    @Test
    @DisplayName("Deve aprovar empresa com todos os dados obrigatórios")
    void shouldApproveCompanyWithRequiredData() {
        // Given - adiciona todos os dados obrigatórios
        addRequiredDataForApproval();
        
        // When
        company.approve();
        
        // Then
        assertEquals(CompanyStatusEnum.ACTIVE, company.getStatus());
    }
    
    @Test
    @DisplayName("Deve lançar ValidationException ao tentar aprovar empresa sem endereço ativo")
    void shouldThrowValidationExceptionWhenApprovingCompanyWithoutActiveAddress() {
        // Given - empresa sem endereço ativo
        addBankAccount();
        addCnae();
        addContact();
        addRepresentative();
        
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> company.approve());
        assertEquals("Empresa deve ter pelo menos um endereço ativo para ser aprovada", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar ValidationException ao tentar aprovar empresa sem conta bancária ativa")
    void shouldThrowValidationExceptionWhenApprovingCompanyWithoutActiveBankAccount() {
        // Given - empresa sem conta bancária ativa
        addAddress();
        addCnae();
        addContact();
        addRepresentative();
        
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> company.approve());
        assertEquals("Empresa deve ter pelo menos uma conta bancária ativa para ser aprovada", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar ValidationException ao tentar aprovar empresa sem CNAE ativo e principal")
    void shouldThrowValidationExceptionWhenApprovingCompanyWithoutActivePrincipalCnae() {
        // Given - empresa sem CNAE ativo e principal
        addAddress();
        addBankAccount();
        addContact();
        addRepresentative();
        
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> company.approve());
        assertEquals("Empresa deve ter pelo menos um CNAE ativo e principal para ser aprovada", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar ValidationException ao tentar aprovar empresa sem contato ativo")
    void shouldThrowValidationExceptionWhenApprovingCompanyWithoutActiveContact() {
        // Given - empresa sem contato ativo
        addAddress();
        addBankAccount();
        addCnae();
        addRepresentative();
        
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> company.approve());
        assertEquals("Empresa deve ter pelo menos um contato ativo para ser aprovada", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar ValidationException ao tentar aprovar empresa sem representante ativo")
    void shouldThrowValidationExceptionWhenApprovingCompanyWithoutActiveRepresentative() {
        // Given - empresa sem representante ativo
        addAddress();
        addBankAccount();
        addCnae();
        addContact();
        
        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> company.approve());
        assertEquals("Empresa deve ter pelo menos um representante ativo para ser aprovada", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve rejeitar empresa")
    void shouldRejectCompany() {
        // When
        company.reject();
        
        // Then
        assertEquals(CompanyStatusEnum.BLOCKED, company.getStatus());
    }
    
    @Test
    @DisplayName("Deve ativar empresa")
    void shouldActivateCompany() {
        // Given - empresa está com status PENDING_APPROVAL por padrão, primeiro aprova e depois desativa
        addRequiredDataForApproval(); // Adiciona dados obrigatórios
        company.approve();
        assertEquals(CompanyStatusEnum.ACTIVE, company.getStatus());
        company.deactivate();
        assertEquals(CompanyStatusEnum.INACTIVE, company.getStatus());
        
        // When
        company.activate();
        
        // Then
        assertEquals(CompanyStatusEnum.ACTIVE, company.getStatus());
    }
    
    @Test
    @DisplayName("Deve desativar empresa")
    void shouldDeactivateCompany() {
        // Given
        addRequiredDataForApproval(); // Adiciona dados obrigatórios
        company.approve();
        
        // When
        company.deactivate();
        
        // Then
        assertEquals(CompanyStatusEnum.INACTIVE, company.getStatus());
    }
    
    @Test
    @DisplayName("Deve suspender empresa")
    void shouldSuspendCompany() {
        // Given
        addRequiredDataForApproval(); // Adiciona dados obrigatórios
        company.approve();
        
        // When
        company.suspend();
        
        // Then
        assertEquals(CompanyStatusEnum.SUSPENDED, company.getStatus());
    }
    
    @Test
    @DisplayName("Deve bloquear empresa")
    void shouldBlockCompany() {
        // When
        company.block();
        
        // Then
        assertEquals(CompanyStatusEnum.BLOCKED, company.getStatus());
    }
    
    @Test
    @DisplayName("Deve verificar se empresa está ativa")
    void shouldCheckIfCompanyIsActive() {
        // Given
        addRequiredDataForApproval(); // Adiciona dados obrigatórios
        company.approve();
        
        // When & Then
        assertTrue(company.isActive());
        
        // Given
        company.deactivate();
        
        // When & Then
        assertFalse(company.isActive());
    }
    
    @Test
    @DisplayName("Deve atualizar informações básicas")
    void shouldUpdateBasicInfo() {
        // When
        company.updateBasicInfo(
            "Nome Atualizado",
            "Razão Atualizada",
            "987654321",
            "123456789",
            "987654321"
        );
        
        // Then
        assertEquals("Nome Atualizado", company.getName());
        assertEquals("Razão Atualizada", company.getLegalName());
        assertEquals("987654321", company.getStateRegistration());
        assertEquals("123456789", company.getMunicipalRegistration());
        assertEquals("987654321", company.getEin());
    }
    
    @Test
    @DisplayName("Deve atualizar regime tributário")
    void shouldUpdateTaxRegime() {
        // Given
        TaxRegimeEnum newTaxRegime = TaxRegimeEnum.LUCRO_REAL;
        
        // When
        company.updateTaxRegime(newTaxRegime);
        
        // Then
        assertEquals(newTaxRegime, company.getTaxRegime());
    }
    
    @Test
    @DisplayName("Deve lançar ValidationException quando CNPJ for nulo")
    void shouldThrowValidationExceptionWhenCnpjIsNull() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> Company.create(
                "Empresa Teste",
                "Empresa Teste Ltda",
                null, // CNPJ nulo
                "123456789",
                "987654321",
                TaxRegimeEnum.SIMPLES_NACIONAL,
                "123456789"
            )
        );
        
        assertEquals("CNPJ é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando CNPJ for vazio")
    void shouldThrowValidationExceptionWhenCnpjIsEmpty() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> Company.create(
                "Empresa Teste",
                "Empresa Teste Ltda",
                "", // CNPJ vazio
                "123456789",
                "987654321",
                TaxRegimeEnum.SIMPLES_NACIONAL,
                "123456789"
            )
        );
        
        assertEquals("CNPJ é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve implementar equals corretamente")
    void shouldImplementEqualsCorrectly() {
        Company company1 = Company.create(
                "Empresa Teste", "Empresa Teste Ltda", "11222333000181",
                "123456789", "987654321", TaxRegimeEnum.SIMPLES_NACIONAL,
                "123456789"
        );
        
        Company company2 = Company.create(
                "Empresa Teste", "Empresa Teste Ltda", "11222333000181",
                "123456789", "987654321", TaxRegimeEnum.SIMPLES_NACIONAL,
                "123456789"
        );
        
        Company company3 = Company.create(
                "Outra Empresa", "Outra Empresa Ltda", "98765432000198",
                "987654321", "123456789", TaxRegimeEnum.LUCRO_REAL,
                "987654321"
        );
        
        assertEquals(company1, company1);
        assertNotEquals(company1, company2); // IDs diferentes
        assertNotEquals(company1, company3);
        assertNotEquals(company1, null);
        assertNotEquals(company1, "not a company");
    }
    
    @Test
    @DisplayName("Deve implementar hashCode corretamente")
    void shouldImplementHashCodeCorrectly() {
        Company company1 = Company.create(
                "Empresa Teste", "Empresa Teste Ltda", "11222333000181",
                "123456789", "987654321", TaxRegimeEnum.SIMPLES_NACIONAL,
                "123456789"
        );
        
        Company company2 = Company.create(
                "Empresa Teste", "Empresa Teste Ltda", "11222333000181",
                "123456789", "987654321", TaxRegimeEnum.SIMPLES_NACIONAL,
                "123456789"
        );
        
        // HashCodes devem ser diferentes para IDs diferentes
        assertNotEquals(company1.hashCode(), company2.hashCode());
    }
    
    @Test
    @DisplayName("Deve implementar toString corretamente")
    void shouldImplementToStringCorrectly() {
        String toString = company.toString();
        
        assertTrue(toString.contains("Company"));
        assertTrue(toString.contains("name='Empresa Teste'"));
        assertTrue(toString.contains("legalName='Empresa Teste Ltda'"));
        assertTrue(toString.contains("cnpj='11222333000181'"));
        assertTrue(toString.contains("status=PENDING_APPROVAL"));
    }
    
    // Métodos auxiliares para testes de aprovação
    private void addRequiredDataForApproval() {
        addRequiredDataForApproval(company);
    }
    
    private void addRequiredDataForApproval(Company targetCompany) {
        addAddress(targetCompany);
        addBankAccount(targetCompany);
        addCnae(targetCompany);
        addContact(targetCompany);
        addRepresentative(targetCompany);
    }
    
    private void addAddress() {
        addAddress(company);
    }
    
    private void addAddress(Company targetCompany) {
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
        targetCompany.addAddress(address);
    }
    
    private void addBankAccount() {
        addBankAccount(company);
    }
    
    private void addBankAccount(Company targetCompany) {
        BankAccount bankAccount = BankAccount.create(
            "001",
            "1234",
            "5",
            "12345678",
            "9",
            AccountTypeEnum.CORRENTE
        );
        targetCompany.addBankAccount(bankAccount);
    }
    
    private void addCnae() {
        addCnae(company);
    }
    
    private void addCnae(Company targetCompany) {
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
        targetCompany.addCnae(cnae);
    }
    
    private void addContact() {
        addContact(company);
    }
    
    private void addContact(Company targetCompany) {
        Contact contact = Contact.create(
            "João Silva",
            "joao@empresa.com",
            "11999999999",
            "www.empresa.com",
            "Gerente",
            "Vendas"
        );
        targetCompany.addContact(contact);
    }
    
    private void addRepresentative() {
        addRepresentative(company);
    }
    
    private void addRepresentative(Company targetCompany) {
        Representative representative = Representative.create(
            "Maria Santos",
            "11144477735", // CPF válido
            "123456789",
            LocalDate.of(1980, 1, 1),
            "maria@empresa.com",
            "11988888888",
            "Diretora"
        );
        targetCompany.addRepresentative(representative);
    }
    
    // ==================== TESTES PARA VALIDAÇÃO DE STATUS PARA OPERAÇÕES ====================
    
    @Test
    @DisplayName("Deve verificar se empresa está bloqueada ou suspensa")
    void shouldCheckIfCompanyIsBlockedOrSuspended() {
        // Given - empresa com status PENDING_APPROVAL por padrão
        assertFalse(company.isBlockedOrSuspended());
        
        // When - bloqueia a empresa
        company.block();
        
        // Then
        assertTrue(company.isBlockedOrSuspended());
        
        // When - suspende a empresa (primeiro aprova para poder suspender)
        // Criar nova instância para testar suspensão
        Company companyForSuspension = Company.create(
            "Empresa Teste Suspensão",
            "Empresa Teste Suspensão LTDA",
            "12345678000195",
            "123456789",
            "987654321",
            TaxRegimeEnum.SIMPLES_NACIONAL,
            "123456789"
        );
        addRequiredDataForApproval(companyForSuspension);
        companyForSuspension.approve();
        companyForSuspension.suspend();
        
        // Then
        assertTrue(company.isBlockedOrSuspended());
        assertTrue(companyForSuspension.isBlockedOrSuspended());
    }
    
    @Test
    @DisplayName("Deve permitir operações quando empresa está ACTIVE")
    void shouldAllowOperationsWhenCompanyIsActive() {
        // Given - empresa aprovada e ativa
        addRequiredDataForApproval();
        company.approve();
        assertEquals(CompanyStatusEnum.ACTIVE, company.getStatus());
        
        // When & Then - não deve lançar exceção
        assertDoesNotThrow(() -> company.validateStatusForOperations());
    }
    
    @Test
    @DisplayName("Deve permitir operações quando empresa está INACTIVE")
    void shouldAllowOperationsWhenCompanyIsInactive() {
        // Given - empresa aprovada e depois desativada
        addRequiredDataForApproval();
        company.approve();
        company.deactivate();
        assertEquals(CompanyStatusEnum.INACTIVE, company.getStatus());
        
        // When & Then - não deve lançar exceção
        assertDoesNotThrow(() -> company.validateStatusForOperations());
    }
    
    @Test
    @DisplayName("Deve permitir operações quando empresa está PENDING_APPROVAL")
    void shouldAllowOperationsWhenCompanyIsPendingApproval() {
        // Given - empresa com status PENDING_APPROVAL por padrão
        assertEquals(CompanyStatusEnum.PENDING_APPROVAL, company.getStatus());
        
        // When & Then - não deve lançar exceção
        assertDoesNotThrow(() -> company.validateStatusForOperations());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando empresa está BLOCKED")
    void shouldThrowExceptionWhenCompanyIsBlocked() {
        // Given - empresa bloqueada
        company.block();
        assertEquals(CompanyStatusEnum.BLOCKED, company.getStatus());
        
        // When & Then
        InvalidStatusForOperationException exception = assertThrows(InvalidStatusForOperationException.class, 
            () -> company.validateStatusForOperations());
        
        assertTrue(exception.getMessage().contains("Não é possível realizar operações na empresa com status 'Bloqueada'"));
        assertTrue(exception.getMessage().contains("Operações são permitidas apenas para empresas com status Ativa, Inativa ou Aguardando Aprovação"));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando empresa está SUSPENDED")
    void shouldThrowExceptionWhenCompanyIsSuspended() {
        // Given - empresa suspensa (primeiro aprova para poder suspender)
        addRequiredDataForApproval();
        company.approve();
        company.suspend();
        assertEquals(CompanyStatusEnum.SUSPENDED, company.getStatus());
        
        // When & Then
        InvalidStatusForOperationException exception = assertThrows(InvalidStatusForOperationException.class, 
            () -> company.validateStatusForOperations());
        
        assertTrue(exception.getMessage().contains("Não é possível realizar operações na empresa com status 'Suspensa'"));
        assertTrue(exception.getMessage().contains("Operações são permitidas apenas para empresas com status Ativa, Inativa ou Aguardando Aprovação"));
    }
}
