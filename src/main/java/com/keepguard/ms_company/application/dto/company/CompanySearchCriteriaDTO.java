package com.keepguard.ms_company.application.dto.company;

import com.keepguard.ms_company.domain.enums.CompanyStatusEnum;

import java.util.List;

public record CompanySearchCriteriaDTO(
    String name,
    String legalName,
    String cnpj,
    String city,
    String state,
    CompanyStatusEnum status,
    int page,
    int size,
    List<String> sortFields,
    String sortDirection
) {

    public static CompanySearchCriteriaDTO of(int page, int size) {
        return new CompanySearchCriteriaDTO(null, null, null, null, null, null, page, size, null, null);
    }

    public static CompanySearchCriteriaDTO of(String name, String legalName, String cnpj,
                                         String city, String state, CompanyStatusEnum status,
                                         int page, int size) {
        return new CompanySearchCriteriaDTO(name, legalName, cnpj, city, state, status, page, size, null, null);
    }
}