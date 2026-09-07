package com.keepguard.ms_company.application.mapper;

import com.keepguard.ms_company.application.dto.representative.RepresentativeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeViewDTO;
import com.keepguard.ms_company.domain.entity.Representative;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

    public RepresentativeViewDTO toView(Representative representative) {
        return toViewDTO(representative);
    }
}
