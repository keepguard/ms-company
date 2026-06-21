package com.keepguard.ms_company.domain.dto;

import java.util.UUID;

public record CnaeDeleteRequestCommandDTO(
    UUID id
) {

    public CnaeDeleteRequestCommandDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID é obrigatório");
        }
    }
}
