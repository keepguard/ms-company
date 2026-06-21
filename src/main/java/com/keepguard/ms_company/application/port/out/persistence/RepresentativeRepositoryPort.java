package com.keepguard.ms_company.application.port.out.persistence;

import com.keepguard.ms_company.domain.entity.Representative;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepresentativeRepositoryPort {

    Representative save(Representative representative);

    Representative save(Representative representative, UUID companyId);

    Optional<Representative> findById(UUID id);

    List<Representative> findAll();

    void deleteById(UUID id);

    void delete(Representative representative);

    List<Representative> findByCompanyId(UUID companyId);

    Optional<Representative> findActiveByCompanyId(UUID companyId);

    List<Representative> findAllActive();

    boolean existsById(UUID id);

    boolean existsByCompanyIdAndCpf(UUID companyId, String cpf);

    Optional<Representative> findByCompanyIdAndCpf(UUID companyId, String cpf);

    Optional<Representative> findByCpf(String cpf);

    Optional<Representative> findByEmail(String email);

    List<Representative> findByNameContainingIgnoreCase(String name);

    List<Representative> findByRoleContainingIgnoreCase(String role);

    long countActiveByCompanyId(UUID companyId);

    Optional<UUID> findCompanyIdByRepresentativeId(UUID representativeId);
}

