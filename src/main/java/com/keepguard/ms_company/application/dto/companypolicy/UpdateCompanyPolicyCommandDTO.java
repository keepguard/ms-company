package com.keepguard.ms_company.application.dto.companypolicy;

import com.keepguard.ms_company.domain.enums.PolicyStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateCompanyPolicyCommandDTO(
    UUID id,
    String description,
    PolicyStatusEnum status,
    LocalDateTime effectiveTo,
    String updatedBy
) {

    public UpdateCompanyPolicyCommandDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID da política é obrigatório");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição da política é obrigatória");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status da política é obrigatório");
        }
        if (updatedBy == null || updatedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Usuário atualizador é obrigatório");
        }
    }
}
