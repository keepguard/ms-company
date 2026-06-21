package com.keepguard.ms_company.infrastructure.persistence;

import com.keepguard.ms_company.application.dto.address.AddressSearchCriteriaDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.port.out.persistence.AddressRepositoryPort;
import com.keepguard.ms_company.domain.entity.Address;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyAddressJpaEntity;
import com.keepguard.ms_company.infrastructure.persistence.mapper.AddressJpaMapper;
import com.keepguard.ms_company.infrastructure.persistence.spring.AddressSpringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;

@Repository
@RequiredArgsConstructor
@Retry(name = "databaseOperation")
@Bulkhead(name = "databaseOperation")
public class AddressRepositoryAdapter implements AddressRepositoryPort {

    private final AddressSpringRepository springRepository;
    private final AddressJpaMapper mapper;

    @Override
    public Address save(Address address) {
        var entity = mapper.toEntity(address);
        var savedEntity = springRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    public Address save(Address address, UUID companyId) {
        CompanyAddressJpaEntity entity = mapper.toEntity(address, companyId);
        CompanyAddressJpaEntity savedEntity = springRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Address> findById(UUID id) {
        return springRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Address> findAll() {
        return springRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        springRepository.deleteById(id);
    }

    @Override
    public void delete(Address address) {
        CompanyAddressJpaEntity entity = mapper.toEntity(address);
        springRepository.delete(entity);
    }

    @Override
    public List<Address> findByCompanyId(UUID companyId) {
        return springRepository.findByCompanyId(companyId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Address> findActiveByCompanyId(UUID companyId) {
        return springRepository.findActiveByCompanyId(companyId)
                .map(mapper::toDomain);
    }

    @Override
    public List<Address> findAllActive() {
        return springRepository.findAllActive().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(UUID id) {
        return springRepository.existsById(id);
    }

    @Override
    public List<Address> findByCityContainingIgnoreCase(String city) {
        String cityPattern = "%" + city + "%";
        return springRepository.findByCityContainingIgnoreCase(cityPattern).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Address> findByState(String state) {
        return springRepository.findByState(state).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Address> findByZipCodeContaining(String zipCode) {
        String zipCodePattern = "%" + zipCode + "%";
        return springRepository.findByZipCodeContaining(zipCodePattern).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public PageResultDTO<Address> search(AddressSearchCriteriaDTO criteria) {
        var pageable = buildPageable(criteria);

        // Prepara os padrões de busca
        String cityPattern = criteria.city() != null ? "%" + criteria.city() + "%" : null;
        String zipCodePattern = criteria.zipCode() != null ? "%" + criteria.zipCode() + "%" : null;

        Page<CompanyAddressJpaEntity> page = springRepository.findByFilters(
            criteria.companyId(),
            criteria.city(),
            cityPattern,
            criteria.state(),
            criteria.zipCode(),
            zipCodePattern,
            criteria.active(),
            pageable
        );

        var addresses = page.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        return new PageResultDTO<>(
                addresses,
                page.getTotalElements(),
                page.getNumber(),
                page.getSize()
        );
    }

    private Pageable buildPageable(AddressSearchCriteriaDTO criteria) {
        Sort sort = Sort.by(Sort.Direction.ASC, "city");

        if (criteria.sortFields() != null && !criteria.sortFields().isEmpty()) {
            Sort.Direction direction = "DESC".equalsIgnoreCase(criteria.sortDirection())
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, criteria.sortFields().toArray(new String[0]));
        }

        return PageRequest.of(criteria.page(), criteria.size(), sort);
    }

    @Override
    public Optional<UUID> findCompanyIdByAddressId(UUID addressId) {
        return springRepository.findCompanyIdByAddressId(addressId);
    }
}
