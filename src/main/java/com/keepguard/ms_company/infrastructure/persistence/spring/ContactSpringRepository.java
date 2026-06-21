package com.keepguard.ms_company.infrastructure.persistence.spring;

import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyContactJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContactSpringRepository extends JpaRepository<CompanyContactJpaEntity, UUID>, JpaSpecificationExecutor<CompanyContactJpaEntity> {

    @Query("SELECT c FROM CompanyContactJpaEntity c WHERE c.company.id = :companyId")
    List<CompanyContactJpaEntity> findByCompanyId(@Param("companyId") @NonNull UUID companyId);

    @Query("SELECT c FROM CompanyContactJpaEntity c WHERE c.company.id = :companyId AND c.active = true")
    List<CompanyContactJpaEntity> findActiveByCompanyId(@Param("companyId") @NonNull UUID companyId);

    @Query("SELECT c FROM CompanyContactJpaEntity c WHERE c.active = true")
    List<CompanyContactJpaEntity> findAllActive();

    Optional<CompanyContactJpaEntity> findByEmail(@NonNull String email);

    @Query("SELECT c FROM CompanyContactJpaEntity c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<CompanyContactJpaEntity> findByNameContainingIgnoreCase(@Param("name") @NonNull String name);

    @Query("SELECT c FROM CompanyContactJpaEntity c WHERE LOWER(c.position) LIKE LOWER(CONCAT('%', :position, '%'))")
    List<CompanyContactJpaEntity> findByPositionContainingIgnoreCase(@Param("position") @NonNull String position);

    @Query("SELECT c FROM CompanyContactJpaEntity c WHERE LOWER(c.department) LIKE LOWER(CONCAT('%', :department, '%'))")
    List<CompanyContactJpaEntity> findByDepartmentContainingIgnoreCase(@Param("department") @NonNull String department);

    @Query("SELECT c.company.id FROM CompanyContactJpaEntity c WHERE c.id = :contactId")
    Optional<UUID> findCompanyIdByContactId(@Param("contactId") @NonNull UUID contactId);

    boolean existsByEmail(@NonNull String email);

    @Query("SELECT c FROM CompanyContactJpaEntity c WHERE c.company.id = :companyId")
    Page<CompanyContactJpaEntity> findByCompanyId(@Param("companyId") @NonNull UUID companyId, Pageable pageable);
}
