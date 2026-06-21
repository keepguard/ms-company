package com.keepguard.ms_company.application.dto.contact;

import java.util.UUID;

public record ContactViewDTO(
    UUID id,
    UUID companyId,
    String name,
    String email,
    String phone,
    String website,
    String position,
    String department,
    boolean active
) {

    public ContactViewDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID é obrigatório");
        }
        // companyId pode ser null quando o contato é retornado como parte de uma Company
        // Validação removida para permitir uso interno no CompanyMapper
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
