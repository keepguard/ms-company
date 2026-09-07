package com.keepguard.ms_company.application.mapper;

import com.keepguard.ms_company.application.dto.cnae.CnaeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeViewDTO;
import com.keepguard.ms_company.domain.entity.Cnae;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

    public CnaeViewDTO toView(Cnae cnae) {
        return toViewDTO(cnae);
    }
}
