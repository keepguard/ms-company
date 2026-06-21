package com.keepguard.ms_company.domain.entity;

import com.keepguard.ms_company.domain.enums.AccountTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a entidade BankAccount
 * Testa apenas lógica de domínio, sem dependências de frameworks
 */
@DisplayName("BankAccount Domain Tests")
class BankAccountTest {
    
    private BankAccount bankAccount;
    private UUID bankAccountId;
    
    @BeforeEach
    void setUp() {
        bankAccountId = UUID.randomUUID();
        bankAccount = BankAccount.create(
            "001",
            "1234",
            "5",
            "12345678",
            "9",
            AccountTypeEnum.CORRENTE
        );
    }
    
    @Test
    @DisplayName("Deve criar dados bancários com dados válidos")
    void shouldCreateBankAccountWithValidData() {
        assertNotNull(bankAccount);
        assertEquals("001", bankAccount.getCode());
        assertEquals("1234", bankAccount.getAgency());
        assertEquals("5", bankAccount.getAgencyDigit());
        assertEquals("12345678", bankAccount.getAccountNumber());
        assertEquals("9", bankAccount.getAccountDigit());
        assertEquals(AccountTypeEnum.CORRENTE, bankAccount.getAccountType());
        assertTrue(bankAccount.isActive());
    }
    
    @Test
    @DisplayName("Deve criar dados bancários com ID específico")
    void shouldCreateBankAccountWithSpecificId() {
        // When
        BankAccount bankAccountWithId = BankAccount.of(
            bankAccountId,
            "001",
            "1234",
            "5",
            "12345678",
            "9",
            AccountTypeEnum.CORRENTE,
            true
        );
        
        // Then
        assertEquals(bankAccountId, bankAccountWithId.getId());
        assertEquals("001", bankAccountWithId.getCode());
        assertEquals("1234", bankAccountWithId.getAgency());
        assertEquals("5", bankAccountWithId.getAgencyDigit());
        assertEquals("12345678", bankAccountWithId.getAccountNumber());
        assertEquals("9", bankAccountWithId.getAccountDigit());
        assertEquals(AccountTypeEnum.CORRENTE, bankAccountWithId.getAccountType());
        assertTrue(bankAccountWithId.isActive());
    }
    
    @Test
    @DisplayName("Deve gerar ID automaticamente quando não fornecido")
    void shouldGenerateIdAutomaticallyWhenNotProvided() {
        // When
        BankAccount newBankAccount = BankAccount.create(
            "001",
            "1234",
            "5",
            "12345678",
            "9",
            AccountTypeEnum.CORRENTE
        );
        
        // Then
        assertNotNull(newBankAccount.getId());
        assertNotEquals(bankAccountId, newBankAccount.getId());
    }
    
    @Test
    @DisplayName("Deve ativar dados bancários")
    void shouldActivateBankAccount() {
        // Given
        BankAccount inactiveBankAccount = BankAccount.of(
            bankAccountId,
            "001",
            "1234",
            "5",
            "12345678",
            "9",
            AccountTypeEnum.CORRENTE,
            false
        );
        
        // When
        inactiveBankAccount.activate();
        
        // Then
        assertTrue(inactiveBankAccount.isActive());
    }
    
    @Test
    @DisplayName("Deve lançar ValidationException quando código bancário for nulo")
    void shouldThrowValidationExceptionWhenBankCodeIsNull() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> BankAccount.of(
                bankAccountId,
                null, // Código bancário nulo
                "1234",
                "5",
                "12345678",
                "9",
                AccountTypeEnum.CORRENTE,
                false
            )
        );
        
        assertEquals("Código do banco é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar ValidationException quando código bancário for vazio")
    void shouldThrowValidationExceptionWhenBankCodeIsEmpty() {
        // When & Then
        com.keepguard.lib_common.exception.ValidationException exception = assertThrows(
            com.keepguard.lib_common.exception.ValidationException.class,
            () -> BankAccount.of(
                bankAccountId,
                "", // Código bancário vazio
                "1234",
                "5",
                "12345678",
                "9",
                AccountTypeEnum.CORRENTE,
                false
            )
        );
        
        assertEquals("Código do banco é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve desativar dados bancários")
    void shouldDeactivateBankAccount() {
        // Given
        BankAccount activeBankAccount = BankAccount.of(
            bankAccountId,
            "001",
            "1234",
            "5",
            "12345678",
            "9",
            AccountTypeEnum.CORRENTE,
            true
        );
        
        // When
        activeBankAccount.deactivate();
        
        // Then
        assertFalse(activeBankAccount.isActive());
    }
    
    @Test
    @DisplayName("Deve validar código do banco obrigatório")
    void shouldValidateRequiredBankCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            BankAccount.create(
                null,
                "1234",
                "5",
                "12345678",
                "9",
                AccountTypeEnum.CORRENTE
            );
        });
    }
    
    @Test
    @DisplayName("Deve validar código do banco não vazio")
    void shouldValidateNonEmptyBankCode() {
        assertThrows(IllegalArgumentException.class, () -> {
            BankAccount.create(
                "   ",
                "1234",
                "5",
                "12345678",
                "9",
                AccountTypeEnum.CORRENTE
            );
        });
    }
    
    @Test
    @DisplayName("Deve validar agência obrigatória")
    void shouldValidateRequiredAgency() {
        assertThrows(IllegalArgumentException.class, () -> {
            BankAccount.create(
                "001",
                null,
                "5",
                "12345678",
                "9",
                AccountTypeEnum.CORRENTE
            );
        });
    }
    
    @Test
    @DisplayName("Deve validar número da conta obrigatório")
    void shouldValidateRequiredAccountNumber() {
        assertThrows(IllegalArgumentException.class, () -> {
            BankAccount.create(
                "001",
                "1234",
                "5",
                null,
                "9",
                AccountTypeEnum.CORRENTE
            );
        });
    }
    
    @Test
    @DisplayName("Deve validar dígito da conta obrigatório")
    void shouldValidateRequiredAccountDigit() {
        assertThrows(IllegalArgumentException.class, () -> {
            BankAccount.create(
                "001",
                "1234",
                "5",
                "12345678",
                null,
                AccountTypeEnum.CORRENTE
            );
        });
    }
    
    @Test
    @DisplayName("Deve validar tipo da conta obrigatório")
    void shouldValidateRequiredAccountType() {
        assertThrows(NullPointerException.class, () -> {
            BankAccount.create(
                "001",
                "1234",
                "5",
                "12345678",
                "9",
                null
            );
        });
    }
    
    @Test
    @DisplayName("Deve remover espaços em branco dos campos")
    void shouldTrimWhitespaceFromFields() {
        // When
        BankAccount bankAccountWithSpaces = BankAccount.create(
            "  001  ",
            "  1234  ",
            "5",
            "  12345678  ",
            "9",
            AccountTypeEnum.CORRENTE
        );
        
        // Then
        assertEquals("001", bankAccountWithSpaces.getCode());
        assertEquals("1234", bankAccountWithSpaces.getAgency());
        assertEquals("5", bankAccountWithSpaces.getAgencyDigit());
        assertEquals("12345678", bankAccountWithSpaces.getAccountNumber());
        assertEquals("9", bankAccountWithSpaces.getAccountDigit());
    }
    
    @Test
    @DisplayName("Deve considerar dados bancários iguais quando têm mesmo ID")
    void shouldConsiderBankAccountsEqualWhenSameId() {
        // Given
        BankAccount bankAccount1 = BankAccount.of(
            bankAccountId,
            "001",
            "1234",
            "5",
            "12345678",
            "9",
            AccountTypeEnum.CORRENTE,
            true
        );
        
        BankAccount bankAccount2 = BankAccount.of(
            bankAccountId,
            "001",
            "5678",
            "0",
            "87654321",
            "1",
            AccountTypeEnum.POUPANCA,
            false
        );
        
        // Then
        assertEquals(bankAccount1, bankAccount2);
        assertEquals(bankAccount1.hashCode(), bankAccount2.hashCode());
    }
    
    @Test
    @DisplayName("Deve considerar dados bancários diferentes quando têm IDs diferentes")
    void shouldConsiderBankAccountsDifferentWhenDifferentIds() {
        // Given
        BankAccount bankAccount1 = BankAccount.of(
            UUID.randomUUID(),
            "001",
            "1234",
            "5",
            "12345678",
            "9",
            AccountTypeEnum.CORRENTE,
            true
        );
        
        BankAccount bankAccount2 = BankAccount.of(
            UUID.randomUUID(),
            "001",
            "1234",
            "5",
            "12345678",
            "9",
            AccountTypeEnum.CORRENTE,
            true
        );
        
        // Then
        assertNotEquals(bankAccount1, bankAccount2);
        assertNotEquals(bankAccount1.hashCode(), bankAccount2.hashCode());
    }
    
    @Test
    @DisplayName("Deve retornar string representativa dos dados bancários")
    void shouldReturnStringRepresentation() {
        // When
        String stringRepresentation = bankAccount.toString();
        
        // Then
        assertNotNull(stringRepresentation);
        assertTrue(stringRepresentation.contains("BankAccount"));
        assertTrue(stringRepresentation.contains("001"));
        assertTrue(stringRepresentation.contains("1234"));
        assertTrue(stringRepresentation.contains("12345678"));
        assertTrue(stringRepresentation.contains("CORRENTE"));
    }
}
