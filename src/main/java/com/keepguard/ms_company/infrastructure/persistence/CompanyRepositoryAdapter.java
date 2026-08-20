package com.keepguard.ms_company.infrastructure.persistence;

import com.keepguard.ms_company.application.dto.company.CompanySearchCriteriaDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.port.out.persistence.CompanyRepositoryPort;
import com.keepguard.ms_company.domain.entity.Company;
import com.keepguard.ms_company.domain.enums.CompanyStatusEnum;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyJpaEntity;
import com.keepguard.ms_company.infrastructure.persistence.mapper.CompanyJpaMapper;
import com.keepguard.ms_company.infrastructure.persistence.spring.CompanySpringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
public class CompanyRepositoryAdapter implements CompanyRepositoryPort {

    private final CompanySpringRepository springRepository;
    private final CompanyJpaMapper mapper;

    @Override
    public Company save(Company company) {
        var entity = mapper.toEntity(company);
        var savedEntity = springRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Company> findById(UUID id) {
        return springRepository.findByIdWithRelations(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Company> findAll() {
        return springRepository.findAllWithRelations().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        springRepository.deleteById(id);
    }

    @Override
    public void delete(Company company) {
        CompanyJpaEntity entity = mapper.toEntity(company);
        springRepository.delete(entity);
    }

    @Override
    public Optional<Company> findByCnpj(String cnpj) {
        return springRepository.findByCnpj(cnpj)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Company> findByCodeCompany(UUID codeCompany) {
        return springRepository.findByCodeCompany(codeCompany)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Company> findByTenantId(UUID tenantId) {
        return springRepository.findByTenantId(tenantId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Company> findAllByStatus(CompanyStatusEnum status) {
        return springRepository.findAllByStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public PageResultDTO<Company> search(CompanySearchCriteriaDTO criteria) {
        var spec = buildSpecification(criteria);
        var pageable = buildPageable(criteria);

        var page = springRepository.findAll(spec, pageable);

        var companies = page.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        return new PageResultDTO<>(
                companies,
                page.getTotalElements(),
                page.getNumber(),
                page.getSize()
        );
    }

    @Override
    public boolean existsByCnpj(String cnpj) {
        return springRepository.existsByCnpj(cnpj);
    }

    @Override
    public List<Company> findByLegalNameContainingIgnoreCase(String legalName) {
        return springRepository.findByLegalNameContainingIgnoreCase(legalName).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Company> findByNameContainingIgnoreCase(String name) {
        return springRepository.findByNameContainingIgnoreCase(name).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    private Specification<CompanyJpaEntity> buildSpecification(CompanySearchCriteriaDTO criteria) {
        Specification<CompanyJpaEntity> spec = Specification.where(null);

        if (criteria.name() != null) {
            spec = spec.and(hasNameContaining(criteria.name()));
        }
        if (criteria.legalName() != null) {
            spec = spec.and(hasLegalNameContaining(criteria.legalName()));
        }
        if (criteria.cnpj() != null) {
            spec = spec.and(hasCnpjContaining(criteria.cnpj()));
        }
        if (criteria.city() != null) {
            spec = spec.and(hasCityContaining(criteria.city()));
        }
        if (criteria.state() != null) {
            spec = spec.and(hasState(criteria.state()));
        }
        if (criteria.status() != null) {
            spec = spec.and(hasStatus(criteria.status()));
        }

        return spec;
    }

    private Pageable buildPageable(CompanySearchCriteriaDTO criteria) {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");

        if (criteria.sortFields() != null && !criteria.sortFields().isEmpty()) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(criteria.sortDirection())
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, criteria.sortFields().toArray(new String[0]));
        }

        return PageRequest.of(criteria.page(), criteria.size(), sort);
    }

    private Specification<CompanyJpaEntity> hasNameContaining(String name) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    private Specification<CompanyJpaEntity> hasLegalNameContaining(String legalName) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("legalName")), "%" + legalName.toLowerCase() + "%");
    }

    private Specification<CompanyJpaEntity> hasCnpjContaining(String cnpj) {
        return (root, query, cb) -> cb.like(root.get("cnpj"), "%" + cnpj + "%");
    }

    private Specification<CompanyJpaEntity> hasCityContaining(String city) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("address").get("city")), "%" + city.toLowerCase() + "%");
    }

    private Specification<CompanyJpaEntity> hasState(String state) {
        return (root, query, cb) -> cb.equal(cb.upper(root.get("address").get("state")), state.toUpperCase());
    }

    private Specification<CompanyJpaEntity> hasStatus(CompanyStatusEnum status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public boolean existsById(UUID id) {
        return springRepository.existsById(id);
    }

    public long count() {
        return springRepository.count();
    }
}