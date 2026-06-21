package com.keepguard.ms_company.adapters.in.rest.contact;

import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.contact.dto.ContactUpdateDTO;
import com.keepguard.ms_company.adapters.in.rest.contact.mapper.ContactAdapterMapper;
import com.keepguard.ms_company.application.dto.contact.ContactCreateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactViewDTO;
import com.keepguard.ms_company.application.port.in.ContactPort;
import com.keepguard.ms_company.test.builder.ContactTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testes unitários para ContactController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Contact Controller Tests")
class ContactControllerTest {

    @Mock
    private ContactPort contactPort;

    @Mock
    private ContactAdapterMapper contactAdapterMapper;

    @InjectMocks
    private ContactController contactController;

    private ContactViewDTO contactView;
    private ContactResponseDTO contactResponse;
    private UUID contactId;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        contactId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        
        // Criar objetos de teste usando builders
        contactView = ContactTestBuilder.builder()
            .withId(contactId)
            .withCompanyId(companyId)
            .buildView();
            
        contactResponse = ContactTestBuilder.builder()
            .withId(contactId)
            .withCompanyId(companyId)
            .buildResponseDTO();
    }

    @Test
    @DisplayName("Deve criar contato com DTO válido")
    void shouldCreateContactWithValidDTO() {
        // Given
        ContactCreateDTO createDTO = ContactTestBuilder.builder()
            .buildCreateDTO();
            
        ContactCreateCommandDTO createCommand = ContactTestBuilder.builder()
            .buildCreateCommand();

        when(contactAdapterMapper.toCreateCommand(any(ContactCreateDTO.class)))
            .thenReturn(createCommand);
        when(contactPort.create(eq(companyId), any(ContactCreateCommandDTO.class)))
            .thenReturn(contactView);
        when(contactAdapterMapper.toResponseDTO(any(ContactViewDTO.class)))
            .thenReturn(contactResponse);

        // When
        ResponseEntity<ContactResponseDTO> response = contactController.create(companyId, createDTO);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        ContactResponseDTO responseBody = response.getBody();
        assertEquals(contactId, responseBody.getId());
        assertEquals(companyId, responseBody.getCompanyId());
        assertEquals(createDTO.getName(), responseBody.getName());
        assertEquals(createDTO.getEmail(), responseBody.getEmail());
        
        verify(contactAdapterMapper, times(1)).toCreateCommand(any(ContactCreateDTO.class));
        verify(contactPort, times(1)).create(eq(companyId), any(ContactCreateCommandDTO.class));
        verify(contactAdapterMapper, times(1)).toResponseDTO(any(ContactViewDTO.class));
    }

    @Test
    @DisplayName("Deve lidar com exceções durante criação")
    void shouldHandleExceptionsDuringCreation() {
        // Given
        ContactCreateDTO createDTO = ContactTestBuilder.builder()
            .buildCreateDTO();
            
        ContactCreateCommandDTO createCommand = ContactTestBuilder.builder()
            .buildCreateCommand();

        when(contactAdapterMapper.toCreateCommand(any(ContactCreateDTO.class)))
            .thenReturn(createCommand);
        when(contactPort.create(eq(companyId), any(ContactCreateCommandDTO.class)))
            .thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            contactController.create(companyId, createDTO);
        });
        
        verify(contactAdapterMapper, times(1)).toCreateCommand(any(ContactCreateDTO.class));
        verify(contactPort, times(1)).create(eq(companyId), any(ContactCreateCommandDTO.class));
    }

    @Test
    @DisplayName("Deve atualizar contato com DTO válido")
    void shouldUpdateContactWithValidDTO() {
        // Given
        ContactUpdateDTO updateDTO = ContactTestBuilder.builder()
            .withName("Contato Atualizado")
            .withEmail("contato.atualizado@email.com")
            .buildUpdateDTO();
            
        ContactUpdateCommandDTO updateCommand = ContactTestBuilder.builder()
            .withName("Contato Atualizado")
            .withEmail("contato.atualizado@email.com")
            .buildUpdateCommand();

        when(contactAdapterMapper.toUpdateCommand(any(ContactUpdateDTO.class)))
            .thenReturn(updateCommand);
        when(contactPort.update(eq(contactId), any(ContactUpdateCommandDTO.class)))
            .thenReturn(contactView);
        ContactResponseDTO updatedResponse = ContactTestBuilder.builder()
            .withId(contactId)
            .withCompanyId(companyId)
            .withName("Contato Atualizado")
            .withEmail("contato.atualizado@email.com")
            .buildResponseDTO();
        when(contactAdapterMapper.toResponseDTO(any(ContactViewDTO.class)))
            .thenReturn(updatedResponse);

        // When
        ResponseEntity<ContactResponseDTO> response = contactController.update(contactId, updateDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        ContactResponseDTO responseBody = response.getBody();
        assertEquals(contactId, responseBody.getId());
        assertEquals(updateDTO.getName(), responseBody.getName());
        assertEquals(updateDTO.getEmail(), responseBody.getEmail());
        
        verify(contactAdapterMapper, times(1)).toUpdateCommand(any(ContactUpdateDTO.class));
        verify(contactPort, times(1)).update(eq(contactId), any(ContactUpdateCommandDTO.class));
        verify(contactAdapterMapper, times(1)).toResponseDTO(any(ContactViewDTO.class));
    }

    @Test
    @DisplayName("Deve lidar com exceções durante atualização")
    void shouldHandleExceptionsDuringUpdate() {
        // Given
        ContactUpdateDTO updateDTO = ContactTestBuilder.builder()
            .withName("Contato Atualizado")
            .buildUpdateDTO();
            
        ContactUpdateCommandDTO updateCommand = ContactTestBuilder.builder()
            .withName("Contato Atualizado")
            .buildUpdateCommand();

        when(contactAdapterMapper.toUpdateCommand(any(ContactUpdateDTO.class)))
            .thenReturn(updateCommand);
        when(contactPort.update(eq(contactId), any(ContactUpdateCommandDTO.class)))
            .thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            contactController.update(contactId, updateDTO);
        });
        
        verify(contactAdapterMapper, times(1)).toUpdateCommand(any(ContactUpdateDTO.class));
        verify(contactPort, times(1)).update(eq(contactId), any(ContactUpdateCommandDTO.class));
    }

    @Test
    @DisplayName("Deve buscar contato por ID")
    void shouldFindContactById() {
        // Given
        when(contactPort.getById(contactId))
            .thenReturn(contactView);
        when(contactAdapterMapper.toResponseDTO(any(ContactViewDTO.class)))
            .thenReturn(contactResponse);

        // When
        ResponseEntity<ContactResponseDTO> response = contactController.getById(contactId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        ContactResponseDTO responseBody = response.getBody();
        assertEquals(contactId, responseBody.getId());
        assertEquals(companyId, responseBody.getCompanyId());
        assertEquals(contactResponse.getName(), responseBody.getName());
        assertEquals(contactResponse.getEmail(), responseBody.getEmail());
        
        verify(contactPort, times(1)).getById(contactId);
        verify(contactAdapterMapper, times(1)).toResponseDTO(any(ContactViewDTO.class));
    }

    @Test
    @DisplayName("Deve deletar contato por ID")
    void shouldDeleteContactById() {
        // Given
        doNothing().when(contactPort).delete(contactId);

        // When
        ResponseEntity<Void> response = contactController.delete(contactId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(contactPort, times(1)).delete(contactId);
    }

    @Test
    @DisplayName("Deve lidar com exceções durante busca por ID")
    void shouldHandleExceptionsDuringGetById() {
        // Given
        when(contactPort.getById(contactId))
            .thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            contactController.getById(contactId);
        });
        
        verify(contactPort, times(1)).getById(contactId);
    }

    @Test
    @DisplayName("Deve lidar com exceções durante exclusão")
    void shouldHandleExceptionsDuringDelete() {
        // Given
        doThrow(new RuntimeException("Service error")).when(contactPort).delete(contactId);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            contactController.delete(contactId);
        });
        
        verify(contactPort, times(1)).delete(contactId);
    }
}