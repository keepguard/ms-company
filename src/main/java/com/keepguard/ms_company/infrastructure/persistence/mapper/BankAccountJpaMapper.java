package com.keepguard.ms_company.infrastructure.persistence.mapper;

import com.keepguard.ms_company.domain.entity.BankAccount;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyBankAccountJpaEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BankAccountJpaMapper {

    public BankAccount toDomain(CompanyBankAccountJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return BankAccount.of(
            entity.getId(),
            entity.getCode(),
            entity.getAgency(),
            entity.getAgencyDigit(),
            entity.getAccountNumber(),
            entity.getAccountDigit(),
            entity.getAccountType(),
            entity.getActive()
        );
    }

    public CompanyBankAccountJpaEntity toEntity(BankAccount bankAccount) {
        if (bankAccount == null) {
            return null;
        }

        return CompanyBankAccountJpaEntity.builder()
            .id(bankAccount.getId())
            .code(bankAccount.getCode())
            .agency(bankAccount.getAgency())
            .agencyDigit(bankAccount.getAgencyDigit())
            .accountNumber(bankAccount.getAccountNumber())
            .accountDigit(bankAccount.getAccountDigit())
            .accountType(bankAccount.getAccountType())
            .active(bankAccount.isActive())
            .build();
    }

    public CompanyBankAccountJpaEntity toEntity(BankAccount bankAccount, UUID companyId) {
        if (bankAccount == null) {
            return null;
        }

        // Cria uma entidade CompanyJpaEntity temporária apenas com o ID
        var companyEntity = new com.keepguard.ms_company.infrastructure.persistence.entity.CompanyJpaEntity();
        companyEntity.setId(companyId);

        return CompanyBankAccountJpaEntity.builder()
            .id(bankAccount.getId())
            .company(companyEntity)
            .code(bankAccount.getCode())
            .agency(bankAccount.getAgency())
            .agencyDigit(bankAccount.getAgencyDigit())
            .accountNumber(bankAccount.getAccountNumber())
            .accountDigit(bankAccount.getAccountDigit())
            .accountType(bankAccount.getAccountType())
            .active(bankAccount.isActive())
            .build();
    }
}
