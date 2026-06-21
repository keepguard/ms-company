package com.keepguard.ms_company.adapters.in.rest.address.mapper;

import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressUpdateDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.AddressDTO;
import com.keepguard.ms_company.application.dto.address.AddressCreateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressViewDTO;
import com.keepguard.ms_company.test.builder.AddressTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para AddressAdapterMapper
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Address Adapter Mapper Tests")
class AddressAdapterMapperTest {
    
    private AddressAdapterMapper addressAdapterMapper;
    private UUID addressId;
    private UUID companyId;
    
    @BeforeEach
    void setUp() {
        addressAdapterMapper = new AddressAdapterMapper();
        addressId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }
    
    @Test
    @DisplayName("Deve mapear AddressCreateDTO para AddressCreateCommandDTO com sucesso")
    void shouldMapAddressCreateDTOToAddressCreateCommandDTOSuccessfully() {
        // Given
        AddressCreateDTO dto = AddressTestBuilder.builder()
            .buildCreateDTO();
        
        // When
        AddressCreateCommandDTO result = addressAdapterMapper.toCreateCommand(dto);
        
        // Then
        assertNotNull(result);
        assertEquals(dto.getStreet(), result.street());
        assertEquals(dto.getNumber(), result.number());
        assertEquals(dto.getComplement(), result.complement());
        assertEquals(dto.getDistrict(), result.district());
        assertEquals(dto.getCity(), result.city());
        assertEquals(dto.getState(), result.state());
        assertEquals(dto.getCountry(), result.country());
        assertEquals(dto.getZipCode(), result.zipCode());
    }
    
    @Test
    @DisplayName("Deve retornar null quando AddressCreateDTO for null")
    void shouldReturnNullWhenAddressCreateDTOIsNull() {
        // When
        AddressCreateCommandDTO result = addressAdapterMapper.toCreateCommand(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear AddressUpdateDTO para AddressUpdateCommandDTO com sucesso")
    void shouldMapAddressUpdateDTOToAddressUpdateCommandDTOSuccessfully() {
        // Given
        AddressUpdateDTO dto = AddressTestBuilder.builder()
            .withRioDeJaneiro()
            .buildUpdateDTO();
        
        // When
        AddressUpdateCommandDTO result = addressAdapterMapper.toUpdateCommand(dto);
        
        // Then
        assertNotNull(result);
        assertEquals(dto.getStreet(), result.street());
        assertEquals(dto.getNumber(), result.number());
        assertEquals(dto.getComplement(), result.complement());
        assertEquals(dto.getDistrict(), result.district());
        assertEquals(dto.getCity(), result.city());
        assertEquals(dto.getState(), result.state());
        assertEquals(dto.getCountry(), result.country());
        assertEquals(dto.getZipCode(), result.zipCode());
    }
    
    @Test
    @DisplayName("Deve retornar null quando AddressUpdateDTO for null")
    void shouldReturnNullWhenAddressUpdateDTOIsNull() {
        // When
        AddressUpdateCommandDTO result = addressAdapterMapper.toUpdateCommand(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear AddressViewDTO para AddressResponseDTO com sucesso")
    void shouldMapAddressViewDTOToAddressResponseDTOSuccessfully() {
        // Given
        AddressViewDTO view = AddressTestBuilder.builder()
            .withId(addressId)
            .withCompanyId(companyId)
            .buildView();
        
        // When
        AddressResponseDTO result = addressAdapterMapper.toResponseDTO(view);
        
        // Then
        assertNotNull(result);
        assertEquals(view.id(), result.getId());
        assertEquals(view.companyId(), result.getCompanyId());
        assertEquals(view.street(), result.getStreet());
        assertEquals(view.number(), result.getNumber());
        assertEquals(view.complement(), result.getComplement());
        assertEquals(view.district(), result.getDistrict());
        assertEquals(view.city(), result.getCity());
        assertEquals(view.state(), result.getState());
        assertEquals(view.country(), result.getCountry());
        assertEquals(view.zipCode(), result.getZipCode());
        assertEquals(view.active(), result.isActive());
    }
    
    @Test
    @DisplayName("Deve retornar null quando AddressViewDTO for null")
    void shouldReturnNullWhenAddressViewDTOIsNull() {
        // When
        AddressResponseDTO result = addressAdapterMapper.toResponseDTO(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear AddressViewDTO para AddressDTO com sucesso")
    void shouldMapAddressViewDTOToAddressDTOSuccessfully() {
        // Given
        AddressViewDTO view = AddressTestBuilder.builder()
            .withId(addressId)
            .withCompanyId(companyId)
            .buildView();
        
        // When
        AddressDTO result = addressAdapterMapper.toCompanyAddressDTO(view);
        
        // Then
        assertNotNull(result);
        assertEquals(view.street(), result.getStreet());
        assertEquals(view.number(), result.getNumber());
        assertEquals(view.complement(), result.getComplement());
        assertEquals(view.district(), result.getDistrict());
        assertEquals(view.city(), result.getCity());
        assertEquals(view.state(), result.getState());
        assertEquals(view.country(), result.getCountry());
        assertEquals(view.zipCode(), result.getZipCode());
    }
    
    @Test
    @DisplayName("Deve retornar null quando AddressViewDTO for null no toCompanyAddressDTO")
    void shouldReturnNullWhenAddressViewDTOIsNullInToCompanyAddressDTO() {
        // When
        AddressDTO result = addressAdapterMapper.toCompanyAddressDTO(null);
        
        // Then
        assertNull(result);
    }
}
