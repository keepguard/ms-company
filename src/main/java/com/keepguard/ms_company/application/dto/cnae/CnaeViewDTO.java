package com.keepguard.ms_company.application.dto.cnae;

import java.time.LocalDateTime;
import java.util.UUID;

public record CnaeViewDTO(
    UUID id,
    UUID companyId,
    String code,
    String description,
    String section,
    String division,
    String groupCode,
    String classCode,
    String subclassCode,
    boolean active,
    boolean principal,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public CnaeViewDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID é obrigatório");
        }
        // companyId pode ser null quando o CNAE é retornado como parte de uma Company
        // Validação removida para permitir uso interno no CompanyMapper
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Código CNAE é obrigatório");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição do CNAE é obrigatória");
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
