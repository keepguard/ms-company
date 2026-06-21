package com.keepguard.ms_company.application.port.out.persistence;

import com.keepguard.ms_company.application.dto.address.AddressSearchCriteriaDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.domain.entity.Address;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepositoryPort {

    Address save(Address address);

    Address save(Address address, UUID companyId);

    Optional<Address> findById(UUID id);

    List<Address> findAll();

    void deleteById(UUID id);

    void delete(Address address);

    List<Address> findByCompanyId(UUID companyId);

    Optional<Address> findActiveByCompanyId(UUID companyId);

    List<Address> findAllActive();

    boolean existsById(UUID id);

    List<Address> findByCityContainingIgnoreCase(String city);

    List<Address> findByState(String state);

    List<Address> findByZipCodeContaining(String zipCode);

    PageResultDTO<Address> search(AddressSearchCriteriaDTO criteria);

    Optional<UUID> findCompanyIdByAddressId(UUID addressId);
}

