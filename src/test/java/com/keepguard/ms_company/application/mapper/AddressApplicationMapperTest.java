package com.keepguard.ms_company.application.mapper;

import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressUpdateDTO;
import com.keepguard.ms_company.application.mapper.AddressApplicationMapper;
import com.keepguard.ms_company.application.dto.address.AddressCreateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressViewDTO;
import com.keepguard.ms_company.domain.entity.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para AddressAdapterMapper
 * Testa conversões entre DTOs e entidades
 */
@DisplayName("Address Application Mapper Tests")
class AddressApplicationMapperTest {
    
    private AddressApplicationMapper addressMapper;
    private UUID addressId;
    private UUID companyId;
    
    @BeforeEach
    void setUp() {
        addressMapper = new AddressApplicationMapper();
        addressId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }
    
    @Test
    @DisplayName("Deve converter AddressCreateDTO para AddressCreateCommandDTO")
    void shouldConvertAddressCreateDTOToAddressCreateCommandDTO() {
        // Given
        AddressCreateDTO dto = new AddressCreateDTO();
        dto.setStreet("Rua das Flores");
        dto.setNumber("123");
        dto.setComplement("Sala 1");
        dto.setDistrict("Centro");
        dto.setCity("São Paulo");
        dto.setState("SP");
        dto.setCountry("Brasil");
        dto.setZipCode("01234567");
        
        // When
        AddressCreateCommandDTO command = addressMapper.toCreateCommand(dto);
        
        // Then
        assertNotNull(command);
        assertEquals("Rua das Flores", command.street());
        assertEquals("123", command.number());
        assertEquals("Sala 1", command.complement());
        assertEquals("Centro", command.district());
        assertEquals("São Paulo", command.city());
        assertEquals("SP", command.state());
        assertEquals("Brasil", command.country());
        assertEquals("01234567", command.zipCode());
    }
    
    @Test
    @DisplayName("Deve converter AddressUpdateDTO para AddressUpdateCommandDTO")
    void shouldConvertAddressUpdateDTOToAddressUpdateCommandDTO() {
        // Given
        AddressUpdateDTO dto = new AddressUpdateDTO();
        dto.setStreet("Rua Atualizada");
        dto.setNumber("456");
        dto.setComplement("Sala 2");
        dto.setDistrict("Centro");
        dto.setCity("São Paulo");
        dto.setState("SP");
        dto.setCountry("Brasil");
        dto.setZipCode("01234567");
        
        // When
        AddressUpdateCommandDTO command = addressMapper.toUpdateCommand(dto);
        
        // Then
        assertNotNull(command);
        assertEquals("Rua Atualizada", command.street());
        assertEquals("456", command.number());
        assertEquals("Sala 2", command.complement());
        assertEquals("Centro", command.district());
        assertEquals("São Paulo", command.city());
        assertEquals("SP", command.state());
        assertEquals("Brasil", command.country());
        assertEquals("01234567", command.zipCode());
    }
    
    @Test
    @DisplayName("Deve converter AddressViewDTO para AddressResponseDTO")
    void shouldConvertAddressViewDTOToAddressResponseDTO() {
        // Given
        AddressViewDTO view = new AddressViewDTO(
            addressId,
            companyId,
            "Rua das Flores",
            "123",
            "Sala 1",
            "Centro",
            "São Paulo",
            "SP",
            "Brasil",
            "01234567",
            true
        );
        
        // When
        AddressResponseDTO responseDTO = addressMapper.toResponseDTO(view);
        
        // Then
        assertNotNull(responseDTO);
        assertEquals(addressId, responseDTO.getId());
        assertEquals(companyId, responseDTO.getCompanyId());
        assertEquals("Rua das Flores", responseDTO.getStreet());
        assertEquals("123", responseDTO.getNumber());
        assertEquals("Sala 1", responseDTO.getComplement());
        assertEquals("Centro", responseDTO.getDistrict());
        assertEquals("São Paulo", responseDTO.getCity());
        assertEquals("SP", responseDTO.getState());
        assertEquals("Brasil", responseDTO.getCountry());
        assertEquals("01234567", responseDTO.getZipCode());
        assertTrue(responseDTO.isActive());
    }
    
    @Test
    @DisplayName("Deve retornar null quando entrada é null")
    void shouldReturnNullWhenInputIsNull() {
        // When & Then
        assertNull(addressMapper.toCreateCommand(null));
        assertNull(addressMapper.toUpdateCommand(null));
        assertNull(addressMapper.toResponseDTO(null));
    }
    
    @Test
    @DisplayName("Deve converter endereço com complemento nulo")
    void shouldConvertAddressWithNullComplement() {
        // Given
        AddressCreateDTO dto = new AddressCreateDTO();
        dto.setStreet("Rua das Flores");
        dto.setNumber("123");
        dto.setComplement(null);
        dto.setDistrict("Centro");
        dto.setCity("São Paulo");
        dto.setState("SP");
        dto.setCountry("Brasil");
        dto.setZipCode("01234567");
        
        // When
        AddressCreateCommandDTO command = addressMapper.toCreateCommand(dto);
        
        // Then
        assertNotNull(command);
        assertEquals("Rua das Flores", command.street());
        assertEquals("123", command.number());
        assertNull(command.complement());
        assertEquals("Centro", command.district());
        assertEquals("São Paulo", command.city());
        assertEquals("SP", command.state());
        assertEquals("Brasil", command.country());
        assertEquals("01234567", command.zipCode());
    }
}
