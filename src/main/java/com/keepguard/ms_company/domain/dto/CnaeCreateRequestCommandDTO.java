package com.keepguard.ms_company.domain.dto;

import java.util.UUID;

public record CnaeCreateRequestCommandDTO(
    UUID companyId,
    String code,
    String description,
    boolean isMain
) {

    public CnaeCreateRequestCommandDTO {
        if (companyId == null) {
            throw new IllegalArgumentException("ID da empresa é obrigatório");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Código CNAE é obrigatório");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição CNAE é obrigatória");
        }
    }
}
