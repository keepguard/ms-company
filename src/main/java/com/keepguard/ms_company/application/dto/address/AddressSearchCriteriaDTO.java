package com.keepguard.ms_company.application.dto.address;

import java.util.List;
import java.util.UUID;

public record AddressSearchCriteriaDTO(
    UUID companyId,
    String city,
    String state,
    String zipCode,
    Boolean active,
    int page,
    int size,
    List<String> sortFields,
    String sortDirection
) {

    public AddressSearchCriteriaDTO {
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
