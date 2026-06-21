package com.keepguard.ms_company.adapters.in.rest.address;

import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressUpdateDTO;
import com.keepguard.ms_company.adapters.in.rest.address.mapper.AddressAdapterMapper;
import com.keepguard.ms_company.application.dto.address.AddressCreateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressViewDTO;
import com.keepguard.ms_company.application.port.in.AddressPort;
import com.keepguard.ms_company.test.builder.AddressTestBuilder;
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
 * Testes unitários para AddressController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Address Controller Tests")
class AddressControllerTest {

    @Mock
    private AddressPort addressPort;

    @Mock
    private AddressAdapterMapper addressAdapterMapper;

    @InjectMocks
    private AddressController addressController;

    private AddressViewDTO addressView;
    private AddressResponseDTO addressResponse;
    private UUID addressId;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        addressId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        
        // Criar objetos de teste usando builders
        addressView = AddressTestBuilder.builder()
            .withId(addressId)
            .withCompanyId(companyId)
            .buildView();
            
        addressResponse = AddressTestBuilder.builder()
            .withId(addressId)
            .withCompanyId(companyId)
            .buildResponseDTO();
    }

    @Test
    @DisplayName("Deve criar endereço com DTO válido")
    void shouldCreateAddressWithValidDTO() {
        // Given
        AddressCreateDTO createDTO = AddressTestBuilder.builder()
            .buildCreateDTO();
            
        AddressCreateCommandDTO createCommand = AddressTestBuilder.builder()
            .buildCreateCommand();

        when(addressAdapterMapper.toCreateCommand(any(AddressCreateDTO.class)))
            .thenReturn(createCommand);
        when(addressPort.create(eq(companyId), any(AddressCreateCommandDTO.class)))
            .thenReturn(addressView);
        when(addressAdapterMapper.toResponseDTO(any(AddressViewDTO.class)))
            .thenReturn(addressResponse);

        // When
        ResponseEntity<AddressResponseDTO> response = addressController.create(companyId, createDTO);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        AddressResponseDTO responseBody = response.getBody();
        assertEquals(addressId, responseBody.getId());
        assertEquals(companyId, responseBody.getCompanyId());
        assertEquals(createDTO.getStreet(), responseBody.getStreet());
        assertEquals(createDTO.getNumber(), responseBody.getNumber());
        assertEquals(createDTO.getCity(), responseBody.getCity());
        assertEquals(createDTO.getState(), responseBody.getState());
        
        verify(addressAdapterMapper, times(1)).toCreateCommand(any(AddressCreateDTO.class));
        verify(addressPort, times(1)).create(eq(companyId), any(AddressCreateCommandDTO.class));
        verify(addressAdapterMapper, times(1)).toResponseDTO(any(AddressViewDTO.class));
    }

    @Test
    @DisplayName("Deve lidar com exceções durante criação")
    void shouldHandleExceptionsDuringCreation() {
        // Given
        AddressCreateDTO createDTO = AddressTestBuilder.builder()
            .buildCreateDTO();
            
        AddressCreateCommandDTO createCommand = AddressTestBuilder.builder()
            .buildCreateCommand();

        when(addressAdapterMapper.toCreateCommand(any(AddressCreateDTO.class)))
            .thenReturn(createCommand);
        when(addressPort.create(eq(companyId), any(AddressCreateCommandDTO.class)))
            .thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            addressController.create(companyId, createDTO);
        });
        
        verify(addressAdapterMapper, times(1)).toCreateCommand(any(AddressCreateDTO.class));
        verify(addressPort, times(1)).create(eq(companyId), any(AddressCreateCommandDTO.class));
    }

    @Test
    @DisplayName("Deve atualizar endereço com DTO válido")
    void shouldUpdateAddressWithValidDTO() {
        // Given
        AddressUpdateDTO updateDTO = AddressTestBuilder.builder()
            .withStreet("Rua Atualizada")
            .withCity("São Paulo Atualizada")
            .buildUpdateDTO();
            
        AddressUpdateCommandDTO updateCommand = AddressTestBuilder.builder()
            .withStreet("Rua Atualizada")
            .withCity("São Paulo Atualizada")
            .buildUpdateCommand();

        when(addressAdapterMapper.toUpdateCommand(any(AddressUpdateDTO.class)))
            .thenReturn(updateCommand);
        when(addressPort.update(eq(addressId), any(AddressUpdateCommandDTO.class)))
            .thenReturn(addressView);
        AddressResponseDTO updatedResponse = AddressTestBuilder.builder()
            .withId(addressId)
            .withCompanyId(companyId)
            .withStreet("Rua Atualizada")
            .withCity("São Paulo Atualizada")
            .buildResponseDTO();
        when(addressAdapterMapper.toResponseDTO(any(AddressViewDTO.class)))
            .thenReturn(updatedResponse);

        // When
        ResponseEntity<AddressResponseDTO> response = addressController.update(addressId, updateDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        AddressResponseDTO responseBody = response.getBody();
        assertEquals(addressId, responseBody.getId());
        assertEquals(updateDTO.getStreet(), responseBody.getStreet());
        
        verify(addressAdapterMapper, times(1)).toUpdateCommand(any(AddressUpdateDTO.class));
        verify(addressPort, times(1)).update(eq(addressId), any(AddressUpdateCommandDTO.class));
        verify(addressAdapterMapper, times(1)).toResponseDTO(any(AddressViewDTO.class));
    }

    @Test
    @DisplayName("Deve lidar com exceções durante atualização")
    void shouldHandleExceptionsDuringUpdate() {
        // Given
        AddressUpdateDTO updateDTO = AddressTestBuilder.builder()
            .withStreet("Rua Atualizada")
            .buildUpdateDTO();
            
        AddressUpdateCommandDTO updateCommand = AddressTestBuilder.builder()
            .withStreet("Rua Atualizada")
            .buildUpdateCommand();

        when(addressAdapterMapper.toUpdateCommand(any(AddressUpdateDTO.class)))
            .thenReturn(updateCommand);
        when(addressPort.update(eq(addressId), any(AddressUpdateCommandDTO.class)))
            .thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            addressController.update(addressId, updateDTO);
        });
        
        verify(addressAdapterMapper, times(1)).toUpdateCommand(any(AddressUpdateDTO.class));
        verify(addressPort, times(1)).update(eq(addressId), any(AddressUpdateCommandDTO.class));
    }

    @Test
    @DisplayName("Deve buscar endereço por ID")
    void shouldFindAddressById() {
        // Given
        when(addressPort.getById(addressId))
            .thenReturn(addressView);
        when(addressAdapterMapper.toResponseDTO(any(AddressViewDTO.class)))
            .thenReturn(addressResponse);

        // When
        ResponseEntity<AddressResponseDTO> response = addressController.getById(addressId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        AddressResponseDTO responseBody = response.getBody();
        assertEquals(addressId, responseBody.getId());
        assertEquals(companyId, responseBody.getCompanyId());
        assertEquals(addressResponse.getStreet(), responseBody.getStreet());
        
        verify(addressPort, times(1)).getById(addressId);
        verify(addressAdapterMapper, times(1)).toResponseDTO(any(AddressViewDTO.class));
    }

    @Test
    @DisplayName("Deve deletar endereço por ID")
    void shouldDeleteAddressById() {
        // Given
        doNothing().when(addressPort).delete(addressId);

        // When
        ResponseEntity<Void> response = addressController.delete(addressId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(addressPort, times(1)).delete(addressId);
    }

    @Test
    @DisplayName("Deve lidar com exceções durante busca por ID")
    void shouldHandleExceptionsDuringGetById() {
        // Given
        when(addressPort.getById(addressId))
            .thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            addressController.getById(addressId);
        });
        
        verify(addressPort, times(1)).getById(addressId);
    }

    @Test
    @DisplayName("Deve lidar com exceções durante exclusão")
    void shouldHandleExceptionsDuringDelete() {
        // Given
        doThrow(new RuntimeException("Service error")).when(addressPort).delete(addressId);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            addressController.delete(addressId);
        });
        
        verify(addressPort, times(1)).delete(addressId);
    }
}
