package com.keepguard.ms_company.domain.dto;

import java.time.LocalDate;
import java.util.UUID;

public record RepresentativeCreateRequestCommandDTO(
    UUID companyId,
    String name,
    String cpf,
    String rg,
    LocalDate birthDate,
    String email,
    String phone,
    String role
) {

    public RepresentativeCreateRequestCommandDTO {
        if (companyId == null) {
            throw new IllegalArgumentException("ID da empresa é obrigatório");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF é obrigatório");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Telefone é obrigatório");
        }
    }
}
