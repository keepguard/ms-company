package com.keepguard.ms_company.application.port.in;

import com.keepguard.ms_company.application.dto.companypolicy.CompanyPolicyViewDTO;
import com.keepguard.ms_company.application.dto.companypolicy.CreateCompanyPolicyCommandDTO;
import com.keepguard.ms_company.application.dto.companypolicy.DeactivateCompanyPolicyCommandDTO;
import com.keepguard.ms_company.application.dto.companypolicy.GetActiveCompanyPoliciesQueryDTO;
import com.keepguard.ms_company.application.dto.companypolicy.GetCompanyPoliciesQueryDTO;
import com.keepguard.ms_company.application.dto.companypolicy.UpdateCompanyPolicyCommandDTO;

import java.util.List;

public interface CompanyPolicyPort {

    CompanyPolicyViewDTO create(CreateCompanyPolicyCommandDTO command);

    CompanyPolicyViewDTO update(UpdateCompanyPolicyCommandDTO command);

    CompanyPolicyViewDTO deactivate(DeactivateCompanyPolicyCommandDTO command);

    List<CompanyPolicyViewDTO> getPolicies(GetCompanyPoliciesQueryDTO query);

    List<CompanyPolicyViewDTO> getActivePolicies(GetActiveCompanyPoliciesQueryDTO query);
}
