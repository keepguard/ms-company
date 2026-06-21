package com.keepguard.ms_company.domain.dto;

import java.util.UUID;

public record AddressDeleteRequestCommandDTO(
    UUID id
) {

    public AddressDeleteRequestCommandDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID é obrigatório");
        }
    }
}
