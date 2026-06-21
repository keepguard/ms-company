package com.keepguard.ms_company.application.mapper;

import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactUpdateDTO;
import com.keepguard.ms_company.application.dto.contact.ContactCreateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactViewDTO;
import com.keepguard.ms_company.domain.entity.Contact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ContactMapper
 * Testa conversões entre DTOs e entidades
 */
@DisplayName("Contact Application Mapper Tests")
class ContactApplicationMapperTest {
    
    private ContactApplicationMapper contactMapper;
    private UUID contactId;
    private UUID companyId;
    
    @BeforeEach
    void setUp() {
        contactMapper = new ContactApplicationMapper();
        contactId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }
    
    @Test
    @DisplayName("Deve converter ContactCreateCommandDTO para Contact")
    void shouldConvertContactCreateCommandDTOToContact() {
        // Given
        ContactCreateCommandDTO command = new ContactCreateCommandDTO(
            "João Silva",
            "joao.silva@empresa.com",
            "(11) 99999-9999",
            "https://www.empresa.com",
            "Gerente",
            "Vendas"
        );
        
        // When
        Contact contact = contactMapper.toDomain(command);
        
        // Then
        assertNotNull(contact);
        assertEquals("João Silva", contact.getName());
        assertEquals("joao.silva@empresa.com", contact.getEmail());
        assertEquals("(11) 99999-9999", contact.getPhone());
        assertEquals("https://www.empresa.com", contact.getWebsite());
        assertEquals("Gerente", contact.getPosition());
        assertEquals("Vendas", contact.getDepartment());
        assertTrue(contact.isActive());
    }
    
    @Test
    @DisplayName("Deve converter ContactUpdateCommandDTO para Contact atualizado")
    void shouldConvertContactUpdateCommandDTOToUpdatedContact() {
        // Given
        Contact existingContact = Contact.of(
            contactId, "João Silva", "joao.silva@empresa.com", 
            "(11) 99999-9999", "https://www.empresa.com", "Gerente", "Vendas", true
        );
        
        ContactUpdateCommandDTO command = new ContactUpdateCommandDTO(
            "João Silva Atualizado",
            "joao.silva.novo@empresa.com",
            "(11) 88888-8888",
            "https://www.empresa.com",
            "Supervisor",
            "Marketing"
        );
        
        // When
        Contact updatedContact = contactMapper.toDomain(command, existingContact);
        
        // Then
        assertNotNull(updatedContact);
        assertEquals(contactId, updatedContact.getId());
        assertEquals("João Silva Atualizado", updatedContact.getName());
        assertEquals("joao.silva.novo@empresa.com", updatedContact.getEmail());
        assertEquals("(11) 88888-8888", updatedContact.getPhone());
        assertEquals("Supervisor", updatedContact.getPosition());
        assertEquals("Marketing", updatedContact.getDepartment());
        assertTrue(updatedContact.isActive());
    }
    
    @Test
    @DisplayName("Deve converter Contact para ContactViewDTO")
    void shouldConvertContactToContactViewDTO() {
        // Given
        Contact contact = Contact.of(
            contactId, "João Silva", "joao.silva@empresa.com", 
            "(11) 99999-9999", "https://www.empresa.com", "Gerente", "Vendas", true
        );
        
        // When
        ContactViewDTO view = contactMapper.toViewDTO(contact, companyId);
        
        // Then
        assertNotNull(view);
        assertEquals(contactId, view.id());
        assertEquals(companyId, view.companyId());
        assertEquals("João Silva", view.name());
        assertEquals("joao.silva@empresa.com", view.email());
        assertEquals("(11) 99999-9999", view.phone());
        assertEquals("Gerente", view.position());
        assertEquals("Vendas", view.department());
        assertTrue(view.active());
    }
    
    @Test
    @DisplayName("Deve converter ContactCreateDTO para ContactCreateCommandDTO")
    void shouldConvertContactCreateDTOToContactCreateCommandDTO() {
        // Given
        ContactCreateDTO dto = ContactCreateDTO.builder()
            .name("João Silva")
            .email("joao.silva@empresa.com")
            .phone("(11) 99999-9999")
            .position("Gerente")
            .department("Vendas")
            .build();
        
        // When
        ContactCreateCommandDTO command = contactMapper.toCreateCommand(dto);
        
        // Then
        assertNotNull(command);
        assertEquals("João Silva", command.name());
        assertEquals("joao.silva@empresa.com", command.email());
        assertEquals("(11) 99999-9999", command.phone());
        assertEquals("Gerente", command.position());
        assertEquals("Vendas", command.department());
    }
    
    @Test
    @DisplayName("Deve converter ContactUpdateDTO para ContactUpdateCommandDTO")
    void shouldConvertContactUpdateDTOToContactUpdateCommandDTO() {
        // Given
        ContactUpdateDTO dto = ContactUpdateDTO.builder()
            .name("João Silva Atualizado")
            .email("joao.silva.novo@empresa.com")
            .phone("(11) 88888-8888")
            .position("Supervisor")
            .department("Marketing")
            .build();
        
        // When
        ContactUpdateCommandDTO command = contactMapper.toUpdateCommand(dto);
        
        // Then
        assertNotNull(command);
        assertEquals("João Silva Atualizado", command.name());
        assertEquals("joao.silva.novo@empresa.com", command.email());
        assertEquals("(11) 88888-8888", command.phone());
        assertEquals("Supervisor", command.position());
        assertEquals("Marketing", command.department());
    }
    
    @Test
    @DisplayName("Deve converter ContactViewDTO para ContactResponseDTO")
    void shouldConvertContactViewDTOToContactResponseDTO() {
        // Given
        ContactViewDTO view = new ContactViewDTO(
            contactId, companyId, "João Silva", "joao.silva@empresa.com", 
            "(11) 99999-9999", "https://www.empresa.com", "Gerente", "Vendas", true
        );
        
        // When
        ContactResponseDTO response = contactMapper.toResponseDTO(view);
        
        // Then
        assertNotNull(response);
        assertEquals(contactId, response.getId());
        assertEquals(companyId, response.getCompanyId());
        assertEquals("João Silva", response.getName());
        assertEquals("joao.silva@empresa.com", response.getEmail());
        assertEquals("(11) 99999-9999", response.getPhone());
        assertEquals("Gerente", response.getPosition());
        assertEquals("Vendas", response.getDepartment());
        assertTrue(response.isActive());
    }
    
    @Test
    @DisplayName("Deve retornar null quando entrada é null")
    void shouldReturnNullWhenInputIsNull() {
        // When & Then
        assertNull(contactMapper.toDomain((ContactCreateCommandDTO) null));
        assertNull(contactMapper.toDomain(null, null));
        assertNull(contactMapper.toViewDTO(null, companyId));
        assertNull(contactMapper.toCreateCommand(null));
        assertNull(contactMapper.toUpdateCommand(null));
        assertNull(contactMapper.toResponseDTO(null));
    }
    
    @Test
    @DisplayName("Deve preservar campos nulos em ContactUpdateCommandDTO")
    void shouldPreserveNullFieldsInContactUpdateCommandDTO() {
        // Given
        Contact existingContact = Contact.of(
            contactId, "João Silva", "joao.silva@empresa.com", 
            "(11) 99999-9999", "https://www.empresa.com", "Gerente", "Vendas", true
        );
        
        ContactUpdateCommandDTO command = new ContactUpdateCommandDTO(
            null, null, null, null, null, null
        );
        
        // When
        Contact updatedContact = contactMapper.toDomain(command, existingContact);
        
        // Then
        assertNotNull(updatedContact);
        assertEquals(contactId, updatedContact.getId());
        assertEquals("João Silva", updatedContact.getName());
        assertEquals("joao.silva@empresa.com", updatedContact.getEmail());
        assertEquals("(11) 99999-9999", updatedContact.getPhone());
        assertEquals("Gerente", updatedContact.getPosition());
        assertEquals("Vendas", updatedContact.getDepartment());
        assertTrue(updatedContact.isActive());
    }
}
