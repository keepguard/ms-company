package com.keepguard.ms_company.application.mapper;

import com.keepguard.ms_company.application.dto.companypolicy.CreateCompanyPolicyCommandDTO;
import com.keepguard.ms_company.application.dto.companypolicy.CompanyPolicyViewDTO;
import com.keepguard.ms_company.domain.entity.CompanyPolicy;
import org.springframework.stereotype.Component;

@Component
public class CompanyPolicyApplicationMapper {

    public CompanyPolicy toDomain(CreateCompanyPolicyCommandDTO command) {
        if (command == null) {
            return null;
        }

        return CompanyPolicy.create(
            command.companyId(),
            command.code(),
            command.description(),
            command.status(),
            command.effectiveFrom(),
            command.effectiveTo(),
            command.createdBy()
        );
    }

    public CompanyPolicyViewDTO toView(CompanyPolicy policy) {
        if (policy == null) {
            return null;
        }

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
