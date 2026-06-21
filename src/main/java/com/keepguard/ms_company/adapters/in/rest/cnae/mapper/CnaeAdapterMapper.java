package com.keepguard.ms_company.adapters.in.rest.cnae.mapper;

import com.keepguard.ms_company.adapters.in.rest.cnae.dto.CnaeCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.cnae.dto.CnaeResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.cnae.dto.CnaeUpdateDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeViewDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class CnaeAdapterMapper {

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

    public CnaeResponseDTO toResponseDTO(CnaeViewDTO view) {
        if (view == null) {
            return null;
        }

        try {
            return CnaeResponseDTO.builder()
                .id(view.id())
                .companyId(view.companyId())
                .code(view.code())
                .description(view.description())
                .section(view.section())
                .division(view.division())
                .groupCode(view.groupCode())
                .classCode(view.classCode())
                .subclassCode(view.subclassCode())
                .active(view.active())
                .principal(view.principal())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
        } catch (Exception e) {
            log.error("Erro ao mapear CnaeViewDTO para CnaeResponseDTO: {}", e.getMessage(), e);
            throw e;
        }
    }
}
