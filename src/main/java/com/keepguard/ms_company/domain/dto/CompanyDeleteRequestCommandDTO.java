package com.keepguard.ms_company.domain.dto;

import java.util.UUID;

public record CompanyDeleteRequestCommandDTO(
    UUID id
) {

    public CompanyDeleteRequestCommandDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID é obrigatório");
        }
    }
}
