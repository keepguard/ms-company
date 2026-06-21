package com.keepguard.ms_company.domain.dto;

import java.util.UUID;

public record BankAccountDeleteRequestCommandDTO(
    UUID id
) {

    public BankAccountDeleteRequestCommandDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID é obrigatório");
        }
    }
}
