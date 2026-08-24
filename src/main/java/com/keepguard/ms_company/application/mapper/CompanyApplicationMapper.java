package com.keepguard.ms_company.application.mapper;

import com.keepguard.ms_company.adapters.in.rest.company.dto.response.CompanySimpleResponseDTO;
import com.keepguard.ms_company.application.dto.company.CompanyCreateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyMfaChannelViewDTO;
import com.keepguard.ms_company.application.dto.company.CompanyUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyViewDTO;
import com.keepguard.ms_company.domain.entity.Company;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class CompanyApplicationMapper {

    public Company toDomain(CompanyCreateCommandDTO command) {
        if (command == null) {
            return null;
        }

        try {
            return Company.create(
                command.name(),
                command.legalName(),
                command.cnpj(),
                command.stateRegistration(),
                command.municipalRegistration(),
                command.taxRegime(),
                command.ein()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear CompanyCreateCommandDTO para Company: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Company toDomain(CompanyUpdateCommandDTO command, Company existingCompany) {
        if (command == null || existingCompany == null) {
            return existingCompany;
        }

        try {
            return Company.of(
                existingCompany.getId(),
                existingCompany.getCodeCompany(),
                existingCompany.getTenantId(),
                command.name() != null ? command.name() : existingCompany.getName(),
                command.legalName() != null ? command.legalName() : existingCompany.getLegalName(),
                existingCompany.getCnpj(), // CNPJ não pode ser alterado
                command.stateRegistration() != null ? command.stateRegistration() : existingCompany.getStateRegistration(),
                command.municipalRegistration() != null ? command.municipalRegistration() : existingCompany.getMunicipalRegistration(),
                command.taxRegime() != null ? command.taxRegime() : existingCompany.getTaxRegime(),
                command.ein() != null ? command.ein() : existingCompany.getEin(),
                existingCompany.getStatus(),
                existingCompany.getCreatedAt(),
                LocalDateTime.now()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear CompanyUpdateCommandDTO para Company: {}", e.getMessage(), e);
            throw e;
        }
    }

    public CompanyViewDTO toViewDTO(Company company) {
        if (company == null) {
            return null;
        }

        try {
            return new CompanyViewDTO(
                company.getId(),
                company.getCodeCompany(),
                company.getTenantId(),
                company.getName(),
                company.getLegalName(),
                company.getCnpj(),
                company.getStateRegistration(),
                company.getMunicipalRegistration(),
                null, // address - será preenchido separadamente
                null, // contacts - será preenchido separadamente
                null, // representatives - será preenchido separadamente
                null, // bankAccount - será preenchido separadamente
                company.getTaxRegime(),
                null, // cnaes - será preenchido separadamente
                company.getMfaChannels() != null ?
                    company.getMfaChannels().stream()
                        .map(ch -> new CompanyMfaChannelViewDTO(ch.getId(), ch.getChannel(), ch.isRequired(), ch.isEnabled()))
                        .toList() : null,
                company.getEin(),
                company.getStatus(),
                company.getCreatedAt(),
                company.getUpdatedAt()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear Company para CompanyViewDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public CompanySimpleResponseDTO toSimpleResponseDTO(CompanyViewDTO viewDTO) {
        if (viewDTO == null) {
            return null;
        }

        try {
            return CompanySimpleResponseDTO.builder()
                .id(viewDTO.id())
                .codeCompany(viewDTO.codeCompany())
                .tenantId(viewDTO.tenantId())
                .name(viewDTO.name())
                .legalName(viewDTO.legalName())
                .cnpj(viewDTO.cnpj())
                .stateRegistration(viewDTO.stateRegistration())
                .municipalRegistration(viewDTO.municipalRegistration())
                .taxRegime(viewDTO.taxRegime())
                .ein(viewDTO.ein())
                .status(viewDTO.status())
                .createdAt(viewDTO.createdAt())
                .updatedAt(viewDTO.updatedAt())
                .build();
        } catch (Exception e) {
            log.error("Erro ao mapear CompanyViewDTO para CompanySimpleResponseDTO: {}", e.getMessage(), e);
            throw e;
        }
    }
}
