package com.keepguard.ms_company.infrastructure.persistence.spring;

import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyBankAccountJpaEntity;
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
public interface BankAccountSpringRepository extends JpaRepository<CompanyBankAccountJpaEntity, UUID>, JpaSpecificationExecutor<CompanyBankAccountJpaEntity> {

    @Query("SELECT b FROM CompanyBankAccountJpaEntity b WHERE b.company.id = :companyId")
    List<CompanyBankAccountJpaEntity> findByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT b FROM CompanyBankAccountJpaEntity b WHERE b.company.id = :companyId AND b.active = true")
    Optional<CompanyBankAccountJpaEntity> findActiveByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT b FROM CompanyBankAccountJpaEntity b WHERE b.active = true")
    List<CompanyBankAccountJpaEntity> findAllActive();

    @Query("SELECT b FROM CompanyBankAccountJpaEntity b WHERE b.code = :bankCode")
    List<CompanyBankAccountJpaEntity> findByBankCode(@Param("bankCode") String bankCode);

    @Query("SELECT b FROM CompanyBankAccountJpaEntity b WHERE b.accountType = :accountType")
    List<CompanyBankAccountJpaEntity> findByAccountType(@Param("accountType") String accountType);

    boolean existsById(@NonNull UUID id);

    @Query("SELECT b FROM CompanyBankAccountJpaEntity b WHERE " +
           "(:companyId IS NULL OR b.company.id = :companyId) AND " +
           "(:bankCode IS NULL OR b.code = :bankCode) AND " +
           "(:accountType IS NULL OR b.accountType = :accountType) AND " +
           "(:active IS NULL OR b.active = :active)")
    Page<CompanyBankAccountJpaEntity> findByFilters(
        @Param("companyId") UUID companyId,
        @Param("bankCode") String bankCode,
        @Param("accountType") String accountType,
        @Param("active") Boolean active,
        Pageable pageable
    );

    @Query("SELECT b.company.id FROM CompanyBankAccountJpaEntity b WHERE b.id = :bankAccountId")
    Optional<UUID> findCompanyIdByBankAccountId(@Param("bankAccountId") UUID bankAccountId);
}
