package com.keepguard.ms_company.application.dto.representative;

import java.time.LocalDate;
import java.util.UUID;

public record RepresentativeCreateCommandDTO(
    String name,
    String cpf,
    String rg,
    LocalDate birthDate,
    String email,
    String phone,
    String role,
    UUID companyId
) {

    public RepresentativeCreateCommandDTO {
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
        if (companyId == null) {
            throw new IllegalArgumentException("ID da empresa é obrigatório");
        }
    }
}
