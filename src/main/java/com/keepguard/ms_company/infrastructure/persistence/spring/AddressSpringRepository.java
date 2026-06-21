package com.keepguard.ms_company.infrastructure.persistence.spring;

import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyAddressJpaEntity;
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
public interface AddressSpringRepository extends JpaRepository<CompanyAddressJpaEntity, UUID>, JpaSpecificationExecutor<CompanyAddressJpaEntity> {

    @Query("SELECT a FROM CompanyAddressJpaEntity a WHERE a.company.id = :companyId")
    List<CompanyAddressJpaEntity> findByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT a FROM CompanyAddressJpaEntity a WHERE a.company.id = :companyId AND a.active = true")
    Optional<CompanyAddressJpaEntity> findActiveByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT a FROM CompanyAddressJpaEntity a WHERE a.active = true")
    List<CompanyAddressJpaEntity> findAllActive();

    @Query("SELECT a FROM CompanyAddressJpaEntity a WHERE a.city ILIKE :cityPattern")
    List<CompanyAddressJpaEntity> findByCityContainingIgnoreCase(@Param("cityPattern") String cityPattern);

    @Query("SELECT a FROM CompanyAddressJpaEntity a WHERE a.state = :state")
    List<CompanyAddressJpaEntity> findByState(@Param("state") String state);

    @Query("SELECT a FROM CompanyAddressJpaEntity a WHERE a.zipCode LIKE :zipCodePattern")
    List<CompanyAddressJpaEntity> findByZipCodeContaining(@Param("zipCodePattern") String zipCodePattern);

    boolean existsById(@NonNull UUID id);

    @Query("SELECT a FROM CompanyAddressJpaEntity a WHERE " +
           "(:companyId IS NULL OR a.company.id = :companyId) AND " +
           "(:city IS NULL OR a.city ILIKE :cityPattern) AND " +
           "(:state IS NULL OR a.state = :state) AND " +
           "(:zipCode IS NULL OR a.zipCode LIKE :zipCodePattern) AND " +
           "(:active IS NULL OR a.active = :active)")
    Page<CompanyAddressJpaEntity> findByFilters(
        @Param("companyId") UUID companyId,
        @Param("city") String city,
        @Param("cityPattern") String cityPattern,
        @Param("state") String state,
        @Param("zipCode") String zipCode,
        @Param("zipCodePattern") String zipCodePattern,
        @Param("active") Boolean active,
        Pageable pageable
    );

    @Query("SELECT a.company.id FROM CompanyAddressJpaEntity a WHERE a.id = :addressId")
    Optional<UUID> findCompanyIdByAddressId(@Param("addressId") UUID addressId);
}
