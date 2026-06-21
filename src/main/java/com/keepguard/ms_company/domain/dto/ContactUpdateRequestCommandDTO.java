package com.keepguard.ms_company.domain.dto;

import java.util.UUID;

public record ContactUpdateRequestCommandDTO(
    UUID id,
    String name,
    String email,
    String phone,
    String website,
    String position,
    String department
) {

    public ContactUpdateRequestCommandDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID é obrigatório");
        }
        // Validações opcionais para update - campos podem ser nulos para não alterar
        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser vazio");
        }
        if (email != null && email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email não pode ser vazio");
        }
        if (phone != null && phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Telefone não pode ser vazio");
        }
    }
}
