package com.keepguard.ms_company.adapters.in.rest.company.dto.request;

import java.util.List;

public record CompanySearchRequestDTO(
    String name,
    String cnpj,
    String tradeName,
    String taxRegime,
    String status,
    int page,
    int size,
    List<String> sortFields,
    String sortDirection
) {
    public CompanySearchRequestDTO {
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
