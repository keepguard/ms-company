package com.keepguard.ms_company.domain.dto;

import java.util.UUID;

public record CompanyPolicyDeleteRequestCommandDTO(
    UUID id
) {

    public CompanyPolicyDeleteRequestCommandDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID é obrigatório");
        }
    }
}
