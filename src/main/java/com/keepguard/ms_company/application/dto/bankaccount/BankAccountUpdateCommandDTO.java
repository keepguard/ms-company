package com.keepguard.ms_company.application.dto.bankaccount;

import com.keepguard.ms_company.domain.enums.AccountTypeEnum;

public record BankAccountUpdateCommandDTO(
    String code,
    String agency,
    String agencyDigit,
    String accountNumber,
    String accountDigit,
    AccountTypeEnum accountType
) {

    public BankAccountUpdateCommandDTO {
        if (code != null && code.trim().isEmpty()) {
            throw new IllegalArgumentException("Código do banco não pode ser vazio");
        }
        if (agency != null && agency.trim().isEmpty()) {
            throw new IllegalArgumentException("Agência não pode ser vazia");
        }
        if (agencyDigit != null && agencyDigit.trim().isEmpty()) {
            throw new IllegalArgumentException("Dígito da agência não pode ser vazio");
        }
        if (accountNumber != null && accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Número da conta não pode ser vazio");
        }
        if (accountDigit != null && accountDigit.trim().isEmpty()) {
            throw new IllegalArgumentException("Dígito da conta não pode ser vazio");
        }
    }
}
