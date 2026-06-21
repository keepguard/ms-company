package com.keepguard.ms_company.adapters.in.rest.address.mapper;

import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressUpdateDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.AddressDTO;
import com.keepguard.ms_company.application.dto.address.AddressCreateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressViewDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AddressAdapterMapper {

    public AddressCreateCommandDTO toCreateCommand(AddressCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        try {
            return new AddressCreateCommandDTO(
                dto.getStreet(),
                dto.getNumber(),
                dto.getComplement(),
                dto.getDistrict(),
                dto.getCity(),
                dto.getState(),
                dto.getCountry(),
                dto.getZipCode()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear AddressCreateDTO para AddressCreateCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AddressUpdateCommandDTO toUpdateCommand(AddressUpdateDTO dto) {
        if (dto == null) {
            return null;
        }

        try {
            return new AddressUpdateCommandDTO(
                dto.getStreet(),
                dto.getNumber(),
                dto.getComplement(),
                dto.getDistrict(),
                dto.getCity(),
                dto.getState(),
                dto.getCountry(),
                dto.getZipCode()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear AddressUpdateDTO para AddressUpdateCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AddressResponseDTO toResponseDTO(AddressViewDTO view) {
        if (view == null) {
            return null;
        }

        try {
            return AddressResponseDTO.builder()
                .id(view.id())
                .companyId(view.companyId())
                .street(view.street())
                .number(view.number())
                .complement(view.complement())
                .district(view.district())
                .city(view.city())
                .state(view.state())
                .country(view.country())
                .zipCode(view.zipCode())
                .active(view.active())
                .build();
        } catch (Exception e) {
            log.error("Erro ao mapear AddressViewDTO para AddressResponseDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AddressDTO toCompanyAddressDTO(AddressViewDTO view) {
        if (view == null) {
            return null;
        }

        try {
            return AddressDTO.builder()
                .street(view.street())
                .number(view.number())
                .complement(view.complement())
                .district(view.district())
                .city(view.city())
                .state(view.state())
                .country(view.country())
                .zipCode(view.zipCode())
                .build();
        } catch (Exception e) {
            log.error("Erro ao mapear AddressViewDTO para AddressDTO: {}", e.getMessage(), e);
            throw e;
        }
    }
}
