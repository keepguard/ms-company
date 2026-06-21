package com.keepguard.ms_company.application.mapper;

import com.keepguard.ms_company.adapters.in.rest.cnae.dto.CnaeCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.cnae.dto.CnaeResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.cnae.dto.CnaeUpdateDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeViewDTO;
import com.keepguard.ms_company.domain.entity.Cnae;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class CnaeApplicationMapper {

    public Cnae toDomain(CnaeCreateCommandDTO command) {
        if (command == null) {
            return null;
        }

        try {
            return Cnae.create(
                command.code(),
                command.description(),
                command.section(),
                command.division(),
                command.groupCode(),
                command.classCode(),
                command.subclassCode(),
                command.principal(),
                command.companyId()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear CnaeCreateCommandDTO para Cnae: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Cnae toDomain(CnaeUpdateCommandDTO command, Cnae existingCnae) {
        if (command == null || existingCnae == null) {
            return null;
        }

        try {
            return Cnae.of(
                existingCnae.getId(),
                command.code() != null ? command.code() : existingCnae.getCode(),
                command.description() != null ? command.description() : existingCnae.getDescription(),
                command.section() != null ? command.section() : existingCnae.getSection(),
                command.division() != null ? command.division() : existingCnae.getDivision(),
                command.groupCode() != null ? command.groupCode() : existingCnae.getGroupCode(),
                command.classCode() != null ? command.classCode() : existingCnae.getClassCode(),
                command.subclassCode() != null ? command.subclassCode() : existingCnae.getSubclassCode(),
                existingCnae.isActive(),
                existingCnae.isPrincipal(),
                existingCnae.getCompanyId(),
                existingCnae.getCreatedAt(),
                existingCnae.getUpdatedAt()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear CnaeUpdateCommandDTO para Cnae: {}", e.getMessage(), e);
            throw e;
        }
    }

    public CnaeViewDTO toViewDTO(Cnae cnae) {
        if (cnae == null) {
            return null;
        }

        try {
            return new CnaeViewDTO(
                cnae.getId(),
                cnae.getCompanyId(),
                cnae.getCode(),
                cnae.getDescription(),
                cnae.getSection(),
                cnae.getDivision(),
                cnae.getGroupCode(),
                cnae.getClassCode(),
                cnae.getSubclassCode(),
                cnae.isActive(),
                cnae.isPrincipal(),
                cnae.getCreatedAt(),
                cnae.getUpdatedAt()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear Cnae para CnaeViewDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public CnaeCreateCommandDTO toCreateCommand(CnaeCreateDTO dto, UUID companyId) {
        if (dto == null) {
            return null;
        }

        try {
            return new CnaeCreateCommandDTO(
                dto.getCode(),
                dto.getDescription(),
                dto.getSection(),
                dto.getDivision(),
                dto.getGroupCode(),
                dto.getClassCode(),
                dto.getSubclassCode(),
                dto.isPrincipal(),
                companyId
            );
        } catch (Exception e) {
            log.error("Erro ao mapear CnaeCreateDTO para CnaeCreateCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public CnaeUpdateCommandDTO toUpdateCommand(CnaeUpdateDTO dto) {
        if (dto == null) {
            return null;
        }

        try {
            return new CnaeUpdateCommandDTO(
                dto.getCode(),
                dto.getDescription(),
                dto.getSection(),
                dto.getDivision(),
                dto.getGroupCode(),
                dto.getClassCode(),
                dto.getSubclassCode()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear CnaeUpdateDTO para CnaeUpdateCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public CnaeViewDTO toView(Cnae cnae) {
        return toViewDTO(cnae);
    }

    public CnaeResponseDTO toResponseDTO(CnaeViewDTO viewDTO) {
        if (viewDTO == null) {
            return null;
        }

        try {
            return CnaeResponseDTO.builder()
                .id(viewDTO.id())
                .companyId(viewDTO.companyId())
                .code(viewDTO.code())
                .description(viewDTO.description())
                .section(viewDTO.section())
                .division(viewDTO.division())
                .groupCode(viewDTO.groupCode())
                .classCode(viewDTO.classCode())
                .subclassCode(viewDTO.subclassCode())
                .active(viewDTO.active())
                .principal(viewDTO.principal())
                .createdAt(viewDTO.createdAt())
                .updatedAt(viewDTO.updatedAt())
                .build();
        } catch (Exception e) {
            log.error("Erro ao mapear CnaeViewDTO para CnaeResponseDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public CnaeResponseDTO toResponseDTO(Cnae cnae) {
        if (cnae == null) {
            return null;
        }

        try {
            return CnaeResponseDTO.builder()
                .id(cnae.getId())
                .companyId(cnae.getCompanyId())
                .code(cnae.getCode())
                .description(cnae.getDescription())
                .section(cnae.getSection())
                .division(cnae.getDivision())
                .groupCode(cnae.getGroupCode())
                .classCode(cnae.getClassCode())
                .subclassCode(cnae.getSubclassCode())
                .active(cnae.isActive())
                .principal(cnae.isPrincipal())
                .createdAt(cnae.getCreatedAt())
                .updatedAt(cnae.getUpdatedAt())
                .build();
        } catch (Exception e) {
            log.error("Erro ao mapear Cnae para CnaeResponseDTO: {}", e.getMessage(), e);
            throw e;
        }
    }
}
