package com.keepguard.ms_company.domain.dto;

import com.keepguard.ms_company.domain.enums.TaxRegimeEnum;

import java.util.UUID;

public record CompanyCreateRequestCommandDTO(
    String name,
    String legalName,
    String cnpj,
    String stateRegistration,
    String municipalRegistration,
    TaxRegimeEnum taxRegime,
    String ein
) {

    public CompanyCreateRequestCommandDTO {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome fantasia é obrigatório");
        }
        if (legalName == null || legalName.trim().isEmpty()) {
            throw new IllegalArgumentException("Razão social é obrigatória");
        }
        if (cnpj == null || cnpj.trim().isEmpty()) {
            throw new IllegalArgumentException("CNPJ é obrigatório");
        }
        if (taxRegime == null) {
            throw new IllegalArgumentException("Regime tributário é obrigatório");
        }
    }
}
