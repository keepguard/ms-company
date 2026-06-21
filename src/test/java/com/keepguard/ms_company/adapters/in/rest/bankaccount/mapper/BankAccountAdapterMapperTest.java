package com.keepguard.ms_company.adapters.in.rest.bankaccount.mapper;

import com.keepguard.ms_company.adapters.in.rest.bankaccount.dto.BankAccountCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.bankaccount.dto.BankAccountResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.bankaccount.dto.BankAccountUpdateDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.BankAccountDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountCreateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountViewDTO;
import com.keepguard.ms_company.test.builder.BankAccountTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para BankAccountAdapterMapper
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Bank Account Adapter Mapper Tests")
class BankAccountAdapterMapperTest {
    
    private BankAccountAdapterMapper bankAccountAdapterMapper;
    private UUID bankAccountId;
    private UUID companyId;
    
    @BeforeEach
    void setUp() {
        bankAccountAdapterMapper = new BankAccountAdapterMapper();
        bankAccountId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }
    
    @Test
    @DisplayName("Deve mapear BankAccountCreateDTO para BankAccountCreateCommandDTO com sucesso")
    void shouldMapBankAccountCreateDTOToBankAccountCreateCommandDTOSuccessfully() {
        // Given
        BankAccountCreateDTO dto = BankAccountTestBuilder.builder()
            .buildCreateDTO();
        
        // When
        BankAccountCreateCommandDTO result = bankAccountAdapterMapper.toCreateCommand(dto);
        
        // Then
        assertNotNull(result);
        assertEquals(dto.getCode(), result.code());
        assertEquals(dto.getAgency(), result.agency());
        assertEquals(dto.getAgencyDigit(), result.agencyDigit());
        assertEquals(dto.getAccountNumber(), result.accountNumber());
        assertEquals(dto.getAccountDigit(), result.accountDigit());
        assertEquals(dto.getAccountType(), result.accountType());
    }
    
    @Test
    @DisplayName("Deve retornar null quando BankAccountCreateDTO for null")
    void shouldReturnNullWhenBankAccountCreateDTOIsNull() {
        // When
        BankAccountCreateCommandDTO result = bankAccountAdapterMapper.toCreateCommand(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear BankAccountUpdateDTO para BankAccountUpdateCommandDTO com sucesso")
    void shouldMapBankAccountUpdateDTOToBankAccountUpdateCommandDTOSuccessfully() {
        // Given
        BankAccountUpdateDTO dto = BankAccountTestBuilder.builder()
            .withPoupancaType()
            .buildUpdateDTO();
        
        // When
        BankAccountUpdateCommandDTO result = bankAccountAdapterMapper.toUpdateCommand(dto);
        
        // Then
        assertNotNull(result);
        assertEquals(dto.getCode(), result.code());
        assertEquals(dto.getAgency(), result.agency());
        assertEquals(dto.getAgencyDigit(), result.agencyDigit());
        assertEquals(dto.getAccountNumber(), result.accountNumber());
        assertEquals(dto.getAccountDigit(), result.accountDigit());
        assertEquals(dto.getAccountType(), result.accountType());
    }
    
    @Test
    @DisplayName("Deve retornar null quando BankAccountUpdateDTO for null")
    void shouldReturnNullWhenBankAccountUpdateDTOIsNull() {
        // When
        BankAccountUpdateCommandDTO result = bankAccountAdapterMapper.toUpdateCommand(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear BankAccountViewDTO para BankAccountResponseDTO com sucesso")
    void shouldMapBankAccountViewDTOToBankAccountResponseDTOSuccessfully() {
        // Given
        BankAccountViewDTO view = BankAccountTestBuilder.builder()
            .withId(bankAccountId)
            .withCompanyId(companyId)
            .buildView();
        
        // When
        BankAccountResponseDTO result = bankAccountAdapterMapper.toResponseDTO(view);
        
        // Then
        assertNotNull(result);
        assertEquals(view.id(), result.getId());
        assertEquals(view.companyId(), result.getCompanyId());
        assertEquals(view.code(), result.getCode());
        assertEquals(view.agency(), result.getAgency());
        assertEquals(view.agencyDigit(), result.getAgencyDigit());
        assertEquals(view.accountNumber(), result.getAccountNumber());
        assertEquals(view.accountDigit(), result.getAccountDigit());
        assertEquals(view.accountType(), result.getAccountType());
        assertEquals(view.active(), result.isActive());
    }
    
    @Test
    @DisplayName("Deve retornar null quando BankAccountViewDTO for null")
    void shouldReturnNullWhenBankAccountViewDTOIsNull() {
        // When
        BankAccountResponseDTO result = bankAccountAdapterMapper.toResponseDTO(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear BankAccountViewDTO para BankAccountDTO com sucesso")
    void shouldMapBankAccountViewDTOToBankAccountDTOSuccessfully() {
        // Given
        BankAccountViewDTO view = BankAccountTestBuilder.builder()
            .withId(bankAccountId)
            .withCompanyId(companyId)
            .buildView();
        
        // When
        BankAccountDTO result = bankAccountAdapterMapper.toCompanyBankAccountDTO(view);
        
        // Then
        assertNotNull(result);
        assertEquals(view.code(), result.getCode());
        assertEquals(view.agency(), result.getAgency());
        assertEquals(view.agencyDigit(), result.getAgencyDigit());
        assertEquals(view.accountNumber(), result.getAccountNumber());
        assertEquals(view.accountDigit(), result.getAccountDigit());
        assertEquals(view.accountType(), result.getAccountType());
    }
    
    @Test
    @DisplayName("Deve retornar null quando BankAccountViewDTO for null no toCompanyBankAccountDTO")
    void shouldReturnNullWhenBankAccountViewDTOIsNullInToCompanyBankAccountDTO() {
        // When
        BankAccountDTO result = bankAccountAdapterMapper.toCompanyBankAccountDTO(null);
        
        // Then
        assertNull(result);
    }
}
