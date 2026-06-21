package com.keepguard.ms_company.application.mapper;

import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressUpdateDTO;
import com.keepguard.ms_company.application.dto.address.AddressCreateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressViewDTO;
import com.keepguard.ms_company.domain.entity.Address;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class AddressApplicationMapper {

    public Address toDomain(AddressCreateCommandDTO command) {
        if (command == null) {
            return null;
        }

        try {
            return Address.create(
                command.street(),
                command.number(),
                command.complement(),
                command.district(),
                command.city(),
                command.state(),
                command.country(),
                command.zipCode()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear AddressCreateCommandDTO para Address: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Address toDomain(AddressUpdateCommandDTO command, Address existingAddress) {
        if (command == null || existingAddress == null) {
            return null;
        }

        try {
            // Cria um novo endereço com os dados atualizados
            return Address.of(
                existingAddress.getId(),
                command.street() != null ? command.street() : existingAddress.getStreet(),
                command.number() != null ? command.number() : existingAddress.getNumber(),
                command.complement() != null ? command.complement() : existingAddress.getComplement(),
                command.district() != null ? command.district() : existingAddress.getDistrict(),
                command.city() != null ? command.city() : existingAddress.getCity(),
                command.state() != null ? command.state() : existingAddress.getState(),
                command.country() != null ? command.country() : existingAddress.getCountry(),
                command.zipCode() != null ? command.zipCode() : existingAddress.getZipCode(),
                existingAddress.isActive()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear AddressUpdateCommandDTO para Address: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AddressViewDTO toViewDTO(Address address) {
        if (address == null) {
            return null;
        }

        try {
            return new AddressViewDTO(
                address.getId(),
                null, // companyId - será definido pelo caller quando necessário
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getDistrict(),
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getZipCode(),
                address.isActive()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear Address para AddressViewDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public AddressViewDTO toViewDTO(Address address, UUID companyId) {
        if (address == null) {
            return null;
        }

        try {
            return new AddressViewDTO(
                address.getId(),
                companyId,
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getDistrict(),
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getZipCode(),
                address.isActive()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear Address para AddressViewDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

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

    public AddressResponseDTO toResponseDTO(AddressViewDTO viewDTO) {
        if (viewDTO == null) {
            return null;
        }

        try {
            return AddressResponseDTO.builder()
                .id(viewDTO.id())
                .companyId(viewDTO.companyId())
                .street(viewDTO.street())
                .number(viewDTO.number())
                .complement(viewDTO.complement())
                .district(viewDTO.district())
                .city(viewDTO.city())
                .state(viewDTO.state())
                .country(viewDTO.country())
                .zipCode(viewDTO.zipCode())
                .active(viewDTO.active())
                .build();
        } catch (Exception e) {
            log.error("Erro ao mapear AddressViewDTO para AddressResponseDTO: {}", e.getMessage(), e);
            throw e;
        }
    }
}
