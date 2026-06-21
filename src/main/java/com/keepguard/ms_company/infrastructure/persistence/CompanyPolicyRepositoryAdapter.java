package com.keepguard.ms_company.infrastructure.persistence;

import com.keepguard.ms_company.application.port.out.persistence.CompanyPolicyRepositoryPort;
import com.keepguard.ms_company.domain.entity.CompanyPolicy;
import com.keepguard.ms_company.domain.enums.PolicyStatusEnum;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyPolicyJpaEntity;
import com.keepguard.ms_company.infrastructure.persistence.mapper.CompanyPolicyJpaMapper;
import com.keepguard.ms_company.infrastructure.persistence.spring.CompanyPolicySpringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;

@Repository
@RequiredArgsConstructor
@Retry(name = "databaseOperation")
@Bulkhead(name = "databaseOperation")
public class CompanyPolicyRepositoryAdapter implements CompanyPolicyRepositoryPort {

    private final CompanyPolicySpringRepository springRepository;
    private final CompanyPolicyJpaMapper mapper;

    @Override
    public CompanyPolicy save(CompanyPolicy policy) {
        var entity = mapper.toEntity(policy);
        var savedEntity = springRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<CompanyPolicy> findById(UUID id) {
        return springRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<CompanyPolicy> findByCompanyId(UUID companyId) {
        return springRepository.findByCompanyId(companyId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompanyPolicy> findByCompanyIdAndStatus(UUID companyId, PolicyStatusEnum status) {
        return springRepository.findByCompanyIdAndStatus(companyId, status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCompanyIdAndCodeAndStatus(UUID companyId, String code, PolicyStatusEnum status) {
        return springRepository.existsByCompanyIdAndCodeAndStatus(companyId, code, status);
    }

    @Override
    public Optional<CompanyPolicy> findByCompanyIdAndCodeAndStatus(UUID companyId, String code, PolicyStatusEnum status) {
        return springRepository.findByCompanyIdAndCodeAndStatus(companyId, code, status)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsActivePolicyByCompanyId(UUID companyId) {
        return springRepository.existsActivePolicyByCompanyId(companyId);
    }
}
