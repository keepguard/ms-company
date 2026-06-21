package com.keepguard.ms_company.domain.dto;

import com.keepguard.ms_company.domain.enums.AccountTypeEnum;

import java.util.UUID;

public record BankAccountUpdateRequestCommandDTO(
    UUID id,
    String bankCode,
    String bankName,
    String agency,
    String accountNumber,
    String accountDigit,
    AccountTypeEnum accountType,
    String holderName,
    String holderDocument
) {

    public BankAccountUpdateRequestCommandDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID é obrigatório");
        }
        // Validações opcionais para update - campos podem ser nulos para não alterar
        if (bankCode != null && bankCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Código do banco não pode ser vazio");
        }
        if (bankName != null && bankName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do banco não pode ser vazio");
        }
        if (agency != null && agency.trim().isEmpty()) {
            throw new IllegalArgumentException("Agência não pode ser vazia");
        }
        if (accountNumber != null && accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Número da conta não pode ser vazio");
        }
        if (holderName != null && holderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do titular não pode ser vazio");
        }
        if (holderDocument != null && holderDocument.trim().isEmpty()) {
            throw new IllegalArgumentException("Documento do titular não pode ser vazio");
        }
    }
}
