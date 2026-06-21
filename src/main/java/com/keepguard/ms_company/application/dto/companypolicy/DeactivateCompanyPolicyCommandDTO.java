package com.keepguard.ms_company.application.dto.companypolicy;

import java.util.UUID;

public record DeactivateCompanyPolicyCommandDTO(
    UUID id,
    String updatedBy
) {

    public DeactivateCompanyPolicyCommandDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID da política é obrigatório");
        }
        if (updatedBy == null || updatedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Usuário atualizador é obrigatório");
        }
    }
}
