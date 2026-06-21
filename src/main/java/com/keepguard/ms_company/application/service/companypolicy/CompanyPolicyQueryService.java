package com.keepguard.ms_company.application.service.companypolicy;

import com.keepguard.lib_common.logging.annotation.LogOperation;
import com.keepguard.ms_company.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_company.application.dto.companypolicy.CompanyPolicyViewDTO;
import com.keepguard.ms_company.application.dto.companypolicy.GetActiveCompanyPoliciesQueryDTO;
import com.keepguard.ms_company.application.dto.companypolicy.GetCompanyPoliciesQueryDTO;
import com.keepguard.ms_company.application.mapper.CompanyPolicyApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.CompanyPolicyRepositoryPort;
import com.keepguard.ms_company.domain.enums.PolicyStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyPolicyQueryService {

    private final CompanyPolicyRepositoryPort repository;
    private final CompanyPolicyApplicationMapper mapper;
    private final MetricsPort metricsPort;

    @LogOperation(
        operation = "GET_COMPANY_POLICIES",
        description = "Buscando políticas da empresa: {companyId}",
        audit = false
    )
    @Transactional(readOnly = true)
    public List<CompanyPolicyViewDTO> getPolicies(GetCompanyPoliciesQueryDTO query) {
        List<CompanyPolicyViewDTO> policies = repository.findByCompanyId(query.companyId())
            .stream()
            .map(mapper::toView)
            .toList();

        metricsPort.incrementCounter("company_policy_queried_total",
            Map.of("operation", "get_policies", "company_id", query.companyId().toString()));

        return policies;
    }

    @LogOperation(
        operation = "GET_ACTIVE_COMPANY_POLICIES",
        description = "Buscando políticas ativas da empresa: {companyId}",
        audit = false
    )
    @Transactional(readOnly = true)
    public List<CompanyPolicyViewDTO> getActivePolicies(GetActiveCompanyPoliciesQueryDTO query) {
        List<CompanyPolicyViewDTO> policies = repository.findByCompanyIdAndStatus(query.companyId(), PolicyStatusEnum.ACTIVE)
            .stream()
            .map(mapper::toView)
            .toList();

        metricsPort.incrementCounter("company_policy_queried_total",
            Map.of("operation", "get_active_policies", "company_id", query.companyId().toString()));

        return policies;
    }
}
