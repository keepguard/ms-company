package com.keepguard.ms_company.application.dto.companypolicy;

import com.keepguard.ms_company.domain.enums.PolicyStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateCompanyPolicyCommandDTO(
    UUID companyId,
    String code,
    String description,
    PolicyStatusEnum status,
    LocalDateTime effectiveFrom,
    LocalDateTime effectiveTo,
    String createdBy
) {

    public CreateCompanyPolicyCommandDTO {
        if (companyId == null) {
            throw new IllegalArgumentException("ID da empresa é obrigatório");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Código da política é obrigatório");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição da política é obrigatória");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status da política é obrigatório");
        }
        if (effectiveFrom == null) {
            throw new IllegalArgumentException("Data de início de vigência é obrigatória");
        }
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Usuário criador é obrigatório");
        }
    }
}
