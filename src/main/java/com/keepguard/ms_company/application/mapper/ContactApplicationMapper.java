package com.keepguard.ms_company.application.mapper;

import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactUpdateDTO;
import com.keepguard.ms_company.application.dto.contact.ContactCreateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactViewDTO;
import com.keepguard.ms_company.domain.entity.Contact;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class ContactApplicationMapper {

    public Contact toDomain(ContactCreateCommandDTO command) {
        if (command == null) {
            return null;
        }

        try {
            return Contact.create(
                command.name(),
                command.email(),
                command.phone(),
                command.website(),
                command.position(),
                command.department()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear ContactCreateCommandDTO para Contact: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Contact toDomain(ContactUpdateCommandDTO command, Contact existingContact) {
        if (command == null || existingContact == null) {
            return null;
        }

        try {
            // Cria um novo contato com os dados atualizados
            return Contact.of(
                existingContact.getId(),
                command.name() != null ? command.name() : existingContact.getName(),
                command.email() != null ? command.email() : existingContact.getEmail(),
                command.phone() != null ? command.phone() : existingContact.getPhone(),
                command.website() != null ? command.website() : existingContact.getWebsite(),
                command.position() != null ? command.position() : existingContact.getPosition(),
                command.department() != null ? command.department() : existingContact.getDepartment(),
                existingContact.isActive()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear ContactUpdateCommandDTO para Contact: {}", e.getMessage(), e);
            throw e;
        }
    }

    public ContactViewDTO toViewDTO(Contact contact) {
        if (contact == null) {
            return null;
        }

        try {
            return new ContactViewDTO(
                contact.getId(),
                null, // companyId - será definido pelo caller quando necessário
                contact.getName(),
                contact.getEmail(),
                contact.getPhone(),
                contact.getWebsite(),
                contact.getPosition(),
                contact.getDepartment(),
                contact.isActive()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear Contact para ContactViewDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public ContactViewDTO toViewDTO(Contact contact, UUID companyId) {
        if (contact == null) {
            return null;
        }

        try {
            return new ContactViewDTO(
                contact.getId(),
                companyId,
                contact.getName(),
                contact.getEmail(),
                contact.getPhone(),
                contact.getWebsite(),
                contact.getPosition(),
                contact.getDepartment(),
                contact.isActive()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear Contact para ContactViewDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

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

    public ContactResponseDTO toResponseDTO(ContactViewDTO viewDTO) {
        if (viewDTO == null) {
            return null;
        }

        try {
            return ContactResponseDTO.builder()
                .id(viewDTO.id())
                .companyId(viewDTO.companyId())
                .name(viewDTO.name())
                .email(viewDTO.email())
                .phone(viewDTO.phone())
                .website(viewDTO.website())
                .position(viewDTO.position())
                .department(viewDTO.department())
                .active(viewDTO.active())
                .build();
        } catch (Exception e) {
            log.error("Erro ao mapear ContactViewDTO para ContactResponseDTO: {}", e.getMessage(), e);
            throw e;
        }
    }
}
