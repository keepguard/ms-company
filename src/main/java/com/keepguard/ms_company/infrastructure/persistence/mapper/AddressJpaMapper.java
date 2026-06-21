package com.keepguard.ms_company.infrastructure.persistence.mapper;

import com.keepguard.ms_company.domain.entity.Address;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyAddressJpaEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AddressJpaMapper {

    public Address toDomain(CompanyAddressJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Address.of(
            entity.getId(),
            entity.getStreet(),
            entity.getNumber(),
            entity.getComplement(),
            entity.getDistrict(),
            entity.getCity(),
            entity.getState(),
            entity.getCountry(),
            entity.getZipCode(),
            entity.getActive()
        );
    }

    public CompanyAddressJpaEntity toEntity(Address address) {
        if (address == null) {
            return null;
        }

        return CompanyAddressJpaEntity.builder()
            .id(address.getId())
            .street(address.getStreet())
            .number(address.getNumber())
            .complement(address.getComplement())
            .district(address.getDistrict())
            .city(address.getCity())
            .state(address.getState())
            .country(address.getCountry())
            .zipCode(address.getZipCode())
            .active(address.isActive())
            .build();
    }

    public CompanyAddressJpaEntity toEntity(Address address, UUID companyId) {
        if (address == null) {
            return null;
        }

        // Cria uma entidade CompanyJpaEntity temporária apenas com o ID
        var companyEntity = new com.keepguard.ms_company.infrastructure.persistence.entity.CompanyJpaEntity();
        companyEntity.setId(companyId);

        return CompanyAddressJpaEntity.builder()
            .id(address.getId())
            .company(companyEntity)
            .street(address.getStreet())
            .number(address.getNumber())
            .complement(address.getComplement())
            .district(address.getDistrict())
            .city(address.getCity())
            .state(address.getState())
            .country(address.getCountry())
            .zipCode(address.getZipCode())
            .active(address.isActive())
            .build();
    }
}
