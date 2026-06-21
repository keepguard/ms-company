package com.keepguard.ms_company.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a entidade Representative
 * Testa apenas lógica de domínio, sem dependências de frameworks
 */
@DisplayName("Representative Domain Tests")
class RepresentativeTest {
    
    private Representative representative;
    private UUID representativeId;
    
    @BeforeEach
    void setUp() {
        representativeId = UUID.randomUUID();
        representative = Representative.create(
            "João Silva",
            "11144477735",
            "123456789",
            LocalDate.of(1990, 1, 1),
            "joao.silva@empresa.com",
            "11999999999",
            "Diretor"
        );
    }
    
    @Test
    @DisplayName("Deve criar representante com dados válidos")
    void shouldCreateRepresentativeWithValidData() {
        assertNotNull(representative);
        assertEquals("João Silva", representative.getName());
        assertEquals("11144477735", representative.getCpf());
        assertEquals("123456789", representative.getRg());
        assertEquals(LocalDate.of(1990, 1, 1), representative.getBirthDate());
        assertEquals("joao.silva@empresa.com", representative.getEmail());
        assertEquals("11999999999", representative.getPhone());
        assertEquals("Diretor", representative.getRole());
        assertTrue(representative.isActive());
    }
    
    @Test
    @DisplayName("Deve criar representante com ID específico")
    void shouldCreateRepresentativeWithSpecificId() {
        Representative representativeWithId = Representative.of(
            representativeId, "João Silva", "11144477735", "123456789",
            LocalDate.of(1990, 1, 1), "joao.silva@empresa.com", "11999999999", "Diretor", true
        );
        
        assertEquals(representativeId, representativeWithId.getId());
        assertEquals("João Silva", representativeWithId.getName());
        assertTrue(representativeWithId.isActive());
    }
    
    @Test
    @DisplayName("Deve ativar representante")
    void shouldActivateRepresentative() {
        representative.deactivate();
        assertFalse(representative.isActive());
        
        representative.activate();
        assertTrue(representative.isActive());
    }
    
    @Test
    @DisplayName("Deve desativar representante")
    void shouldDeactivateRepresentative() {
        assertTrue(representative.isActive());
        
        representative.deactivate();
        assertFalse(representative.isActive());
    }
    
    @Test
    @DisplayName("Deve limpar espaços em branco do nome")
    void shouldTrimNameWhitespace() {
        Representative representativeWithSpaces = Representative.create(
            "  João Silva  ",
            "11144477735",
            "123456789",
            LocalDate.of(1990, 1, 1),
            "joao.silva@empresa.com",
            "11999999999",
            "Diretor"
        );
        
        assertEquals("João Silva", representativeWithSpaces.getName());
    }
    
    @Test
    @DisplayName("Deve limpar formatação do CPF")
    void shouldCleanCpfFormatting() {
        Representative representativeWithFormattedCpf = Representative.create(
            "João Silva",
            "11144477735",
            "123456789",
            LocalDate.of(1990, 1, 1),
            "joao.silva@empresa.com",
            "11999999999",
            "Diretor"
        );
        
        assertEquals("11144477735", representativeWithFormattedCpf.getCpf());
    }
    
    @Test
    @DisplayName("Deve limpar formatação do telefone")
    void shouldCleanPhoneFormatting() {
        Representative representativeWithFormattedPhone = Representative.create(
            "João Silva",
            "11144477735",
            "123456789",
            LocalDate.of(1990, 1, 1),
            "joao.silva@empresa.com",
            "(11) 99999-9999",
            "Diretor"
        );
        
        assertEquals("(11) 99999-9999", representativeWithFormattedPhone.getPhone());
    }
    
    @Test
    @DisplayName("Deve normalizar email para minúsculas")
    void shouldNormalizeEmailToLowerCase() {
        Representative representativeWithUpperCaseEmail = Representative.create(
            "João Silva",
            "11144477735",
            "123456789",
            LocalDate.of(1990, 1, 1),
            "JOAO.SILVA@EMPRESA.COM",
            "11999999999",
            "Diretor"
        );
        
        assertEquals("joao.silva@empresa.com", representativeWithUpperCaseEmail.getEmail());
    }
    
    @Test
    @DisplayName("Deve permitir RG nulo")
    void shouldAllowNullRg() {
        Representative representativeWithoutRg = Representative.create(
            "João Silva",
            "11144477735",
            null,
            LocalDate.of(1990, 1, 1),
            "joao.silva@empresa.com",
            "11999999999",
            "Diretor"
        );
        
        assertNull(representativeWithoutRg.getRg());
    }
    
    @Test
    @DisplayName("Deve permitir cargo nulo")
    void shouldAllowNullRole() {
        Representative representativeWithoutRole = Representative.create(
            "João Silva",
            "11144477735",
            "123456789",
            LocalDate.of(1990, 1, 1),
            "joao.silva@empresa.com",
            "11999999999",
            null
        );
        
        assertNull(representativeWithoutRole.getRole());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando nome for nulo")
    void shouldThrowExceptionWhenNameIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Representative.create(
                null, "11144477735", "123456789", LocalDate.of(1990, 1, 1),
                "joao.silva@empresa.com", "11999999999", "Diretor"
            )
        );
        
        assertEquals("Nome do representante é obrigatório", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando nome for vazio")
    void shouldThrowExceptionWhenNameIsEmpty() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Representative.create(
                "   ", "11144477735", "123456789", LocalDate.of(1990, 1, 1),
                "joao.silva@empresa.com", "11999999999", "Diretor"
            )
        );
        
        assertEquals("Nome do representante é obrigatório", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando nome for muito longo")
    void shouldThrowExceptionWhenNameIsTooLong() {
        String longName = "a".repeat(151);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Representative.create(
                longName, "11144477735", "123456789", LocalDate.of(1990, 1, 1),
                "joao.silva@empresa.com", "11999999999", "Diretor"
            )
        );
        
        assertEquals("Nome deve ter no máximo 150 caracteres", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando CPF for nulo")
    void shouldThrowValidationExceptionWhenCpfIsNull() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> Representative.create(
                "João Silva",
                null, // CPF nulo
                "123456789",
                LocalDate.of(1990, 1, 1),
                "joao.silva@empresa.com",
                "11999999999",
                "Diretor"
            )
        );
        
        assertEquals("CPF é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando CPF for vazio")
    void shouldThrowValidationExceptionWhenCpfIsEmpty() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> Representative.create(
                "João Silva",
                "", // CPF vazio
                "123456789",
                LocalDate.of(1990, 1, 1),
                "joao.silva@empresa.com",
                "11999999999",
                "Diretor"
            )
        );
        
        assertEquals("CPF é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando email for nulo")
    void shouldThrowValidationExceptionWhenEmailIsNull() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> Representative.create(
                "João Silva",
                "11144477735",
                "123456789",
                LocalDate.of(1990, 1, 1),
                null, // Email nulo
                "11999999999",
                "Diretor"
            )
        );
        
        assertEquals("Email do representante é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando email for vazio")
    void shouldThrowValidationExceptionWhenEmailIsEmpty() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> Representative.create(
                "João Silva",
                "11144477735",
                "123456789",
                LocalDate.of(1990, 1, 1),
                "", // Email vazio
                "11999999999",
                "Diretor"
            )
        );
        
        assertEquals("Email do representante é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando telefone for nulo")
    void shouldThrowValidationExceptionWhenPhoneIsNull() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> Representative.create(
                "João Silva",
                "11144477735",
                "123456789",
                LocalDate.of(1990, 1, 1),
                "joao.silva@empresa.com",
                null, // Telefone nulo
                "Diretor"
            )
        );
        
        assertEquals("Telefone do representante é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando telefone for vazio")
    void shouldThrowValidationExceptionWhenPhoneIsEmpty() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> Representative.create(
                "João Silva",
                "11144477735",
                "123456789",
                LocalDate.of(1990, 1, 1),
                "joao.silva@empresa.com",
                "", // Telefone vazio
                "Diretor"
            )
        );
        
        assertEquals("Telefone do representante é obrigatório", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando CPF for nulo")
    void shouldThrowExceptionWhenCpfIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Representative.create(
                "João Silva", null, "123456789", LocalDate.of(1990, 1, 1),
                "joao.silva@empresa.com", "11999999999", "Diretor"
            )
        );
        
        assertEquals("CPF é obrigatório", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando CPF for inválido")
    void shouldThrowExceptionWhenCpfIsInvalid() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Representative.create(
                "João Silva", "123456789", "123456789", LocalDate.of(1990, 1, 1),
                "joao.silva@empresa.com", "11999999999", "Diretor"
            )
        );
        
        assertEquals("CPF deve conter exatamente 11 dígitos", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando RG for muito longo")
    void shouldThrowExceptionWhenRgIsTooLong() {
        String longRg = "a".repeat(16);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Representative.create(
                "João Silva", "11144477735", longRg, LocalDate.of(1990, 1, 1),
                "joao.silva@empresa.com", "11999999999", "Diretor"
            )
        );
        
        assertEquals("RG deve ter no máximo 15 caracteres", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando data de nascimento for nula")
    void shouldThrowExceptionWhenBirthDateIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Representative.create(
                "João Silva", "11144477735", "123456789", null,
                "joao.silva@empresa.com", "11999999999", "Diretor"
            )
        );
        
        assertEquals("Data de nascimento é obrigatória", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando data de nascimento for futura")
    void shouldThrowExceptionWhenBirthDateIsFuture() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Representative.create(
                "João Silva", "11144477735", "123456789", futureDate,
                "joao.silva@empresa.com", "11999999999", "Diretor"
            )
        );
        
        assertEquals("Data de nascimento não pode ser futura", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando data de nascimento for muito antiga")
    void shouldThrowExceptionWhenBirthDateIsTooOld() {
        LocalDate tooOldDate = LocalDate.now().minusYears(121);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Representative.create(
                "João Silva", "11144477735", "123456789", tooOldDate,
                "joao.silva@empresa.com", "11999999999", "Diretor"
            )
        );
        
        assertEquals("Data de nascimento inválida", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando email for nulo")
    void shouldThrowExceptionWhenEmailIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Representative.create(
                "João Silva", "11144477735", "123456789", LocalDate.of(1990, 1, 1),
                null, "11999999999", "Diretor"
            )
        );
        
        assertEquals("Email do representante é obrigatório", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando email for inválido")
    void shouldThrowExceptionWhenEmailIsInvalid() {
        com.keepguard.lib_common.exception.InvalidEmailException exception = assertThrows(
            com.keepguard.lib_common.exception.InvalidEmailException.class,
            () -> Representative.create(
                "João Silva", "11144477735", "123456789", LocalDate.of(1990, 1, 1),
                "email-invalido", "11999999999", "Diretor"
            )
        );
        
        assertEquals("Formato de email inválido: email-invalido", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando email for muito longo")
    void shouldThrowExceptionWhenEmailIsTooLong() {
        String longEmail = "a".repeat(151) + "@empresa.com";
        
        com.keepguard.lib_common.exception.InvalidEmailException exception = assertThrows(
            com.keepguard.lib_common.exception.InvalidEmailException.class,
            () -> Representative.create(
                "João Silva", "11144477735", "123456789", LocalDate.of(1990, 1, 1),
                longEmail, "11999999999", "Diretor"
            )
        );
        
        assertEquals("Parte local do email muito longa (máximo 64 caracteres)", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando telefone for nulo")
    void shouldThrowExceptionWhenPhoneIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Representative.create(
                "João Silva", "11144477735", "123456789", LocalDate.of(1990, 1, 1),
                "joao.silva@empresa.com", null, "Diretor"
            )
        );
        
        assertEquals("Telefone do representante é obrigatório", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando telefone for muito curto")
    void shouldThrowExceptionWhenPhoneIsTooShort() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Representative.create(
                "João Silva", "11144477735", "123456789", LocalDate.of(1990, 1, 1),
                "joao.silva@empresa.com", "123456789", "Diretor"
            )
        );
        
        assertEquals("Formato de telefone inválido. Use: (XX) XXXXX-XXXX ou (XX) XXXX-XXXX", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando telefone for muito longo")
    void shouldThrowExceptionWhenPhoneIsTooLong() {
        String longPhone = "1".repeat(16);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Representative.create(
                "João Silva", "11144477735", "123456789", LocalDate.of(1990, 1, 1),
                "joao.silva@empresa.com", longPhone, "Diretor"
            )
        );
        
        assertEquals("Formato de telefone inválido. Use: (XX) XXXXX-XXXX ou (XX) XXXX-XXXX", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando cargo for muito longo")
    void shouldThrowExceptionWhenRoleIsTooLong() {
        String longRole = "a".repeat(101);
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Representative.create(
                "João Silva", "11144477735", "123456789", LocalDate.of(1990, 1, 1),
                "joao.silva@empresa.com", "11999999999", longRole
            )
        );
        
        assertEquals("Cargo deve ter no máximo 100 caracteres", exception.getMessage());
    }
    
    @Test
    @DisplayName("Deve comparar representantes por ID")
    void shouldCompareRepresentativesById() {
        Representative representative1 = Representative.of(
            representativeId, "João Silva", "11144477735", "123456789",
            LocalDate.of(1990, 1, 1), "joao.silva@empresa.com", "11999999999", "Diretor", true
        );
        
        Representative representative2 = Representative.of(
            representativeId, "Maria Silva", "98765432100", "987654321",
            LocalDate.of(1985, 5, 15), "maria.silva@empresa.com", "11888888888", "Gerente", false
        );
        
        assertEquals(representative1, representative2);
        assertEquals(representative1.hashCode(), representative2.hashCode());
    }
    
    @Test
    @DisplayName("Deve gerar ID automaticamente quando não fornecido")
    void shouldGenerateIdAutomaticallyWhenNotProvided() {
        Representative representative1 = Representative.create(
            "João Silva", "11144477735", "123456789", LocalDate.of(1990, 1, 1),
            "joao.silva@empresa.com", "11999999999", "Diretor"
        );
        
        Representative representative2 = Representative.create(
            "Maria Silva", "98765432100", "987654321", LocalDate.of(1985, 5, 15),
            "maria.silva@empresa.com", "11888888888", "Gerente"
        );
        
        assertNotNull(representative1.getId());
        assertNotNull(representative2.getId());
        assertNotEquals(representative1.getId(), representative2.getId());
    }
}
