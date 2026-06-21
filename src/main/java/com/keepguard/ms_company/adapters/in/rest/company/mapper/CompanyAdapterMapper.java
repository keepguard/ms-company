package com.keepguard.ms_company.adapters.in.rest.company.mapper;

import com.keepguard.ms_company.adapters.in.rest.address.mapper.AddressAdapterMapper;
import com.keepguard.ms_company.adapters.in.rest.bankaccount.mapper.BankAccountAdapterMapper;
import com.keepguard.ms_company.adapters.in.rest.cnae.mapper.CnaeAdapterMapper;
import com.keepguard.ms_company.adapters.in.rest.contact.mapper.ContactAdapterMapper;
import com.keepguard.ms_company.adapters.in.rest.representative.mapper.RepresentativeAdapterMapper;
import com.keepguard.ms_company.adapters.in.rest.company.dto.request.CompanyCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.request.CompanyUpdateDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.response.CompanyResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.response.CompanySimpleResponseDTO;
import com.keepguard.ms_company.application.dto.company.CompanyCreateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyViewDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CompanyAdapterMapper {

    private final AddressAdapterMapper addressAdapterMapper;
    private final ContactAdapterMapper contactAdapterMapper;
    private final RepresentativeAdapterMapper representativeAdapterMapper;
    private final BankAccountAdapterMapper bankAccountAdapterMapper;
    private final CnaeAdapterMapper cnaeAdapterMapper;

    public CompanyCreateCommandDTO toCreateCommand(CompanyCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        try {
            return new CompanyCreateCommandDTO(
                dto.getName(),
                dto.getLegalName(),
                dto.getCnpj(),
                dto.getStateRegistration(),
                dto.getMunicipalRegistration(),
                dto.getTaxRegime(),
                dto.getEin()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear CompanyCreateDTO para CompanyCreateCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public CompanyUpdateCommandDTO toUpdateCommand(CompanyUpdateDTO dto) {
        if (dto == null) {
            return null;
        }

        try {
            return new CompanyUpdateCommandDTO(
                dto.getName(),
                dto.getLegalName(),
                dto.getStateRegistration(),
                dto.getMunicipalRegistration(),
                dto.getTaxRegime(),
                dto.getEin()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear CompanyUpdateDTO para CompanyUpdateCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public CompanyResponseDTO toResponseDTO(CompanyViewDTO view) {
        if (view == null) {
            return null;
        }

        try {
            return CompanyResponseDTO.builder()
                .id(view.id())
                .codeCompany(view.codeCompany())
                .xApplication(view.xApplication())
                .name(view.name())
                .legalName(view.legalName())
                .cnpj(view.cnpj())
                .stateRegistration(view.stateRegistration())
                .municipalRegistration(view.municipalRegistration())
                .address(view.address() != null ? addressAdapterMapper.toCompanyAddressDTO(view.address()) : null)
                .contacts(view.contacts() != null ? view.contacts().stream().map(contactAdapterMapper::toCompanyContactDTO).toList() : null)
                .representatives(view.representatives() != null ? view.representatives().stream().map(representativeAdapterMapper::toCompanyRepresentativeDTO).toList() : null)
                .bankAccount(view.bankAccount() != null ? bankAccountAdapterMapper.toCompanyBankAccountDTO(view.bankAccount()) : null)
                .taxRegime(view.taxRegime())
                .cnaes(view.cnaes() != null ? view.cnaes().stream().map(cnaeAdapterMapper::toResponseDTO).toList() : null)
                .ein(view.ein())
                .status(view.status())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
        } catch (Exception e) {
            log.error("Erro ao mapear CompanyViewDTO para CompanyResponseDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public CompanySimpleResponseDTO toSimpleResponseDTO(CompanyViewDTO view) {
        if (view == null) {
            return null;
        }

        try {
            return CompanySimpleResponseDTO.builder()
                .id(view.id())
                .codeCompany(view.codeCompany())
                .xApplication(view.xApplication())
                .name(view.name())
                .legalName(view.legalName())
                .cnpj(view.cnpj())
                .stateRegistration(view.stateRegistration())
                .municipalRegistration(view.municipalRegistration())
                .taxRegime(view.taxRegime())
                .ein(view.ein())
                .status(view.status())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
        } catch (Exception e) {
            log.error("Erro ao mapear CompanyViewDTO para CompanySimpleResponseDTO: {}", e.getMessage(), e);
            throw e;
        }
    }
}
