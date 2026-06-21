package com.keepguard.ms_company.domain.dto;

import java.util.UUID;

public record CnaeUpdateRequestCommandDTO(
    UUID id,
    String code,
    String description,
    Boolean isMain
) {

    public CnaeUpdateRequestCommandDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID é obrigatório");
        }
        // Validações opcionais para update - campos podem ser nulos para não alterar
        if (code != null && code.trim().isEmpty()) {
            throw new IllegalArgumentException("Código CNAE não pode ser vazio");
        }
        if (description != null && description.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição CNAE não pode ser vazia");
        }
    }
}
