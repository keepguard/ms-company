package com.keepguard.ms_company.application.port.out.persistence;

import com.keepguard.ms_company.domain.entity.Cnae;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CnaeRepositoryPort {

    Cnae save(Cnae cnae);

    Optional<Cnae> findById(UUID id);

    List<Cnae> findAll();

    void deleteById(UUID id);

    void delete(Cnae cnae);

    List<Cnae> findByCompanyId(UUID companyId);

    Optional<Cnae> findPrincipalByCompanyId(UUID companyId);

    List<Cnae> findActiveByCompanyId(UUID companyId);

    List<Cnae> findAllActive();

    boolean existsById(UUID id);

    boolean existsByCompanyIdAndCode(UUID companyId, String code);

    Optional<Cnae> findByCompanyIdAndCode(UUID companyId, String code);

    long countActiveByCompanyId(UUID companyId);
}

