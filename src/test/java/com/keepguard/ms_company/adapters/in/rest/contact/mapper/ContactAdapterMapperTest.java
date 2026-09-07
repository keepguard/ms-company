package com.keepguard.ms_company.adapters.in.rest.contact.mapper;

import com.keepguard.ms_company.adapters.in.rest.contact.dto.request.ContactCreateRequestDTO;
import com.keepguard.ms_company.adapters.in.rest.contact.dto.response.ContactResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.contact.dto.request.ContactUpdateRequestDTO;
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
    @DisplayName("Deve mapear ContactCreateRequestDTO para ContactCreateCommandDTO com sucesso")
    void shouldMapContactCreateDTOToContactCreateCommandDTOSuccessfully() {
        // Given
        ContactCreateRequestDTO dto = ContactTestBuilder.builder()
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
    @DisplayName("Deve retornar null quando ContactCreateRequestDTO for null")
    void shouldReturnNullWhenContactCreateDTOIsNull() {
        // When
        ContactCreateCommandDTO result = contactAdapterMapper.toCreateCommand(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear ContactUpdateRequestDTO para ContactUpdateCommandDTO com sucesso")
    void shouldMapContactUpdateDTOToContactUpdateCommandDTOSuccessfully() {
        // Given
        ContactUpdateRequestDTO dto = ContactTestBuilder.builder()
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
    @DisplayName("Deve retornar null quando ContactUpdateRequestDTO for null")
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
