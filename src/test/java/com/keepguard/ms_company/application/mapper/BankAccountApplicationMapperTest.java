package com.keepguard.ms_company.application.mapper;

import com.keepguard.ms_company.adapters.in.rest.bankaccount.dto.BankAccountCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.bankaccount.dto.BankAccountResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.bankaccount.dto.BankAccountUpdateDTO;
import com.keepguard.ms_company.application.mapper.BankAccountApplicationMapper;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountCreateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountViewDTO;
import com.keepguard.ms_company.domain.entity.BankAccount;
import com.keepguard.ms_company.domain.enums.AccountTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para BankAccountMapper
 * Testa conversões entre DTOs e entidades de BankAccount
 */
@DisplayName("BankAccount Application Mapper Tests")
class BankAccountApplicationMapperTest {
    
    private BankAccountApplicationMapper bankAccountMapper;
    private UUID bankAccountId;
    private UUID companyId;
    
    @BeforeEach
    void setUp() {
        bankAccountMapper = new BankAccountApplicationMapper();
        bankAccountId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }
    
    @Test
    @DisplayName("Deve converter BankAccountCreateCommandDTO para BankAccount")
    void shouldConvertCreateCommandToBankAccount() {
        // Given
        BankAccountCreateCommandDTO command = new BankAccountCreateCommandDTO(
            "001",
            "1234",
            "5",
            "12345678",
            "9",
            AccountTypeEnum.CORRENTE
        );
        
        // When
        BankAccount bankAccount = bankAccountMapper.toDomain(command);
        
        // Then
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
    @DisplayName("Deve converter BankAccountUpdateCommandDTO para BankAccount com dados existentes")
    void shouldConvertUpdateCommandToBankAccountWithExistingData() {
        // Given
        BankAccount existingBankAccount = BankAccount.of(
            bankAccountId,
            "001",
            "1234",
            "5",
            "12345678",
            "9",
            AccountTypeEnum.CORRENTE,
            true
        );
        
        BankAccountUpdateCommandDTO command = new BankAccountUpdateCommandDTO(
            "001",
            "5678",
            "0",
            "87654321",
            "1",
            AccountTypeEnum.POUPANCA
        );
        
        // When
        BankAccount updatedBankAccount = bankAccountMapper.toDomain(command, existingBankAccount);
        
        // Then
        assertNotNull(updatedBankAccount);
        assertEquals(bankAccountId, updatedBankAccount.getId());
        assertEquals("001", updatedBankAccount.getCode());
        assertEquals("5678", updatedBankAccount.getAgency());
        assertEquals("0", updatedBankAccount.getAgencyDigit());
        assertEquals("87654321", updatedBankAccount.getAccountNumber());
        assertEquals("1", updatedBankAccount.getAccountDigit());
        assertEquals(AccountTypeEnum.POUPANCA, updatedBankAccount.getAccountType());
        assertTrue(updatedBankAccount.isActive());
    }
    
    @Test
    @DisplayName("Deve converter BankAccountUpdateCommandDTO parcial para BankAccount")
    void shouldConvertPartialUpdateCommandToBankAccount() {
        // Given
        BankAccount existingBankAccount = BankAccount.of(
            bankAccountId,
            "001",
            "1234",
            "5",
            "12345678",
            "9",
            AccountTypeEnum.CORRENTE,
            true
        );
        
        BankAccountUpdateCommandDTO command = new BankAccountUpdateCommandDTO(
            "001",
            null,
            null,
            null,
            null,
            null
        );
        
        // When
        BankAccount updatedBankAccount = bankAccountMapper.toDomain(command, existingBankAccount);
        
        // Then
        assertNotNull(updatedBankAccount);
        assertEquals(bankAccountId, updatedBankAccount.getId());
        assertEquals("001", updatedBankAccount.getCode());
        assertEquals("1234", updatedBankAccount.getAgency()); // Mantém valor existente
        assertEquals("5", updatedBankAccount.getAgencyDigit()); // Mantém valor existente
        assertEquals("12345678", updatedBankAccount.getAccountNumber()); // Mantém valor existente
        assertEquals("9", updatedBankAccount.getAccountDigit()); // Mantém valor existente
        assertEquals(AccountTypeEnum.CORRENTE, updatedBankAccount.getAccountType()); // Mantém valor existente
        assertTrue(updatedBankAccount.isActive());
    }
    
    @Test
    @DisplayName("Deve converter BankAccount para BankAccountViewDTO")
    void shouldConvertBankAccounttoViewDTO() {
        // Given
        BankAccount bankAccount = BankAccount.of(
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
        BankAccountViewDTO view = bankAccountMapper.toViewDTO(bankAccount, companyId);
        
        // Then
        assertNotNull(view);
        assertEquals(bankAccountId, view.id());
        assertEquals(companyId, view.companyId());
        assertEquals("001", view.code());
        assertEquals("1234", view.agency());
        assertEquals("5", view.agencyDigit());
        assertEquals("12345678", view.accountNumber());
        assertEquals("9", view.accountDigit());
        assertEquals(AccountTypeEnum.CORRENTE, view.accountType());
        assertTrue(view.active());
    }
    
    @Test
    @DisplayName("Deve converter BankAccountCreateDTO para BankAccountCreateCommandDTO")
    void shouldConvertCreateDTOToCreateCommand() {
        // Given
        BankAccountCreateDTO dto = new BankAccountCreateDTO();
        dto.setCode("001");
        dto.setAgency("1234");
        dto.setAgencyDigit("5");
        dto.setAccountNumber("12345678");
        dto.setAccountDigit("9");
        dto.setAccountType(AccountTypeEnum.CORRENTE);
        
        // When
        BankAccountCreateCommandDTO command = bankAccountMapper.toCreateCommand(dto);
        
        // Then
        assertNotNull(command);
        assertEquals("001", command.code());
        assertEquals("1234", command.agency());
        assertEquals("5", command.agencyDigit());
        assertEquals("12345678", command.accountNumber());
        assertEquals("9", command.accountDigit());
        assertEquals(AccountTypeEnum.CORRENTE, command.accountType());
    }
    
    @Test
    @DisplayName("Deve converter BankAccountUpdateDTO para BankAccountUpdateCommandDTO")
    void shouldConvertUpdateDTOToUpdateCommand() {
        // Given
        BankAccountUpdateDTO dto = new BankAccountUpdateDTO();
        dto.setCode("001");
        dto.setAgency("5678");
        dto.setAgencyDigit("0");
        dto.setAccountNumber("87654321");
        dto.setAccountDigit("1");
        dto.setAccountType(AccountTypeEnum.POUPANCA);
        
        // When
        BankAccountUpdateCommandDTO command = bankAccountMapper.toUpdateCommand(dto);
        
        // Then
        assertNotNull(command);
        assertEquals("001", command.code());
        assertEquals("5678", command.agency());
        assertEquals("0", command.agencyDigit());
        assertEquals("87654321", command.accountNumber());
        assertEquals("1", command.accountDigit());
        assertEquals(AccountTypeEnum.POUPANCA, command.accountType());
    }
    
    @Test
    @DisplayName("Deve converter BankAccountViewDTO para BankAccountResponseDTO")
    void shouldConvertViewToResponseDTO() {
        // Given
        BankAccountViewDTO view = new BankAccountViewDTO(
            bankAccountId,
            companyId,
            "001",
            "1234",
            "5",
            "12345678",
            "9",
            AccountTypeEnum.CORRENTE,
            true
        );
        
        // When
        BankAccountResponseDTO responseDTO = bankAccountMapper.toResponseDTO(view);
        
        // Then
        assertNotNull(responseDTO);
        assertEquals(bankAccountId, responseDTO.getId());
        assertEquals(companyId, responseDTO.getCompanyId());
        assertEquals("001", responseDTO.getCode());
        assertEquals("1234", responseDTO.getAgency());
        assertEquals("5", responseDTO.getAgencyDigit());
        assertEquals("12345678", responseDTO.getAccountNumber());
        assertEquals("9", responseDTO.getAccountDigit());
        assertEquals(AccountTypeEnum.CORRENTE, responseDTO.getAccountType());
        assertTrue(responseDTO.isActive());
    }
    
    @Test
    @DisplayName("Deve retornar null quando converter objeto nulo")
    void shouldReturnNullWhenConvertingNullObject() {
        // When & Then
        assertNull(bankAccountMapper.toDomain((BankAccountCreateCommandDTO) null));
        assertNull(bankAccountMapper.toDomain((BankAccountUpdateCommandDTO) null, null));
        assertNull(bankAccountMapper.toViewDTO(null, companyId));
        assertNull(bankAccountMapper.toCreateCommand(null));
        assertNull(bankAccountMapper.toUpdateCommand(null));
        assertNull(bankAccountMapper.toResponseDTO(null));
    }
    
    @Test
    @DisplayName("Deve converter BankAccountUpdateCommandDTO com campos nulos")
    void shouldConvertUpdateCommandWithNullFields() {
        // Given
        BankAccount existingBankAccount = BankAccount.of(
            bankAccountId,
            "001",
            "1234",
            "5",
            "12345678",
            "9",
            AccountTypeEnum.CORRENTE,
            true
        );
        
        BankAccountUpdateCommandDTO command = new BankAccountUpdateCommandDTO(
            null,
            null,
            null,
            null,
            null,
            null
        );
        
        // When
        BankAccount updatedBankAccount = bankAccountMapper.toDomain(command, existingBankAccount);
        
        // Then
        assertNotNull(updatedBankAccount);
        assertEquals(bankAccountId, updatedBankAccount.getId());
        assertEquals("001", updatedBankAccount.getCode()); // Mantém valor existente
        assertEquals("1234", updatedBankAccount.getAgency()); // Mantém valor existente
        assertEquals("5", updatedBankAccount.getAgencyDigit()); // Mantém valor existente
        assertEquals("12345678", updatedBankAccount.getAccountNumber()); // Mantém valor existente
        assertEquals("9", updatedBankAccount.getAccountDigit()); // Mantém valor existente
        assertEquals(AccountTypeEnum.CORRENTE, updatedBankAccount.getAccountType()); // Mantém valor existente
        assertTrue(updatedBankAccount.isActive());
    }
    
    @Test
    @DisplayName("Deve converter BankAccountViewDTO sem dígito da agência")
    void shouldConvertViewWithoutAgencyDigit() {
        // Given
        BankAccount bankAccount = BankAccount.of(
            bankAccountId,
            "001",
            "1234",
            null,
            "12345678",
            "9",
            AccountTypeEnum.CORRENTE,
            true
        );
        
        // When
        BankAccountViewDTO view = bankAccountMapper.toViewDTO(bankAccount, companyId);
        
        // Then
        assertNotNull(view);
        assertEquals(bankAccountId, view.id());
        assertEquals(companyId, view.companyId());
        assertEquals("001", view.code());
        assertEquals("1234", view.agency());
        assertNull(view.agencyDigit());
        assertEquals("12345678", view.accountNumber());
        assertEquals("9", view.accountDigit());
        assertEquals(AccountTypeEnum.CORRENTE, view.accountType());
        assertTrue(view.active());
    }
}
