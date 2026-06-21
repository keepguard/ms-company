package com.keepguard.ms_company.application.dto.company;

import com.keepguard.ms_company.domain.enums.TaxRegimeEnum;

public record CompanyUpdateCommandDTO(
    String name,
    String legalName,
    String stateRegistration,
    String municipalRegistration,
    TaxRegimeEnum taxRegime,
    String ein
) {

    public CompanyUpdateCommandDTO {
        // Validações opcionais para update - campos podem ser nulos para não alterar
        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome fantasia não pode ser vazio");
        }
        if (legalName != null && legalName.trim().isEmpty()) {
            throw new IllegalArgumentException("Razão social não pode ser vazia");
        }
    }
}
