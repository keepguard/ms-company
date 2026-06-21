package com.keepguard.ms_company.application.dto.representative;

import java.time.LocalDate;

public record RepresentativeUpdateCommandDTO(
    String name,
    String cpf,
    String rg,
    LocalDate birthDate,
    String email,
    String phone,
    String role
) {

    public RepresentativeUpdateCommandDTO {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF é obrigatório");
        }
        if (birthDate == null) {
            throw new IllegalArgumentException("Data de nascimento é obrigatória");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Telefone é obrigatório");
        }
    }
}
