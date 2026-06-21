package com.keepguard.ms_company.domain.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompanyPolicyUpdateRequestCommandDTO(
    UUID id,
    String code,
    String description,
    LocalDateTime effectiveFrom,
    LocalDateTime effectiveTo,
    String updatedBy
) {

    public CompanyPolicyUpdateRequestCommandDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID é obrigatório");
        }
        if (updatedBy == null || updatedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Atualizado por é obrigatório");
        }
        // Validações opcionais para update - campos podem ser nulos para não alterar
        if (code != null && code.trim().isEmpty()) {
            throw new IllegalArgumentException("Código da política não pode ser vazio");
        }
        if (description != null && description.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição da política não pode ser vazia");
        }
    }
}
