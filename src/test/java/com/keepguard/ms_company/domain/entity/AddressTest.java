package com.keepguard.ms_company.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a entidade Address
 * Testa apenas lógica de domínio, sem dependências de frameworks
 */
@DisplayName("Address Domain Tests")
class AddressTest {
    
    private Address address;
    private UUID addressId;
    
    @BeforeEach
    void setUp() {
        addressId = UUID.randomUUID();
        address = Address.create(
            "Rua das Flores",
            "123",
            "Sala 1",
            "Centro",
            "São Paulo",
            "SP",
            "Brasil",
            "01234567"
        );
    }
    
    @Test
    @DisplayName("Deve criar endereço com dados válidos")
    void shouldCreateAddressWithValidData() {
        assertNotNull(address);
        assertEquals("Rua das Flores", address.getStreet());
        assertEquals("123", address.getNumber());
        assertEquals("Sala 1", address.getComplement());
        assertEquals("Centro", address.getDistrict());
        assertEquals("São Paulo", address.getCity());
        assertEquals("SP", address.getState());
        assertEquals("Brasil", address.getCountry());
        assertEquals("01234567", address.getZipCode());
        assertTrue(address.isActive());
    }
    
    @Test
    @DisplayName("Deve criar endereço com ID específico")
    void shouldCreateAddressWithSpecificId() {
        Address addressWithId = Address.of(
            addressId,
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
        
        assertEquals(addressId, addressWithId.getId());
        assertEquals("Rua das Flores", addressWithId.getStreet());
        assertTrue(addressWithId.isActive());
    }
    
    @Test
    @DisplayName("Deve ativar endereço")
    void shouldActivateAddress() {
        // Given
        address.deactivate();
        assertFalse(address.isActive());
        
        // When
        address.activate();
        
        // Then
        assertTrue(address.isActive());
    }
    
    @Test
    @DisplayName("Deve desativar endereço")
    void shouldDeactivateAddress() {
        // Given
        assertTrue(address.isActive());
        
        // When
        address.deactivate();
        
        // Then
        assertFalse(address.isActive());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar endereço com logradouro nulo")
    void shouldThrowExceptionWhenCreatingAddressWithNullStreet() {
        assertThrows(IllegalArgumentException.class, () -> {
            Address.create(
                null, "123", "Sala 1", "Centro", "São Paulo", "SP", "Brasil", "01234567"
            );
        });
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar endereço com logradouro vazio")
    void shouldThrowExceptionWhenCreatingAddressWithEmptyStreet() {
        assertThrows(IllegalArgumentException.class, () -> {
            Address.create(
                "   ", "123", "Sala 1", "Centro", "São Paulo", "SP", "Brasil", "01234567"
            );
        });
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando estado for nulo")
    void shouldThrowValidationExceptionWhenStateIsNull() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> Address.create(
                "Rua das Flores",
                "123",
                "Sala 1",
                "Centro",
                "São Paulo",
                null, // Estado nulo
                "Brasil",
                "01234567"
            )
        );
        
        assertEquals("Estado é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando estado for vazio")
    void shouldThrowValidationExceptionWhenStateIsEmpty() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> Address.create(
                "Rua das Flores",
                "123",
                "Sala 1",
                "Centro",
                "São Paulo",
                "", // Estado vazio
                "Brasil",
                "01234567"
            )
        );
        
        assertEquals("Estado é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando CEP for nulo")
    void shouldThrowValidationExceptionWhenZipCodeIsNull() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> Address.create(
                "Rua das Flores",
                "123",
                "Sala 1",
                "Centro",
                "São Paulo",
                "SP",
                "Brasil",
                null // CEP nulo
            )
        );
        
        assertEquals("CEP é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando CEP for vazio")
    void shouldThrowValidationExceptionWhenZipCodeIsEmpty() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> Address.create(
                "Rua das Flores",
                "123",
                "Sala 1",
                "Centro",
                "São Paulo",
                "SP",
                "Brasil",
                "" // CEP vazio
            )
        );
        
        assertEquals("CEP é obrigatório", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar endereço com logradouro muito longo")
    void shouldThrowExceptionWhenCreatingAddressWithTooLongStreet() {
        String longStreet = "a".repeat(151);
        assertThrows(IllegalArgumentException.class, () -> {
            Address.create(
                longStreet, "123", "Sala 1", "Centro", "São Paulo", "SP", "Brasil", "01234567"
            );
        });
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar endereço com número nulo")
    void shouldThrowExceptionWhenCreatingAddressWithNullNumber() {
        assertThrows(IllegalArgumentException.class, () -> {
            Address.create(
                "Rua das Flores", null, "Sala 1", "Centro", "São Paulo", "SP", "Brasil", "01234567"
            );
        });
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar endereço com número muito longo")
    void shouldThrowExceptionWhenCreatingAddressWithTooLongNumber() {
        String longNumber = "1".repeat(21);
        assertThrows(IllegalArgumentException.class, () -> {
            Address.create(
                "Rua das Flores", longNumber, "Sala 1", "Centro", "São Paulo", "SP", "Brasil", "01234567"
            );
        });
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar endereço com bairro nulo")
    void shouldThrowExceptionWhenCreatingAddressWithNullDistrict() {
        assertThrows(IllegalArgumentException.class, () -> {
            Address.create(
                "Rua das Flores", "123", "Sala 1", null, "São Paulo", "SP", "Brasil", "01234567"
            );
        });
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar endereço com cidade nula")
    void shouldThrowExceptionWhenCreatingAddressWithNullCity() {
        assertThrows(IllegalArgumentException.class, () -> {
            Address.create(
                "Rua das Flores", "123", "Sala 1", "Centro", null, "SP", "Brasil", "01234567"
            );
        });
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar endereço com estado nulo")
    void shouldThrowExceptionWhenCreatingAddressWithNullState() {
        assertThrows(IllegalArgumentException.class, () -> {
            Address.create(
                "Rua das Flores", "123", "Sala 1", "Centro", "São Paulo", null, "Brasil", "01234567"
            );
        });
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar endereço com estado inválido")
    void shouldThrowExceptionWhenCreatingAddressWithInvalidState() {
        assertThrows(IllegalArgumentException.class, () -> {
            Address.create(
                "Rua das Flores", "123", "Sala 1", "Centro", "São Paulo", "SPA", "Brasil", "01234567"
            );
        });
    }
    
    @Test
    @DisplayName("Deve aceitar estado em minúsculas e converter para maiúsculas")
    void shouldAcceptLowerCaseStateAndConvertToUpperCase() {
        Address addressWithLowerCaseState = Address.create(
            "Rua das Flores", "123", "Sala 1", "Centro", "São Paulo", "sp", "Brasil", "01234567"
        );
        
        assertEquals("SP", addressWithLowerCaseState.getState());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar endereço com país nulo")
    void shouldThrowExceptionWhenCreatingAddressWithNullCountry() {
        assertThrows(IllegalArgumentException.class, () -> {
            Address.create(
                "Rua das Flores", "123", "Sala 1", "Centro", "São Paulo", "SP", null, "01234567"
            );
        });
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar endereço com CEP nulo")
    void shouldThrowExceptionWhenCreatingAddressWithNullZipCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            Address.create(
                "Rua das Flores", "123", "Sala 1", "Centro", "São Paulo", "SP", "Brasil", null
            );
        });
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar endereço com CEP inválido")
    void shouldThrowExceptionWhenCreatingAddressWithInvalidZipCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            Address.create(
                "Rua das Flores", "123", "Sala 1", "Centro", "São Paulo", "SP", "Brasil", "1234567"
            );
        });
    }
    
    @Test
    @DisplayName("Deve aceitar CEP com formatação e limpar")
    void shouldAcceptZipCodeWithFormattingAndClean() {
        Address addressWithFormattedZipCode = Address.create(
            "Rua das Flores", "123", "Sala 1", "Centro", "São Paulo", "SP", "Brasil", "01234-567"
        );
        
        assertEquals("01234567", addressWithFormattedZipCode.getZipCode());
    }
    
    @Test
    @DisplayName("Deve aceitar complemento nulo")
    void shouldAcceptNullComplement() {
        Address addressWithoutComplement = Address.create(
            "Rua das Flores", "123", null, "Centro", "São Paulo", "SP", "Brasil", "01234567"
        );
        
        assertNull(addressWithoutComplement.getComplement());
    }
    
    @Test
    @DisplayName("Deve remover espaços em branco dos campos")
    void shouldTrimWhitespaceFromFields() {
        Address addressWithWhitespace = Address.create(
            "  Rua das Flores  ", "  123  ", "  Sala 1  ", "  Centro  ", 
            "  São Paulo  ", "  SP  ", "  Brasil  ", "  01234567  "
        );
        
        assertEquals("Rua das Flores", addressWithWhitespace.getStreet());
        assertEquals("123", addressWithWhitespace.getNumber());
        assertEquals("Sala 1", addressWithWhitespace.getComplement());
        assertEquals("Centro", addressWithWhitespace.getDistrict());
        assertEquals("São Paulo", addressWithWhitespace.getCity());
        assertEquals("SP", addressWithWhitespace.getState());
        assertEquals("Brasil", addressWithWhitespace.getCountry());
        assertEquals("01234567", addressWithWhitespace.getZipCode());
    }
    
    @Test
    @DisplayName("Deve implementar equals corretamente")
    void shouldImplementEqualsCorrectly() {
        Address address1 = Address.create(
            "Rua das Flores", "123", "Sala 1", "Centro", "São Paulo", "SP", "Brasil", "01234567"
        );
        
        Address address2 = Address.create(
            "Rua das Flores", "123", "Sala 1", "Centro", "São Paulo", "SP", "Brasil", "01234567"
        );
        
        Address address3 = Address.create(
            "Rua das Rosas", "456", "Sala 2", "Centro", "São Paulo", "SP", "Brasil", "01234567"
        );
        
        assertEquals(address1, address1);
        assertNotEquals(address1, address2); // IDs diferentes
        assertNotEquals(address1, address3);
        assertNotEquals(address1, null);
        assertNotEquals(address1, "not an address");
    }
    
    @Test
    @DisplayName("Deve implementar hashCode corretamente")
    void shouldImplementHashCodeCorrectly() {
        Address address1 = Address.create(
            "Rua das Flores", "123", "Sala 1", "Centro", "São Paulo", "SP", "Brasil", "01234567"
        );
        
        Address address2 = Address.create(
            "Rua das Flores", "123", "Sala 1", "Centro", "São Paulo", "SP", "Brasil", "01234567"
        );
        
        // HashCodes devem ser diferentes para IDs diferentes
        assertNotEquals(address1.hashCode(), address2.hashCode());
    }
    
    @Test
    @DisplayName("Deve implementar toString corretamente")
    void shouldImplementToStringCorrectly() {
        String toString = address.toString();
        
        assertTrue(toString.contains("Address"));
        assertTrue(toString.contains("street='Rua das Flores'"));
        assertTrue(toString.contains("number='123'"));
        assertTrue(toString.contains("city='São Paulo'"));
        assertTrue(toString.contains("state='SP'"));
        assertTrue(toString.contains("active=true"));
    }
}
