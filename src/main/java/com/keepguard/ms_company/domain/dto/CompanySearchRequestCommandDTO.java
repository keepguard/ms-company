package com.keepguard.ms_company.domain.dto;

import com.keepguard.ms_company.domain.enums.CompanyStatusEnum;

import java.util.List;

public record CompanySearchRequestCommandDTO(
    String name,
    String legalName,
    String cnpj,
    String city,
    String state,
    CompanyStatusEnum status,
    List<String> taxRegimes,
    int page,
    int size,
    String sortBy,
    String sortDirection
) {

    public CompanySearchRequestCommandDTO {
        if (page < 0) {
            throw new IllegalArgumentException("Página deve ser maior ou igual a 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Tamanho deve ser maior que 0");
        }
        if (size > 100) {
            throw new IllegalArgumentException("Tamanho máximo é 100");
        }
    }
}
