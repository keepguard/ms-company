package com.keepguard.ms_company.adapters.in.rest.companypolicy.mapper;

import com.keepguard.ms_company.adapters.in.rest.companypolicy.dto.request.CreateCompanyPolicyRequestDTO;
import com.keepguard.ms_company.adapters.in.rest.companypolicy.dto.request.UpdateCompanyPolicyRequestDTO;
import com.keepguard.ms_company.adapters.in.rest.companypolicy.dto.response.CompanyPolicyResponseDTO;
import com.keepguard.ms_company.application.dto.companypolicy.CompanyPolicyViewDTO;
import com.keepguard.ms_company.application.dto.companypolicy.CreateCompanyPolicyCommandDTO;
import com.keepguard.ms_company.application.dto.companypolicy.DeactivateCompanyPolicyCommandDTO;
import com.keepguard.ms_company.application.dto.companypolicy.GetActiveCompanyPoliciesQueryDTO;
import com.keepguard.ms_company.application.dto.companypolicy.GetCompanyPoliciesQueryDTO;
import com.keepguard.ms_company.application.dto.companypolicy.UpdateCompanyPolicyCommandDTO;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CompanyPolicyAdapterMapper {

    public CreateCompanyPolicyCommandDTO toCreateCommand(UUID companyId, CreateCompanyPolicyRequestDTO request) {
        return new CreateCompanyPolicyCommandDTO(
            companyId,
            request.getCode(),
            request.getDescription(),
            request.getStatus(),
            request.getEffectiveFrom(),
            request.getEffectiveTo(),
            request.getCreatedBy()
        );
    }

    public UpdateCompanyPolicyCommandDTO toUpdateCommand(UUID policyId, UpdateCompanyPolicyRequestDTO request) {
        return new UpdateCompanyPolicyCommandDTO(
            policyId,
            request.getDescription(),
            request.getStatus(),
            request.getEffectiveTo(),
            request.getUpdatedBy()
        );
    }

    public DeactivateCompanyPolicyCommandDTO toDeactivateCommand(UUID policyId, String updatedBy) {
        return new DeactivateCompanyPolicyCommandDTO(policyId, updatedBy);
    }

    public GetCompanyPoliciesQueryDTO toGetPoliciesQuery(UUID companyId) {
        return new GetCompanyPoliciesQueryDTO(companyId);
    }

    public GetActiveCompanyPoliciesQueryDTO toGetActivePoliciesQuery(UUID companyId) {
        return new GetActiveCompanyPoliciesQueryDTO(companyId);
    }

    public CompanyPolicyResponseDTO toResponseDTO(CompanyPolicyViewDTO view) {
        if (view == null) {
            return null;
        }
        return CompanyPolicyResponseDTO.builder()
            .id(view.id())
            .companyId(view.companyId())
            .code(view.code())
            .description(view.description())
            .status(view.status())
            .version(view.version())
            .effectiveFrom(view.effectiveFrom())
            .effectiveTo(view.effectiveTo())
            .createdAt(view.createdAt())
            .updatedAt(view.updatedAt())
            .createdBy(view.createdBy())
            .updatedBy(view.updatedBy())
            .build();
    }
}
