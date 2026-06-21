package com.keepguard.ms_company.application.port.out.persistence;

import com.keepguard.ms_company.domain.entity.CompanyPolicy;
import com.keepguard.ms_company.domain.enums.PolicyStatusEnum;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyPolicyRepositoryPort {

    CompanyPolicy save(CompanyPolicy policy);

    Optional<CompanyPolicy> findById(UUID id);

    List<CompanyPolicy> findByCompanyId(UUID companyId);

    List<CompanyPolicy> findByCompanyIdAndStatus(UUID companyId, PolicyStatusEnum status);

    boolean existsByCompanyIdAndCodeAndStatus(UUID companyId, String code, PolicyStatusEnum status);

    Optional<CompanyPolicy> findByCompanyIdAndCodeAndStatus(UUID companyId, String code, PolicyStatusEnum status);

    boolean existsActivePolicyByCompanyId(UUID companyId);
}

