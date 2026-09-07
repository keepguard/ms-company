package com.keepguard.ms_company.application.mapper;

import com.keepguard.ms_company.application.dto.address.AddressCreateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressViewDTO;
import com.keepguard.ms_company.domain.entity.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Address Application Mapper Tests")
class AddressApplicationMapperTest {

    private AddressApplicationMapper addressMapper;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        addressMapper = new AddressApplicationMapper();
        companyId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Deve converter AddressCreateCommandDTO para Address")
    void shouldConvertCreateCommandToAddress() {
        var command = new AddressCreateCommandDTO(
            "Rua das Flores", "123", "Sala 1", "Centro", "São Paulo", "SP", "Brasil", "01234567");

        Address address = addressMapper.toDomain(command);

        assertNotNull(address);
        assertEquals("Rua das Flores", address.getStreet());
        assertEquals("123", address.getNumber());
        assertEquals("Sala 1", address.getComplement());
        assertEquals("Centro", address.getDistrict());
        assertEquals("São Paulo", address.getCity());
        assertEquals("SP", address.getState());
        assertEquals("Brasil", address.getCountry());
        assertEquals("01234567", address.getZipCode());
    }

    @Test
    @DisplayName("Deve converter Address para AddressViewDTO")
    void shouldConvertAddressToViewDTO() {
        Address address = Address.create(
            "Rua das Flores", "123", "Sala 1", "Centro", "São Paulo", "SP", "Brasil", "01234567");

        AddressViewDTO view = addressMapper.toViewDTO(address, companyId);

        assertNotNull(view);
        assertEquals(companyId, view.companyId());
        assertEquals("Rua das Flores", view.street());
        assertEquals("123", view.number());
        assertTrue(view.active());
    }

    @Test
    @DisplayName("Deve atualizar Address a partir de AddressUpdateCommandDTO")
    void shouldConvertUpdateCommandToAddress() {
        Address existing = Address.create(
            "Rua Antiga", "1", null, "Centro", "São Paulo", "SP", "Brasil", "01234567");
        var command = new AddressUpdateCommandDTO(
            "Rua Nova", "456", "Sala 2", "Centro", "São Paulo", "SP", "Brasil", "01234567");

        Address updated = addressMapper.toDomain(command, existing);

        assertNotNull(updated);
        assertEquals("Rua Nova", updated.getStreet());
        assertEquals("456", updated.getNumber());
        assertEquals("Sala 2", updated.getComplement());
    }

    @Test
    @DisplayName("Deve retornar null quando entrada é null")
    void shouldReturnNullWhenInputIsNull() {
        assertNull(addressMapper.toDomain((AddressCreateCommandDTO) null));
        assertNull(addressMapper.toViewDTO(null));
        assertNull(addressMapper.toViewDTO(null, companyId));
    }
}
