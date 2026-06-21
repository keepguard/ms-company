package com.keepguard.ms_company.application.dto.bankaccount;

import com.keepguard.ms_company.domain.enums.AccountTypeEnum;

public record BankAccountCreateCommandDTO(
    String code,
    String agency,
    String agencyDigit,
    String accountNumber,
    String accountDigit,
    AccountTypeEnum accountType
) {

    public BankAccountCreateCommandDTO {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Código do banco é obrigatório");
        }
        if (agency == null || agency.trim().isEmpty()) {
            throw new IllegalArgumentException("Agência é obrigatória");
        }
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Número da conta é obrigatório");
        }
        if (accountDigit == null || accountDigit.trim().isEmpty()) {
            throw new IllegalArgumentException("Dígito da conta é obrigatório");
        }
        if (accountType == null) {
            throw new IllegalArgumentException("Tipo da conta é obrigatório");
        }
    }
}
