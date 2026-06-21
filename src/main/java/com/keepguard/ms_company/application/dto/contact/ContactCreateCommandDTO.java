package com.keepguard.ms_company.application.dto.contact;

public record ContactCreateCommandDTO(
    String name,
    String email,
    String phone,
    String website,
    String position,
    String department
) {

    public ContactCreateCommandDTO {
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
