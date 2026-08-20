package com.keepguard.ms_company.infrastructure.persistence.spring;

import com.keepguard.ms_company.domain.enums.CompanyStatusEnum;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanySpringRepository extends JpaRepository<CompanyJpaEntity, UUID>, JpaSpecificationExecutor<CompanyJpaEntity> {

    @Query("SELECT c FROM CompanyJpaEntity c WHERE c.id = :id")
    Optional<CompanyJpaEntity> findByIdWithRelations(@Param("id") UUID id);

    @Query("SELECT c FROM CompanyJpaEntity c WHERE c.cnpj = :cnpj")
    Optional<CompanyJpaEntity> findByCnpj(@Param("cnpj") String cnpj);

    @Query("SELECT c FROM CompanyJpaEntity c WHERE c.codeCompany = :codeCompany")
    Optional<CompanyJpaEntity> findByCodeCompany(@Param("codeCompany") UUID codeCompany);

    @Query("SELECT c FROM CompanyJpaEntity c WHERE c.tenantId = :tenantId")
    Optional<CompanyJpaEntity> findByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT c FROM CompanyJpaEntity c WHERE c.status = :status")
    List<CompanyJpaEntity> findAllByStatus(@Param("status") CompanyStatusEnum status);

    boolean existsByCnpj(String cnpj);

    boolean existsBytenantId(UUID tenantId);

    @Query("SELECT c FROM CompanyJpaEntity c WHERE LOWER(c.legalName) LIKE LOWER(CONCAT('%', :legalName, '%'))")
    List<CompanyJpaEntity> findByLegalNameContainingIgnoreCase(@Param("legalName") String legalName);

    @Query("SELECT c FROM CompanyJpaEntity c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<CompanyJpaEntity> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT c FROM CompanyJpaEntity c")
    List<CompanyJpaEntity> findAllWithRelations();

    @Query("SELECT DISTINCT c FROM CompanyJpaEntity c LEFT JOIN c.addresses a WHERE " +
           "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:legalName IS NULL OR LOWER(c.legalName) LIKE LOWER(CONCAT('%', :legalName, '%'))) AND " +
           "(:cnpj IS NULL OR c.cnpj LIKE CONCAT('%', :cnpj, '%')) AND " +
           "(:city IS NULL OR LOWER(a.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:state IS NULL OR UPPER(a.state) = UPPER(:state)) AND " +
           "(:status IS NULL OR c.status = :status)")
    List<CompanyJpaEntity> searchWithRelations(@Param("name") String name,
                                             @Param("legalName") String legalName,
                                             @Param("cnpj") String cnpj,
                                             @Param("city") String city,
                                             @Param("state") String state,
                                             @Param("status") CompanyStatusEnum status);

    @Query("SELECT DISTINCT c FROM CompanyJpaEntity c LEFT JOIN c.addresses a WHERE " +
           "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:legalName IS NULL OR LOWER(c.legalName) LIKE LOWER(CONCAT('%', :legalName, '%'))) AND " +
           "(:cnpj IS NULL OR c.cnpj LIKE CONCAT('%', :cnpj, '%')) AND " +
           "(:city IS NULL OR LOWER(a.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:state IS NULL OR UPPER(a.state) = UPPER(:state)) AND " +
           "(:status IS NULL OR c.status = :status)")
    Page<CompanyJpaEntity> findByFilters(@Param("name") String name,
                                         @Param("legalName") String legalName,
                                         @Param("cnpj") String cnpj,
                                         @Param("city") String city,
                                         @Param("state") String state,
                                         @Param("status") CompanyStatusEnum status,
                                         Pageable pageable);
}