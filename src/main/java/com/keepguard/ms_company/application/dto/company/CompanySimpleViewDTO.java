package com.keepguard.ms_company.application.dto.company;

import com.keepguard.ms_company.domain.enums.CompanyStatusEnum;
import com.keepguard.ms_company.domain.enums.TaxRegimeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompanySimpleViewDTO(
    UUID id,
    UUID codeCompany,
    UUID tenantId,
    String name,
    String legalName,
    String cnpj,
    String stateRegistration,
    String municipalRegistration,
    TaxRegimeEnum taxRegime,
    String ein,
    CompanyStatusEnum status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
