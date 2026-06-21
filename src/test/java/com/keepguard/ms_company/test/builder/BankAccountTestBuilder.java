package com.keepguard.ms_company.test.builder;

import com.keepguard.ms_company.adapters.in.rest.bankaccount.dto.BankAccountCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.bankaccount.dto.BankAccountResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.bankaccount.dto.BankAccountUpdateDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountCreateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountViewDTO;
import com.keepguard.ms_company.domain.entity.BankAccount;
import com.keepguard.ms_company.domain.enums.AccountTypeEnum;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyBankAccountJpaEntity;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyJpaEntity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Builder para criação de dados de teste para BankAccount
 * Facilita a criação de objetos de teste com dados padrão
 */
public class BankAccountTestBuilder {
    
    private UUID id = UUID.randomUUID();
    private UUID companyId = UUID.randomUUID();
    private String code = "001";
    private String agency = "1234";
    private String agencyDigit = "5";
    private String accountNumber = "12345678";
    private String accountDigit = "9";
    private AccountTypeEnum accountType = AccountTypeEnum.CORRENTE;
    private boolean active = true;
    
    public static BankAccountTestBuilder builder() {
        return new BankAccountTestBuilder();
    }
    
    public BankAccountTestBuilder withId(UUID id) {
        this.id = id;
        return this;
    }
    
    public BankAccountTestBuilder withCompanyId(UUID companyId) {
        this.companyId = companyId;
        return this;
    }
    
    public BankAccountTestBuilder withCode(String code) {
        this.code = code;
        return this;
    }
    
    public BankAccountTestBuilder withAgency(String agency) {
        this.agency = agency;
        return this;
    }
    
    public BankAccountTestBuilder withAgencyDigit(String agencyDigit) {
        this.agencyDigit = agencyDigit;
        return this;
    }
    
    public BankAccountTestBuilder withAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }
    
    public BankAccountTestBuilder withAccountDigit(String accountDigit) {
        this.accountDigit = accountDigit;
        return this;
    }
    
    public BankAccountTestBuilder withAccountType(AccountTypeEnum accountType) {
        this.accountType = accountType;
        return this;
    }
    
    public BankAccountTestBuilder withActive(boolean active) {
        this.active = active;
        return this;
    }
    
    public BankAccountTestBuilder withPoupancaType() {
        this.accountType = AccountTypeEnum.POUPANCA;
        return this;
    }
    
    public BankAccountTestBuilder withCorrenteType() {
        this.accountType = AccountTypeEnum.CORRENTE;
        return this;
    }
    
    public BankAccountTestBuilder inactive() {
        this.active = false;
        return this;
    }
    
    public BankAccountTestBuilder withoutAgencyDigit() {
        this.agencyDigit = null;
        return this;
    }
    
    public BankAccount buildDomain() {
        return BankAccount.of(
            id,
            code,
            agency,
            agencyDigit,
            accountNumber,
            accountDigit,
            accountType,
            active
        );
    }
    
    public BankAccountCreateCommandDTO buildCreateCommand() {
        return new BankAccountCreateCommandDTO(
            code,
            agency,
            agencyDigit,
            accountNumber,
            accountDigit,
            accountType
        );
    }
    
    public BankAccountUpdateCommandDTO buildUpdateCommand() {
        return new BankAccountUpdateCommandDTO(
            code,
            agency,
            agencyDigit,
            accountNumber,
            accountDigit,
            accountType
        );
    }
    
    public BankAccountViewDTO buildView() {
        return new BankAccountViewDTO(
            id,
            companyId,
            code,
            agency,
            agencyDigit,
            accountNumber,
            accountDigit,
            accountType,
            active
        );
    }
    
    public BankAccountCreateDTO buildCreateDTO() {
        BankAccountCreateDTO dto = new BankAccountCreateDTO();
        dto.setCode(code);
        dto.setAgency(agency);
        dto.setAgencyDigit(agencyDigit);
        dto.setAccountNumber(accountNumber);
        dto.setAccountDigit(accountDigit);
        dto.setAccountType(accountType);
        return dto;
    }
    
    public BankAccountUpdateDTO buildUpdateDTO() {
        BankAccountUpdateDTO dto = new BankAccountUpdateDTO();
        dto.setCode(code);
        dto.setAgency(agency);
        dto.setAgencyDigit(agencyDigit);
        dto.setAccountNumber(accountNumber);
        dto.setAccountDigit(accountDigit);
        dto.setAccountType(accountType);
        return dto;
    }
    
    public BankAccountResponseDTO buildResponseDTO() {
        return BankAccountResponseDTO.builder()
            .id(id)
            .companyId(companyId)
            .code(code)
            .agency(agency)
            .agencyDigit(agencyDigit)
            .accountNumber(accountNumber)
            .accountDigit(accountDigit)
            .accountType(accountType)
            .active(active)
            .build();
    }
    
    public CompanyBankAccountJpaEntity buildJpaEntity() {
        CompanyJpaEntity companyEntity = CompanyJpaEntity.builder()
            .id(companyId)
            .name("Empresa Teste")
            .legalName("Empresa Teste Ltda")
            .cnpj("11222333000181")
            .build();
        
        return CompanyBankAccountJpaEntity.builder()
            .id(id)
            .company(companyEntity)
            .code(code)
            .agency(agency)
            .agencyDigit(agencyDigit)
            .accountNumber(accountNumber)
            .accountDigit(accountDigit)
            .accountType(accountType)
            .active(active)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    public CompanyBankAccountJpaEntity buildJpaEntityWithCompany(CompanyJpaEntity company) {
        return CompanyBankAccountJpaEntity.builder()
            .id(id)
            .company(company)
            .code(code)
            .agency(agency)
            .agencyDigit(agencyDigit)
            .accountNumber(accountNumber)
            .accountDigit(accountDigit)
            .accountType(accountType)
            .active(active)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    // Métodos de conveniência para cenários comuns
    public static BankAccountTestBuilder defaultBankAccount() {
        return builder();
    }
    
    public static BankAccountTestBuilder poupancaBankAccount() {
        return builder().withPoupancaType();
    }
    
    public static BankAccountTestBuilder inactiveBankAccount() {
        return builder().inactive();
    }
    
    public static BankAccountTestBuilder bankAccountWithoutAgencyDigit() {
        return builder().withoutAgencyDigit();
    }
    
    public static BankAccountTestBuilder bankAccountWithSpecificId(UUID id) {
        return builder().withId(id);
    }
    
    public static BankAccountTestBuilder bankAccountWithSpecificCompany(UUID companyId) {
        return builder().withCompanyId(companyId);
    }
}
