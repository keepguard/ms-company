package com.keepguard.ms_company.infrastructure.persistence.mapper;

import com.keepguard.ms_company.domain.entity.CompanyPolicy;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyPolicyJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CompanyPolicyJpaMapper {

    public CompanyPolicyJpaEntity toEntity(CompanyPolicy policy) {
        if (policy == null) {
            return null;
        }

        return CompanyPolicyJpaEntity.builder()
                .id(policy.getId())
                .companyId(policy.getCompanyId())
                .code(policy.getCode())
                .description(policy.getDescription())
                .status(policy.getStatus())
                .version(policy.getVersion())
                .effectiveFrom(policy.getEffectiveFrom())
                .effectiveTo(policy.getEffectiveTo())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .createdBy(policy.getCreatedBy())
                .updatedBy(policy.getUpdatedBy())
                .build();
    }

    public CompanyPolicy toDomain(CompanyPolicyJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return CompanyPolicy.of(
                entity.getId(),
                entity.getCompanyId(),
                entity.getCode(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getVersion(),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedBy()
        );
    }
}
