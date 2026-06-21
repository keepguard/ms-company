package com.keepguard.ms_company.adapters.in.rest.contact.mapper;

import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactUpdateDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.ContactDTO;
import com.keepguard.ms_company.application.dto.contact.ContactCreateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactViewDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ContactAdapterMapper {

    public ContactCreateCommandDTO toCreateCommand(ContactCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        try {
            return new ContactCreateCommandDTO(
                dto.getName(),
                dto.getEmail(),
                dto.getPhone(),
                dto.getWebsite(),
                dto.getPosition(),
                dto.getDepartment()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear ContactCreateDTO para ContactCreateCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public ContactUpdateCommandDTO toUpdateCommand(ContactUpdateDTO dto) {
        if (dto == null) {
            return null;
        }

        try {
            return new ContactUpdateCommandDTO(
                dto.getName(),
                dto.getEmail(),
                dto.getPhone(),
                dto.getWebsite(),
                dto.getPosition(),
                dto.getDepartment()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear ContactUpdateDTO para ContactUpdateCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public ContactResponseDTO toResponseDTO(ContactViewDTO view) {
        if (view == null) {
            return null;
        }

        try {
            return ContactResponseDTO.builder()
                .id(view.id())
                .companyId(view.companyId())
                .name(view.name())
                .email(view.email())
                .phone(view.phone())
                .website(view.website())
                .position(view.position())
                .department(view.department())
                .active(view.active())
                .build();
        } catch (Exception e) {
            log.error("Erro ao mapear ContactViewDTO para ContactResponseDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public ContactDTO toCompanyContactDTO(ContactViewDTO view) {
        if (view == null) {
            return null;
        }

        try {
            return ContactDTO.builder()
                .email(view.email())
                .phone(view.phone())
                .website(view.website())
                .build();
        } catch (Exception e) {
            log.error("Erro ao mapear ContactViewDTO para ContactDTO: {}", e.getMessage(), e);
            throw e;
        }
    }
}
