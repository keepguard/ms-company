package com.keepguard.ms_company.infrastructure.persistence.spring;

import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyCnaeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CnaeSpringRepository extends JpaRepository<CompanyCnaeJpaEntity, UUID> {

    List<CompanyCnaeJpaEntity> findByCompanyId(UUID companyId);

    @Query("SELECT c FROM CompanyCnaeJpaEntity c WHERE c.company.id = :companyId AND c.principal = true AND c.active = true")
    Optional<CompanyCnaeJpaEntity> findPrincipalByCompanyId(@Param("companyId") UUID companyId);

    List<CompanyCnaeJpaEntity> findByCompanyIdAndActiveTrue(UUID companyId);

    List<CompanyCnaeJpaEntity> findByActiveTrue();

    boolean existsByCompanyIdAndCode(UUID companyId, String code);

    Optional<CompanyCnaeJpaEntity> findByCompanyIdAndCode(UUID companyId, String code);

    long countByCompanyIdAndActiveTrue(UUID companyId);

    @Query("SELECT c FROM CompanyCnaeJpaEntity c WHERE c.company.id = :companyId AND c.principal = true")
    List<CompanyCnaeJpaEntity> findPrincipalsByCompanyId(@Param("companyId") UUID companyId);

    Optional<CompanyCnaeJpaEntity> findByCode(String code);

    long countByActiveTrue();
}
