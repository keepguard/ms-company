package com.keepguard.ms_company.application.mapper;

import com.keepguard.ms_company.adapters.in.rest.representative.dto.RepresentativeCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.representative.dto.RepresentativeResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.representative.dto.RepresentativeUpdateDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeViewDTO;
import com.keepguard.ms_company.domain.entity.Representative;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RepresentativeApplicationMapper {

    public Representative toDomain(RepresentativeCreateCommandDTO command) {
        if (command == null) {
            return null;
        }

        try {
            return Representative.create(
                command.name(),
                command.cpf(),
                command.rg(),
                command.birthDate(),
                command.email(),
                command.phone(),
                command.role()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear RepresentativeCreateCommandDTO para Representative: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Representative toDomain(RepresentativeUpdateCommandDTO command, Representative existingRepresentative) {
        if (command == null || existingRepresentative == null) {
            return null;
        }

        try {
            return Representative.of(
                existingRepresentative.getId(),
                command.name() != null ? command.name() : existingRepresentative.getName(),
                command.cpf() != null ? command.cpf() : existingRepresentative.getCpf(),
                command.rg() != null ? command.rg() : existingRepresentative.getRg(),
                command.birthDate() != null ? command.birthDate() : existingRepresentative.getBirthDate(),
                command.email() != null ? command.email() : existingRepresentative.getEmail(),
                command.phone() != null ? command.phone() : existingRepresentative.getPhone(),
                command.role() != null ? command.role() : existingRepresentative.getRole(),
                existingRepresentative.isActive()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear RepresentativeUpdateCommandDTO para Representative: {}", e.getMessage(), e);
            throw e;
        }
    }

    public RepresentativeViewDTO toViewDTO(Representative representative) {
        if (representative == null) {
            return null;
        }

        try {
            return new RepresentativeViewDTO(
                representative.getId(),
                representative.getName(),
                representative.getCpf(),
                representative.getRg(),
                representative.getBirthDate(),
                representative.getEmail(),
                representative.getPhone(),
                representative.getRole(),
                representative.isActive(),
                null, // createdAt - não disponível na entidade de domínio
                null  // updatedAt - não disponível na entidade de domínio
            );
        } catch (Exception e) {
            log.error("Erro ao mapear Representative para RepresentativeViewDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

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

    public RepresentativeViewDTO toView(Representative representative) {
        return toViewDTO(representative);
    }

    public RepresentativeResponseDTO toResponseDTO(RepresentativeViewDTO viewDTO) {
        if (viewDTO == null) {
            return null;
        }

        try {
            return RepresentativeResponseDTO.builder()
                .id(viewDTO.id())
                .name(viewDTO.name())
                .cpf(viewDTO.cpf())
                .rg(viewDTO.rg())
                .birthDate(viewDTO.birthDate())
                .email(viewDTO.email())
                .phone(viewDTO.phone())
                .role(viewDTO.role())
                .active(viewDTO.active())
                .createdAt(viewDTO.createdAt())
                .updatedAt(viewDTO.updatedAt())
                .build();
        } catch (Exception e) {
            log.error("Erro ao mapear RepresentativeViewDTO para RepresentativeResponseDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public RepresentativeResponseDTO toResponseDTO(Representative representative) {
        if (representative == null) {
            return null;
        }

        try {
            return RepresentativeResponseDTO.builder()
                .id(representative.getId())
                .name(representative.getName())
                .cpf(representative.getCpf())
                .rg(representative.getRg())
                .birthDate(representative.getBirthDate())
                .email(representative.getEmail())
                .phone(representative.getPhone())
                .role(representative.getRole())
                .active(representative.isActive())
                .createdAt(null) // não disponível na entidade de domínio
                .updatedAt(null) // não disponível na entidade de domínio
                .build();
        } catch (Exception e) {
            log.error("Erro ao mapear Representative para RepresentativeResponseDTO: {}", e.getMessage(), e);
            throw e;
        }
    }
}
