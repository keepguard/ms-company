package com.keepguard.ms_company.domain.dto;

import java.util.UUID;

public record RepresentativeDeleteRequestCommandDTO(
    UUID id
) {

    public RepresentativeDeleteRequestCommandDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID é obrigatório");
        }
    }
}
