package com.keepguard.ms_company.infrastructure.persistence;

import com.keepguard.ms_company.domain.entity.Cnae;
import com.keepguard.ms_company.application.port.out.persistence.CnaeRepositoryPort;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyCnaeJpaEntity;
import com.keepguard.ms_company.infrastructure.persistence.mapper.CnaeJpaMapper;
import com.keepguard.ms_company.infrastructure.persistence.spring.CnaeSpringRepository;
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
public class CnaeRepositoryAdapter implements CnaeRepositoryPort {

    private final CnaeSpringRepository springRepository;
    private final CnaeJpaMapper mapper;

    @Override
    public Cnae save(Cnae cnae) {
        var entity = mapper.toEntity(cnae, cnae.getCompanyId());
        var savedEntity = springRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    public Cnae save(Cnae cnae, UUID companyId) {
        CompanyCnaeJpaEntity entity = mapper.toEntity(cnae, companyId);
        CompanyCnaeJpaEntity savedEntity = springRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Cnae> findById(UUID id) {
        return springRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Cnae> findAll() {
        return springRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        springRepository.deleteById(id);
    }

    @Override
    public void delete(Cnae cnae) {
        CompanyCnaeJpaEntity entity = mapper.toEntity(cnae);
        springRepository.delete(entity);
    }

    @Override
    public List<Cnae> findByCompanyId(UUID companyId) {
        return springRepository.findByCompanyId(companyId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Cnae> findPrincipalByCompanyId(UUID companyId) {
        return springRepository.findPrincipalByCompanyId(companyId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Cnae> findActiveByCompanyId(UUID companyId) {
        return springRepository.findByCompanyIdAndActiveTrue(companyId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Cnae> findAllActive() {
        return springRepository.findByActiveTrue().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(UUID id) {
        return springRepository.existsById(id);
    }

    @Override
    public boolean existsByCompanyIdAndCode(UUID companyId, String code) {
        return springRepository.existsByCompanyIdAndCode(companyId, code);
    }

    @Override
    public Optional<Cnae> findByCompanyIdAndCode(UUID companyId, String code) {
        return springRepository.findByCompanyIdAndCode(companyId, code)
                .map(mapper::toDomain);
    }

    @Override
    public long countActiveByCompanyId(UUID companyId) {
        return springRepository.countByCompanyIdAndActiveTrue(companyId);
    }

    public Optional<Cnae> findByCode(String code) {
        return springRepository.findByCode(code)
                .map(mapper::toDomain);
    }

    public List<Cnae> findByActiveTrue() {
        return springRepository.findByActiveTrue().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    public long countByActiveTrue() {
        return springRepository.countByActiveTrue();
    }
}
