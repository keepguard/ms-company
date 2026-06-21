package com.keepguard.ms_company.application.dto.companypolicy;

import com.keepguard.ms_company.domain.entity.CompanyPolicy;
import com.keepguard.ms_company.domain.enums.PolicyStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompanyPolicyViewDTO(
    UUID id,
    UUID companyId,
    String code,
    String description,
    PolicyStatusEnum status,
    Integer version,
    LocalDateTime effectiveFrom,
    LocalDateTime effectiveTo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String createdBy,
    String updatedBy
) {

    public static CompanyPolicyViewDTO from(CompanyPolicy policy) {
        return new CompanyPolicyViewDTO(
            policy.getId(),
            policy.getCompanyId(),
            policy.getCode(),
            policy.getDescription(),
            policy.getStatus(),
            policy.getVersion(),
            policy.getEffectiveFrom(),
            policy.getEffectiveTo(),
            policy.getCreatedAt(),
            policy.getUpdatedAt(),
            policy.getCreatedBy(),
            policy.getUpdatedBy()
        );
    }
}
