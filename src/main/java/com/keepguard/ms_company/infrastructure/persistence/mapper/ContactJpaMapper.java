package com.keepguard.ms_company.infrastructure.persistence.mapper;

import com.keepguard.ms_company.domain.entity.Contact;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyContactJpaEntity;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyJpaEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ContactJpaMapper {

    public Contact toDomain(CompanyContactJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Contact.of(
            entity.getId(),
            entity.getName(),
            entity.getEmail(),
            entity.getPhone(),
            entity.getWebsite(),
            entity.getPosition(),
            entity.getDepartment(),
            entity.getActive()
        );
    }

    public CompanyContactJpaEntity toEntity(Contact contact) {
        if (contact == null) {
            return null;
        }

        return CompanyContactJpaEntity.builder()
            .id(contact.getId())
            .name(contact.getName())
            .email(contact.getEmail())
            .phone(contact.getPhone())
            .website(contact.getWebsite())
            .position(contact.getPosition())
            .department(contact.getDepartment())
            .active(contact.isActive())
            .build();
    }

    public CompanyContactJpaEntity toEntity(Contact contact, UUID companyId) {
        if (contact == null) {
            return null;
        }

        CompanyJpaEntity company = CompanyJpaEntity.builder()
            .id(companyId)
            .build();

        return CompanyContactJpaEntity.builder()
            .id(contact.getId())
            .company(company)
            .name(contact.getName())
            .email(contact.getEmail())
            .phone(contact.getPhone())
            .website(contact.getWebsite())
            .position(contact.getPosition())
            .department(contact.getDepartment())
            .active(contact.isActive())
            .build();
    }
}
