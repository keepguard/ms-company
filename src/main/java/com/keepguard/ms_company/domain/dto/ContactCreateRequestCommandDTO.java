package com.keepguard.ms_company.domain.dto;

import java.util.UUID;

public record ContactCreateRequestCommandDTO(
    UUID companyId,
    String name,
    String email,
    String phone,
    String website,
    String position,
    String department
) {

    public ContactCreateRequestCommandDTO {
        if (companyId == null) {
            throw new IllegalArgumentException("ID da empresa é obrigatório");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Telefone é obrigatório");
        }
    }
}
