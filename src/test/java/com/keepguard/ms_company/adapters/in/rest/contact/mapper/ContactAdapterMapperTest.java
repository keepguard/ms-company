package com.keepguard.ms_company.adapters.in.rest.contact.mapper;

import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactUpdateDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.ContactDTO;
import com.keepguard.ms_company.application.dto.contact.ContactCreateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactViewDTO;
import com.keepguard.ms_company.test.builder.ContactTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ContactAdapterMapper
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Contact Adapter Mapper Tests")
class ContactAdapterMapperTest {
    
    private ContactAdapterMapper contactAdapterMapper;
    private UUID contactId;
    private UUID companyId;
    
    @BeforeEach
    void setUp() {
        contactAdapterMapper = new ContactAdapterMapper();
        contactId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }
    
    @Test
    @DisplayName("Deve mapear ContactCreateDTO para ContactCreateCommandDTO com sucesso")
    void shouldMapContactCreateDTOToContactCreateCommandDTOSuccessfully() {
        // Given
        ContactCreateDTO dto = ContactTestBuilder.builder()
            .buildCreateDTO();
        
        // When
        ContactCreateCommandDTO result = contactAdapterMapper.toCreateCommand(dto);
        
        // Then
        assertNotNull(result);
        assertEquals(dto.getName(), result.name());
        assertEquals(dto.getEmail(), result.email());
        assertEquals(dto.getPhone(), result.phone());
        assertEquals(dto.getWebsite(), result.website());
        assertEquals(dto.getPosition(), result.position());
        assertEquals(dto.getDepartment(), result.department());
    }
    
    @Test
    @DisplayName("Deve retornar null quando ContactCreateDTO for null")
    void shouldReturnNullWhenContactCreateDTOIsNull() {
        // When
        ContactCreateCommandDTO result = contactAdapterMapper.toCreateCommand(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear ContactUpdateDTO para ContactUpdateCommandDTO com sucesso")
    void shouldMapContactUpdateDTOToContactUpdateCommandDTOSuccessfully() {
        // Given
        ContactUpdateDTO dto = ContactTestBuilder.builder()
            .buildUpdateDTO();
        
        // When
        ContactUpdateCommandDTO result = contactAdapterMapper.toUpdateCommand(dto);
        
        // Then
        assertNotNull(result);
        assertEquals(dto.getName(), result.name());
        assertEquals(dto.getEmail(), result.email());
        assertEquals(dto.getPhone(), result.phone());
        assertEquals(dto.getWebsite(), result.website());
        assertEquals(dto.getPosition(), result.position());
        assertEquals(dto.getDepartment(), result.department());
    }
    
    @Test
    @DisplayName("Deve retornar null quando ContactUpdateDTO for null")
    void shouldReturnNullWhenContactUpdateDTOIsNull() {
        // When
        ContactUpdateCommandDTO result = contactAdapterMapper.toUpdateCommand(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear ContactViewDTO para ContactResponseDTO com sucesso")
    void shouldMapContactViewDTOToContactResponseDTOSuccessfully() {
        // Given
        ContactViewDTO view = ContactTestBuilder.builder()
            .withId(contactId)
            .withCompanyId(companyId)
            .buildView();
        
        // When
        ContactResponseDTO result = contactAdapterMapper.toResponseDTO(view);
        
        // Then
        assertNotNull(result);
        assertEquals(view.id(), result.getId());
        assertEquals(view.companyId(), result.getCompanyId());
        assertEquals(view.name(), result.getName());
        assertEquals(view.email(), result.getEmail());
        assertEquals(view.phone(), result.getPhone());
        assertEquals(view.website(), result.getWebsite());
        assertEquals(view.position(), result.getPosition());
        assertEquals(view.department(), result.getDepartment());
        assertEquals(view.active(), result.isActive());
    }
    
    @Test
    @DisplayName("Deve retornar null quando ContactViewDTO for null")
    void shouldReturnNullWhenContactViewDTOIsNull() {
        // When
        ContactResponseDTO result = contactAdapterMapper.toResponseDTO(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear ContactViewDTO para ContactDTO com sucesso")
    void shouldMapContactViewDTOToContactDTOSuccessfully() {
        // Given
        ContactViewDTO view = ContactTestBuilder.builder()
            .withId(contactId)
            .withCompanyId(companyId)
            .buildView();
        
        // When
        ContactDTO result = contactAdapterMapper.toCompanyContactDTO(view);
        
        // Then
        assertNotNull(result);
        assertEquals(view.email(), result.getEmail());
        assertEquals(view.phone(), result.getPhone());
        assertEquals(view.website(), result.getWebsite());
    }
    
    @Test
    @DisplayName("Deve retornar null quando ContactViewDTO for null no toCompanyContactDTO")
    void shouldReturnNullWhenContactViewDTOIsNullInToCompanyContactDTO() {
        // When
        ContactDTO result = contactAdapterMapper.toCompanyContactDTO(null);
        
        // Then
        assertNull(result);
    }
}
