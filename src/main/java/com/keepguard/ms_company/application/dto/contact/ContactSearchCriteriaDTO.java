package com.keepguard.ms_company.application.dto.contact;

import java.util.List;
import java.util.UUID;

public record ContactSearchCriteriaDTO(
    UUID companyId,
    String name,
    String email,
    String position,
    String department,
    Boolean active,
    int page,
    int size,
    List<String> sortFields,
    String sortDirection
) {

    public ContactSearchCriteriaDTO {
        if (page < 0) {
            throw new IllegalArgumentException("Página deve ser maior ou igual a 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Tamanho da página deve ser maior que 0");
        }
        if (size > 100) {
            throw new IllegalArgumentException("Tamanho da página não pode ser maior que 100");
        }
    }
}
