package com.keepguard.ms_company.application.dto.companypolicy;

import java.util.UUID;

public record GetCompanyPoliciesQueryDTO(
    UUID companyId
) {

    public GetCompanyPoliciesQueryDTO {
        if (companyId == null) {
            throw new IllegalArgumentException("ID da empresa é obrigatório");
        }
    }
}
