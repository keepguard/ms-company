package com.keepguard.ms_company.application.mapper;

import com.keepguard.ms_company.application.dto.company.CompanyCreateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyViewDTO;
import com.keepguard.ms_company.domain.entity.Company;
import com.keepguard.ms_company.domain.enums.CompanyStatusEnum;
import com.keepguard.ms_company.test.builder.CompanyTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para CompanyApplicationMapper
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Company Application Mapper Tests")
class CompanyApplicationMapperTest {
    
    private CompanyApplicationMapper companyMapper;
    
    @BeforeEach
    void setUp() {
        companyMapper = new CompanyApplicationMapper();
    }
    
    @Test
    @DisplayName("Deve mapear CompanyCreateCommandDTO para Company com sucesso")
    void shouldMapCompanyCreateCommandDTOToCompanySuccessfully() {
        // Given
        CompanyCreateCommandDTO command = CompanyTestBuilder.builder()
            .withTechCompany()
            .buildCreateCommand();
        
        // When
        Company result = companyMapper.toDomain(command);
        
        // Then
        assertNotNull(result);
        assertEquals(command.name(), result.getName());
        assertEquals(command.legalName(), result.getLegalName());
        assertEquals(command.cnpj(), result.getCnpj());
        assertEquals(command.stateRegistration(), result.getStateRegistration());
        assertEquals(command.municipalRegistration(), result.getMunicipalRegistration());
        assertEquals(command.taxRegime(), result.getTaxRegime());
        assertEquals(command.ein(), result.getEin());
        assertEquals(CompanyStatusEnum.PENDING_APPROVAL, result.getStatus());
    }
    
    @Test
    @DisplayName("Deve retornar null quando CompanyCreateCommandDTO for null")
    void shouldReturnNullWhenCompanyCreateCommandDTOIsNull() {
        // When
        Company result = companyMapper.toDomain((CompanyCreateCommandDTO) null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear CompanyUpdateCommandDTO para Company com sucesso")
    void shouldMapCompanyUpdateCommandDTOToCompanySuccessfully() {
        // Given
        Company existingCompany = CompanyTestBuilder.builder()
            .withRetailCompany()
            .buildDomain();
        
        CompanyUpdateCommandDTO command = CompanyTestBuilder.builder()
            .withTechCompany()
            .buildUpdateCommand();
        
        // When
        Company result = companyMapper.toDomain(command, existingCompany);
        
        // Then
        assertNotNull(result);
        assertEquals(existingCompany.getId(), result.getId());
        assertEquals(command.name(), result.getName());
        assertEquals(command.legalName(), result.getLegalName());
        assertEquals(existingCompany.getCnpj(), result.getCnpj()); // CNPJ não pode ser alterado
        assertEquals(command.stateRegistration(), result.getStateRegistration());
        assertEquals(command.municipalRegistration(), result.getMunicipalRegistration());
        assertEquals(command.taxRegime(), result.getTaxRegime());
        assertEquals(command.ein(), result.getEin());
    }
    
    @Test
    @DisplayName("Deve retornar empresa existente quando CompanyUpdateCommandDTO for null")
    void shouldReturnExistingCompanyWhenCompanyUpdateCommandDTOIsNull() {
        // Given
        Company existingCompany = CompanyTestBuilder.builder()
            .withRetailCompany()
            .buildDomain();
        
        // When
        Company result = companyMapper.toDomain(null, existingCompany);
        
        // Then
        assertNotNull(result);
        assertEquals(existingCompany.getId(), result.getId());
        assertEquals(existingCompany.getName(), result.getName());
        assertEquals(existingCompany.getLegalName(), result.getLegalName());
    }
    
    @Test
    @DisplayName("Deve retornar null quando empresa existente for null")
    void shouldReturnNullWhenExistingCompanyIsNull() {
        // Given
        CompanyUpdateCommandDTO command = CompanyTestBuilder.builder()
            .withTechCompany()
            .buildUpdateCommand();
        
        // When
        Company result = companyMapper.toDomain(command, null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear Company para CompanyViewDTO com sucesso")
    void shouldMapCompanyToCompanyViewDTOSuccessfully() {
        // Given
        Company company = CompanyTestBuilder.builder()
            .withTechCompany()
            .buildDomain();
        
        // When
        CompanyViewDTO result = companyMapper.toViewDTO(company);
        
        // Then
        assertNotNull(result);
        assertEquals(company.getId(), result.id());
        assertEquals(company.getCodeCompany(), result.codeCompany());
        assertEquals(company.getTenantId(), result.tenantId());
        assertEquals(company.getName(), result.name());
        assertEquals(company.getLegalName(), result.legalName());
        assertEquals(company.getCnpj(), result.cnpj());
        assertEquals(company.getStateRegistration(), result.stateRegistration());
        assertEquals(company.getMunicipalRegistration(), result.municipalRegistration());
        assertEquals(company.getTaxRegime(), result.taxRegime());
        assertEquals(company.getEin(), result.ein());
        assertEquals(company.getStatus(), result.status());
        assertEquals(company.getCreatedAt(), result.createdAt());
        assertEquals(company.getUpdatedAt(), result.updatedAt());
    }
    
    @Test
    @DisplayName("Deve retornar null quando Company for null")
    void shouldReturnNullWhenCompanyIsNull() {
        // When
        CompanyViewDTO result = companyMapper.toViewDTO(null);
        
        // Then
        assertNull(result);
    }
}