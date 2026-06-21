package com.keepguard.ms_company.adapters.in.rest.representative.mapper;

import com.keepguard.ms_company.adapters.in.rest.representative.dto.RepresentativeCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.representative.dto.RepresentativeResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.representative.dto.RepresentativeUpdateDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.RepresentativeDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeViewDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RepresentativeAdapterMapper {

    public RepresentativeCreateCommandDTO toCreateCommand(RepresentativeCreateDTO dto, UUID companyId) {
        if (dto == null) {
            return null;
        }

        try {
            return new RepresentativeCreateCommandDTO(
                dto.getName(),
                dto.getCpf(),
                dto.getRg(),
                dto.getBirthDate(),
                dto.getEmail(),
                dto.getPhone(),
                dto.getRole(),
                companyId
            );
        } catch (Exception e) {
            log.error("Erro ao mapear RepresentativeCreateDTO para RepresentativeCreateCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public RepresentativeUpdateCommandDTO toUpdateCommand(RepresentativeUpdateDTO dto) {
        if (dto == null) {
            return null;
        }

        try {
            return new RepresentativeUpdateCommandDTO(
                dto.getName(),
                dto.getCpf(),
                dto.getRg(),
                dto.getBirthDate(),
                dto.getEmail(),
                dto.getPhone(),
                dto.getRole()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear RepresentativeUpdateDTO para RepresentativeUpdateCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public RepresentativeResponseDTO toResponseDTO(RepresentativeViewDTO view) {
        if (view == null) {
            return null;
        }

        try {
            return RepresentativeResponseDTO.builder()
                .id(view.id())
                .name(view.name())
                .cpf(view.cpf())
                .rg(view.rg())
                .birthDate(view.birthDate())
                .email(view.email())
                .phone(view.phone())
                .role(view.role())
                .active(view.active())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
        } catch (Exception e) {
            log.error("Erro ao mapear RepresentativeViewDTO para RepresentativeResponseDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public RepresentativeDTO toCompanyRepresentativeDTO(RepresentativeViewDTO view) {
        if (view == null) {
            return null;
        }

        try {
            return RepresentativeDTO.builder()
                .name(view.name())
                .cpf(view.cpf())
                .rg(view.rg())
                .birthDate(view.birthDate())
                .email(view.email())
                .phone(view.phone())
                .role(view.role())
                .build();
        } catch (Exception e) {
            log.error("Erro ao mapear RepresentativeViewDTO para RepresentativeDTO: {}", e.getMessage(), e);
            throw e;
        }
    }
}
