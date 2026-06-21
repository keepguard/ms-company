package com.keepguard.ms_company.application.dto.contact;

public record ContactUpdateCommandDTO(
    String name,
    String email,
    String phone,
    String website,
    String position,
    String department
) {

    public ContactUpdateCommandDTO {
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
