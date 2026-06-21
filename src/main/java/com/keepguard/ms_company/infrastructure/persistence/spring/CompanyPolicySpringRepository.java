package com.keepguard.ms_company.infrastructure.persistence.spring;

import com.keepguard.ms_company.domain.enums.PolicyStatusEnum;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyPolicyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyPolicySpringRepository extends JpaRepository<CompanyPolicyJpaEntity, UUID> {

    @Query("SELECT p FROM CompanyPolicyJpaEntity p WHERE p.companyId = :companyId")
    List<CompanyPolicyJpaEntity> findByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT p FROM CompanyPolicyJpaEntity p WHERE p.companyId = :companyId AND p.status = :status")
    List<CompanyPolicyJpaEntity> findByCompanyIdAndStatus(@Param("companyId") UUID companyId,
                                                         @Param("status") PolicyStatusEnum status);

    @Query("SELECT COUNT(p) > 0 FROM CompanyPolicyJpaEntity p WHERE p.companyId = :companyId AND p.code = :code AND p.status = :status")
    boolean existsByCompanyIdAndCodeAndStatus(@Param("companyId") UUID companyId,
                                            @Param("code") String code,
                                            @Param("status") PolicyStatusEnum status);

    @Query("SELECT p FROM CompanyPolicyJpaEntity p WHERE p.companyId = :companyId AND p.code = :code AND p.status = :status")
    Optional<CompanyPolicyJpaEntity> findByCompanyIdAndCodeAndStatus(@Param("companyId") UUID companyId,
                                                                    @Param("code") String code,
                                                                    @Param("status") PolicyStatusEnum status);

    @Query("SELECT COUNT(p) > 0 FROM CompanyPolicyJpaEntity p WHERE p.companyId = :companyId AND p.status = 'ACTIVE'")
    boolean existsActivePolicyByCompanyId(@Param("companyId") UUID companyId);
}
