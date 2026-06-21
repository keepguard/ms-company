package com.keepguard.ms_company.application.dto.cnae;

import java.util.UUID;

public record CnaeCreateCommandDTO(
    String code,
    String description,
    String section,
    String division,
    String groupCode,
    String classCode,
    String subclassCode,
    boolean principal,
    UUID companyId
) {

    public CnaeCreateCommandDTO {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Código CNAE é obrigatório");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição do CNAE é obrigatória");
        }
        if (companyId == null) {
            throw new IllegalArgumentException("ID da empresa é obrigatório");
        }

        // Trim dos campos
        code = code.trim();
        description = description.trim();
        section = section != null ? section.trim() : null;
        division = division != null ? division.trim() : null;
        groupCode = groupCode != null ? groupCode.trim() : null;
        classCode = classCode != null ? classCode.trim() : null;
        subclassCode = subclassCode != null ? subclassCode.trim() : null;
    }
}
