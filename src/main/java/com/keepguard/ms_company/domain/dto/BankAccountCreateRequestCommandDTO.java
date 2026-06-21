package com.keepguard.ms_company.domain.dto;

import com.keepguard.ms_company.domain.enums.AccountTypeEnum;

import java.util.UUID;

public record BankAccountCreateRequestCommandDTO(
    UUID companyId,
    String bankCode,
    String bankName,
    String agency,
    String accountNumber,
    String accountDigit,
    AccountTypeEnum accountType,
    String holderName,
    String holderDocument
) {

    public BankAccountCreateRequestCommandDTO {
        if (companyId == null) {
            throw new IllegalArgumentException("ID da empresa é obrigatório");
        }
        if (bankCode == null || bankCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Código do banco é obrigatório");
        }
        if (bankName == null || bankName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do banco é obrigatório");
        }
        if (agency == null || agency.trim().isEmpty()) {
            throw new IllegalArgumentException("Agência é obrigatória");
        }
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Número da conta é obrigatório");
        }
        if (accountType == null) {
            throw new IllegalArgumentException("Tipo da conta é obrigatório");
        }
        if (holderName == null || holderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do titular é obrigatório");
        }
        if (holderDocument == null || holderDocument.trim().isEmpty()) {
            throw new IllegalArgumentException("Documento do titular é obrigatório");
        }
    }
}
