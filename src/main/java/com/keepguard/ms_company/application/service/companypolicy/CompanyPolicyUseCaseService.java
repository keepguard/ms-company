package com.keepguard.ms_company.application.service.companypolicy;

import com.keepguard.ms_company.application.dto.companypolicy.CompanyPolicyViewDTO;
import com.keepguard.ms_company.application.dto.companypolicy.CreateCompanyPolicyCommandDTO;
import com.keepguard.ms_company.application.dto.companypolicy.DeactivateCompanyPolicyCommandDTO;
import com.keepguard.ms_company.application.dto.companypolicy.GetActiveCompanyPoliciesQueryDTO;
import com.keepguard.ms_company.application.dto.companypolicy.GetCompanyPoliciesQueryDTO;
import com.keepguard.ms_company.application.dto.companypolicy.UpdateCompanyPolicyCommandDTO;
import com.keepguard.ms_company.application.port.in.CompanyPolicyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyPolicyUseCaseService implements CompanyPolicyPort {

    private final CompanyPolicyCommandService commandService;
    private final CompanyPolicyQueryService queryService;

    @Override
    public CompanyPolicyViewDTO create(CreateCompanyPolicyCommandDTO command) {
        return commandService.create(command);
    }

    @Override
    public CompanyPolicyViewDTO update(UpdateCompanyPolicyCommandDTO command) {
        return commandService.update(command);
    }

    @Override
    public CompanyPolicyViewDTO deactivate(DeactivateCompanyPolicyCommandDTO command) {
        return commandService.deactivate(command);
    }

    @Override
    public List<CompanyPolicyViewDTO> getPolicies(GetCompanyPoliciesQueryDTO query) {
        return queryService.getPolicies(query);
    }

    @Override
    public List<CompanyPolicyViewDTO> getActivePolicies(GetActiveCompanyPoliciesQueryDTO query) {
        return queryService.getActivePolicies(query);
    }
}
