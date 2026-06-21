package com.keepguard.ms_company.domain.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a entidade CNAE
 */
class CnaeTest {
    
    private final UUID companyId = UUID.randomUUID();
    private final String validCode = "1234567";
    private final String validDescription = "Atividade de desenvolvimento de software";
    
    @Test
    @DisplayName("Deve criar CNAE com dados válidos")
    void shouldCreateCnaeWithValidData() {
        // Given & When
        Cnae cnae = Cnae.create(
            validCode,
            validDescription,
            "J",
            "62",
            "620",
            "6201",
            "62015",
            true,
            companyId
        );
        
        // Then
        assertNotNull(cnae.getId());
        assertEquals(validCode, cnae.getCode());
        assertEquals(validDescription, cnae.getDescription());
        assertEquals("J", cnae.getSection());
        assertEquals("62", cnae.getDivision());
        assertEquals("620", cnae.getGroupCode());
        assertEquals("6201", cnae.getClassCode());
        assertEquals("62015", cnae.getSubclassCode());
        assertTrue(cnae.isActive());
        assertTrue(cnae.isPrincipal());
        assertEquals(companyId, cnae.getCompanyId());
        assertNotNull(cnae.getCreatedAt());
        assertNotNull(cnae.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve criar CNAE secundário")
    void shouldCreateSecondaryCnae() {
        // Given & When
        Cnae cnae = Cnae.create(
            validCode,
            validDescription,
            null, null, null, null, null,
            false,
            companyId
        );
        
        // Then
        assertFalse(cnae.isPrincipal());
        assertTrue(cnae.isActive());
    }
    
    @Test
    @DisplayName("Deve validar código CNAE obrigatório")
    void shouldValidateRequiredCnaeCode() {
        // Given & When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            Cnae.create(null, validDescription, null, null, null, null, null, false, companyId);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            Cnae.create("", validDescription, null, null, null, null, null, false, companyId);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            Cnae.create("   ", validDescription, null, null, null, null, null, false, companyId);
        });
    }
    
    @Test
    @DisplayName("Deve validar formato do código CNAE")
    void shouldValidateCnaeCodeFormat() {
        // Given & When & Then
        assertDoesNotThrow(() -> {
            Cnae.create("1234567", validDescription, null, null, null, null, null, false, companyId);
        });
        
        assertDoesNotThrow(() -> {
            Cnae.create("1234567", validDescription, null, null, null, null, null, false, companyId);
        });
        
        assertDoesNotThrow(() -> {
            Cnae.create("1234567", validDescription, null, null, null, null, null, false, companyId);
        });
        
        assertDoesNotThrow(() -> {
            Cnae.create("123-4567", validDescription, null, null, null, null, null, false, companyId);
        });
    }
    
    @Test
    @DisplayName("Deve lançar ValidationException quando código CNAE for nulo")
    void shouldThrowValidationExceptionWhenCnaeCodeIsNull() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> Cnae.create(
                null, // Código CNAE nulo
                validDescription,
                null, null, null, null, null,
                false,
                companyId
            )
        );
        
        assertEquals("Código CNAE é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando código CNAE for vazio")
    void shouldThrowValidationExceptionWhenCnaeCodeIsEmpty() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> Cnae.create(
                "", // Código CNAE vazio
                validDescription,
                null, null, null, null, null,
                false,
                companyId
            )
        );
        
        assertEquals("Código CNAE é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve limpar código CNAE removendo caracteres não numéricos")
    void shouldCleanCnaeCode() {
        // Given & When
        Cnae cnae = Cnae.create(
            "123-4567",
            validDescription,
            null, null, null, null, null,
            false,
            companyId
        );
        
        // Then
        assertEquals("1234567", cnae.getCode());
    }
    
    @Test
    @DisplayName("Deve validar descrição obrigatória")
    void shouldValidateRequiredDescription() {
        // Given & When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            Cnae.create(validCode, null, null, null, null, null, null, false, companyId);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            Cnae.create(validCode, "", null, null, null, null, null, false, companyId);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            Cnae.create(validCode, "   ", null, null, null, null, null, false, companyId);
        });
    }
    
    @Test
    @DisplayName("Deve validar tamanho máximo da descrição")
    void shouldValidateDescriptionMaxLength() {
        // Given
        String longDescription = "a".repeat(501);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            Cnae.create(validCode, longDescription, null, null, null, null, null, false, companyId);
        });
    }
    
    @Test
    @DisplayName("Deve validar company ID obrigatório")
    void shouldValidateRequiredCompanyId() {
        // Given & When & Then
        assertThrows(NullPointerException.class, () -> {
            Cnae.create(validCode, validDescription, null, null, null, null, null, false, null);
        });
    }
    
    @Test
    @DisplayName("Deve ativar CNAE")
    void shouldActivateCnae() {
        // Given
        Cnae cnae = Cnae.create(validCode, validDescription, null, null, null, null, null, false, companyId);
        cnae.deactivate();
        assertFalse(cnae.isActive());
        
        // When
        cnae.activate();
        
        // Then
        assertTrue(cnae.isActive());
        assertNotNull(cnae.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve desativar CNAE secundário")
    void shouldDeactivateSecondaryCnae() {
        // Given
        Cnae cnae = Cnae.create(validCode, validDescription, null, null, null, null, null, false, companyId);
        assertTrue(cnae.isActive());
        
        // When
        cnae.deactivate();
        
        // Then
        assertFalse(cnae.isActive());
        assertNotNull(cnae.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Não deve desativar CNAE principal")
    void shouldNotDeactivatePrincipalCnae() {
        // Given
        Cnae cnae = Cnae.create(validCode, validDescription, null, null, null, null, null, true, companyId);
        assertTrue(cnae.isPrincipal());
        
        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            cnae.deactivate();
        });
    }
    
    @Test
    @DisplayName("Deve definir CNAE como principal")
    void shouldSetAsPrincipal() {
        // Given
        Cnae cnae = Cnae.create(validCode, validDescription, null, null, null, null, null, false, companyId);
        assertFalse(cnae.isPrincipal());
        
        // When
        cnae.setAsPrincipal();
        
        // Then
        assertTrue(cnae.isPrincipal());
        assertTrue(cnae.isActive()); // Principal deve estar ativo
        assertNotNull(cnae.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve remover status de principal")
    void shouldUnsetAsPrincipal() {
        // Given
        Cnae cnae = Cnae.create(validCode, validDescription, null, null, null, null, null, true, companyId);
        assertTrue(cnae.isPrincipal());
        
        // When
        cnae.unsetAsPrincipal();
        
        // Then
        assertFalse(cnae.isPrincipal());
        assertNotNull(cnae.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve atualizar descrição")
    void shouldUpdateDescription() {
        // Given
        Cnae cnae = Cnae.create(validCode, validDescription, null, null, null, null, null, false, companyId);
        String newDescription = "Nova descrição da atividade";
        
        // When
        cnae.updateDescription(newDescription);
        
        // Then
        assertEquals(newDescription, cnae.getDescription());
        assertNotNull(cnae.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve atualizar código")
    void shouldUpdateCode() {
        // Given
        Cnae cnae = Cnae.create(validCode, validDescription, null, null, null, null, null, false, companyId);
        String newCode = "7654321";
        
        // When
        cnae.updateCode(newCode);
        
        // Then
        assertEquals(newCode, cnae.getCode());
        assertNotNull(cnae.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve criar CNAE usando método of")
    void shouldCreateCnaeUsingOfMethod() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        
        // When
        Cnae cnae = Cnae.of(
            id, validCode, validDescription, "J", "62", "620", "6201", "62015",
            true, true, companyId, createdAt, updatedAt
        );
        
        // Then
        assertEquals(id, cnae.getId());
        assertEquals(validCode, cnae.getCode());
        assertEquals(validDescription, cnae.getDescription());
        assertTrue(cnae.isActive());
        assertTrue(cnae.isPrincipal());
        assertEquals(createdAt, cnae.getCreatedAt());
        assertEquals(updatedAt, cnae.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve implementar equals corretamente")
    void shouldImplementEqualsCorrectly() {
        // Given
        UUID id = UUID.randomUUID();
        Cnae cnae1 = Cnae.of(id, validCode, validDescription, null, null, null, null, null, true, true, companyId, null, null);
        Cnae cnae2 = Cnae.of(id, validCode, validDescription, null, null, null, null, null, true, true, companyId, null, null);
        Cnae cnae3 = Cnae.of(UUID.randomUUID(), validCode, validDescription, null, null, null, null, null, true, true, companyId, null, null);
        
        // When & Then
        assertEquals(cnae1, cnae2);
        assertNotEquals(cnae1, cnae3);
        assertEquals(cnae1.hashCode(), cnae2.hashCode());
    }
    
    @Test
    @DisplayName("Deve implementar toString corretamente")
    void shouldImplementToStringCorrectly() {
        // Given
        Cnae cnae = Cnae.create(validCode, validDescription, null, null, null, null, null, true, companyId);
        
        // When
        String toString = cnae.toString();
        
        // Then
        assertTrue(toString.contains("Cnae{"));
        assertTrue(toString.contains("id="));
        assertTrue(toString.contains("code='" + validCode + "'"));
        assertTrue(toString.contains("description='" + validDescription + "'"));
        assertTrue(toString.contains("principal=true"));
        assertTrue(toString.contains("active=true"));
        assertTrue(toString.contains("companyId=" + companyId));
    }
}
