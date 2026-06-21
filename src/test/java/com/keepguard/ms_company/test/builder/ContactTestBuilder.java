package com.keepguard.ms_company.test.builder;

import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactUpdateDTO;
import com.keepguard.ms_company.application.dto.contact.ContactCreateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactViewDTO;
import com.keepguard.ms_company.domain.entity.Contact;

import java.util.UUID;

/**
 * Builder para criação de dados de teste para Contact
 * Facilita a criação de objetos de teste com dados padrão
 */
public class ContactTestBuilder {
    
    private UUID id = UUID.randomUUID();
    private UUID companyId = UUID.randomUUID();
    private String name = "João Silva";
    private String email = "joao.silva@empresa.com";
    private String phone = "(11) 99999-9999";
    private String website = "https://www.empresa.com";
    private String position = "Gerente";
    private String department = "Vendas";
    private boolean active = true;
    
    public static ContactTestBuilder builder() {
        return new ContactTestBuilder();
    }
    
    public ContactTestBuilder withId(UUID id) {
        this.id = id;
        return this;
    }
    
    public ContactTestBuilder withCompanyId(UUID companyId) {
        this.companyId = companyId;
        return this;
    }
    
    public ContactTestBuilder withName(String name) {
        this.name = name;
        return this;
    }
    
    public ContactTestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
    
    public ContactTestBuilder withPhone(String phone) {
        this.phone = phone;
        return this;
    }
    
    public ContactTestBuilder withWebsite(String website) {
        this.website = website;
        return this;
    }
    
    public ContactTestBuilder withPosition(String position) {
        this.position = position;
        return this;
    }
    
    public ContactTestBuilder withDepartment(String department) {
        this.department = department;
        return this;
    }
    
    public ContactTestBuilder withActive(boolean active) {
        this.active = active;
        return this;
    }
    
    public ContactTestBuilder inactive() {
        this.active = false;
        return this;
    }
    
    public Contact build() {
        return Contact.of(id, name, email, phone, website, position, department, active);
    }
    
    public ContactCreateCommandDTO buildCreateCommand() {
        return new ContactCreateCommandDTO(name, email, phone, website, position, department);
    }
    
    public ContactUpdateCommandDTO buildUpdateCommand() {
        return new ContactUpdateCommandDTO(name, email, phone, website, position, department);
    }
    
    public ContactViewDTO buildView() {
        return new ContactViewDTO(id, companyId, name, email, phone, website, position, department, active);
    }
    
    public ContactCreateDTO buildCreateDTO() {
        return ContactCreateDTO.builder()
            .name(name)
            .email(email)
            .phone(phone)
            .website(website)
            .position(position)
            .department(department)
            .build();
    }
    
    public ContactUpdateDTO buildUpdateDTO() {
        return ContactUpdateDTO.builder()
            .name(name)
            .email(email)
            .phone(phone)
            .website(website)
            .position(position)
            .department(department)
            .build();
    }
    
    public ContactResponseDTO buildResponseDTO() {
        return ContactResponseDTO.builder()
            .id(id)
            .companyId(companyId)
            .name(name)
            .email(email)
            .phone(phone)
            .website(website)
            .position(position)
            .department(department)
            .active(active)
            .build();
    }
}
