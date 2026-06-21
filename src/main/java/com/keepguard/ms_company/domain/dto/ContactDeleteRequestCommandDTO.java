package com.keepguard.ms_company.domain.dto;

import java.util.UUID;

public record ContactDeleteRequestCommandDTO(
    UUID id
) {

    public ContactDeleteRequestCommandDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID é obrigatório");
        }
    }
}
