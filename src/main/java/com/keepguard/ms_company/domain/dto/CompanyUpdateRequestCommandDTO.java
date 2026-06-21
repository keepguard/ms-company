package com.keepguard.ms_company.domain.dto;

import com.keepguard.ms_company.domain.enums.TaxRegimeEnum;

import java.util.UUID;

public record CompanyUpdateRequestCommandDTO(
    UUID id,
    String name,
    String legalName,
    String stateRegistration,
    String municipalRegistration,
    TaxRegimeEnum taxRegime,
    String ein
) {

    public CompanyUpdateRequestCommandDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID é obrigatório");
        }
        // Validações opcionais para update - campos podem ser nulos para não alterar
        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome fantasia não pode ser vazio");
        }
        if (legalName != null && legalName.trim().isEmpty()) {
            throw new IllegalArgumentException("Razão social não pode ser vazia");
        }
    }
}
