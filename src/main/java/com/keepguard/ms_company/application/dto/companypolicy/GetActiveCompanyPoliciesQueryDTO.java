package com.keepguard.ms_company.application.dto.companypolicy;

import java.util.UUID;

public record GetActiveCompanyPoliciesQueryDTO(
    UUID companyId
) {

    public GetActiveCompanyPoliciesQueryDTO {
        if (companyId == null) {
            throw new IllegalArgumentException("ID da empresa é obrigatório");
        }
    }
}
