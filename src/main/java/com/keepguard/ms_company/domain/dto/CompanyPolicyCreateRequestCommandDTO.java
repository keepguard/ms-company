package com.keepguard.ms_company.domain.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompanyPolicyCreateRequestCommandDTO(
    UUID companyId,
    String code,
    String description,
    LocalDateTime effectiveFrom,
    LocalDateTime effectiveTo,
    String createdBy
) {

    public CompanyPolicyCreateRequestCommandDTO {
        if (companyId == null) {
            throw new IllegalArgumentException("ID da empresa é obrigatório");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Código da política é obrigatório");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição da política é obrigatória");
        }
        if (effectiveFrom == null) {
            throw new IllegalArgumentException("Data de início é obrigatória");
        }
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Criado por é obrigatório");
        }
    }
}
