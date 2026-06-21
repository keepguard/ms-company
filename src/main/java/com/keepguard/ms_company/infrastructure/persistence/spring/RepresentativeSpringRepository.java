package com.keepguard.ms_company.infrastructure.persistence.spring;

import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyRepresentativeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepresentativeSpringRepository extends JpaRepository<CompanyRepresentativeJpaEntity, UUID> {

    List<CompanyRepresentativeJpaEntity> findByCompanyId(UUID companyId);

    Optional<CompanyRepresentativeJpaEntity> findFirstByCompanyIdAndActiveTrue(UUID companyId);

    List<CompanyRepresentativeJpaEntity> findByActiveTrue();

    boolean existsByCompanyIdAndCpf(UUID companyId, String cpf);

    Optional<CompanyRepresentativeJpaEntity> findByCompanyIdAndCpf(UUID companyId, String cpf);

    Optional<CompanyRepresentativeJpaEntity> findByCpf(String cpf);

    Optional<CompanyRepresentativeJpaEntity> findByEmail(String email);

    List<CompanyRepresentativeJpaEntity> findByNameContainingIgnoreCase(String name);

    List<CompanyRepresentativeJpaEntity> findByRoleContainingIgnoreCase(String role);

    long countByCompanyIdAndActiveTrue(UUID companyId);

    @Query("SELECT r.company.id FROM CompanyRepresentativeJpaEntity r WHERE r.id = :representativeId")
    Optional<UUID> findCompanyIdByRepresentativeId(@Param("representativeId") UUID representativeId);
}
