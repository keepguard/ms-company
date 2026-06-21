package com.keepguard.ms_company.adapters.in.rest.company.mapper;

import com.keepguard.ms_company.adapters.in.rest.address.mapper.AddressAdapterMapper;
import com.keepguard.ms_company.adapters.in.rest.bankaccount.mapper.BankAccountAdapterMapper;
import com.keepguard.ms_company.adapters.in.rest.cnae.mapper.CnaeAdapterMapper;
import com.keepguard.ms_company.adapters.in.rest.contact.mapper.ContactAdapterMapper;
import com.keepguard.ms_company.adapters.in.rest.representative.mapper.RepresentativeAdapterMapper;
import com.keepguard.ms_company.adapters.in.rest.company.dto.request.CompanyCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.request.CompanyUpdateDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.response.CompanyResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.response.CompanySimpleResponseDTO;
import com.keepguard.ms_company.application.dto.company.CompanyCreateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyViewDTO;
import com.keepguard.ms_company.test.builder.CompanyTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para CompanyAdapterMapper
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Company Adapter Mapper Tests")
class CompanyAdapterMapperTest {
    
    @Mock
    private AddressAdapterMapper addressAdapterMapper;
    
    @Mock
    private ContactAdapterMapper contactAdapterMapper;
    
    @Mock
    private RepresentativeAdapterMapper representativeAdapterMapper;
    
    @Mock
    private BankAccountAdapterMapper bankAccountAdapterMapper;
    
    @Mock
    private CnaeAdapterMapper cnaeAdapterMapper;
    
    private CompanyAdapterMapper companyAdapterMapper;
    
    @BeforeEach
    void setUp() {
        companyAdapterMapper = new CompanyAdapterMapper(
            addressAdapterMapper,
            contactAdapterMapper,
            representativeAdapterMapper,
            bankAccountAdapterMapper,
            cnaeAdapterMapper
        );
    }
    
    @Test
    @DisplayName("Deve mapear CompanyCreateDTO para CompanyCreateCommandDTO com sucesso")
    void shouldMapCompanyCreateDTOToCompanyCreateCommandDTOSuccessfully() {
        // Given
        CompanyCreateDTO dto = CompanyTestBuilder.builder()
            .withTechCompany()
            .buildCreateDTO();
        
        // When
        CompanyCreateCommandDTO result = companyAdapterMapper.toCreateCommand(dto);
        
        // Then
        assertNotNull(result);
        assertEquals(dto.getName(), result.name());
        assertEquals(dto.getLegalName(), result.legalName());
        assertEquals(dto.getCnpj(), result.cnpj());
        assertEquals(dto.getStateRegistration(), result.stateRegistration());
        assertEquals(dto.getMunicipalRegistration(), result.municipalRegistration());
        assertEquals(dto.getTaxRegime(), result.taxRegime());
        assertEquals(dto.getEin(), result.ein());
    }
    
    @Test
    @DisplayName("Deve retornar null quando CompanyCreateDTO for null")
    void shouldReturnNullWhenCompanyCreateDTOIsNull() {
        // When
        CompanyCreateCommandDTO result = companyAdapterMapper.toCreateCommand(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear CompanyUpdateDTO para CompanyUpdateCommandDTO com sucesso")
    void shouldMapCompanyUpdateDTOToCompanyUpdateCommandDTOSuccessfully() {
        // Given
        CompanyUpdateDTO dto = CompanyTestBuilder.builder()
            .withRetailCompany()
            .buildUpdateDTO();
        
        // When
        CompanyUpdateCommandDTO result = companyAdapterMapper.toUpdateCommand(dto);
        
        // Then
        assertNotNull(result);
        assertEquals(dto.getName(), result.name());
        assertEquals(dto.getLegalName(), result.legalName());
        assertEquals(dto.getStateRegistration(), result.stateRegistration());
        assertEquals(dto.getMunicipalRegistration(), result.municipalRegistration());
        assertEquals(dto.getTaxRegime(), result.taxRegime());
        assertEquals(dto.getEin(), result.ein());
    }
    
    @Test
    @DisplayName("Deve retornar null quando CompanyUpdateDTO for null")
    void shouldReturnNullWhenCompanyUpdateDTOIsNull() {
        // When
        CompanyUpdateCommandDTO result = companyAdapterMapper.toUpdateCommand(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear CompanyViewDTO para CompanyResponseDTO com sucesso")
    void shouldMapCompanyViewDTOToCompanyResponseDTOSuccessfully() {
        // Given
        CompanyViewDTO view = CompanyTestBuilder.builder()
            .withTechCompany()
            .buildView();
        
        // When
        CompanyResponseDTO result = companyAdapterMapper.toResponseDTO(view);
        
        // Then
        assertNotNull(result);
        assertEquals(view.id(), result.getId());
        assertEquals(view.name(), result.getName());
        assertEquals(view.legalName(), result.getLegalName());
        assertEquals(view.cnpj(), result.getCnpj());
        assertEquals(view.stateRegistration(), result.getStateRegistration());
        assertEquals(view.municipalRegistration(), result.getMunicipalRegistration());
        assertEquals(view.taxRegime(), result.getTaxRegime());
        assertEquals(view.ein(), result.getEin());
        assertEquals(view.status(), result.getStatus());
        assertEquals(view.createdAt(), result.getCreatedAt());
        assertEquals(view.updatedAt(), result.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve retornar null quando CompanyViewDTO for null")
    void shouldReturnNullWhenCompanyViewDTOIsNull() {
        // When
        CompanyResponseDTO result = companyAdapterMapper.toResponseDTO(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear CompanyViewDTO para CompanySimpleResponseDTO com sucesso")
    void shouldMapCompanyViewDTOToCompanySimpleResponseDTOSuccessfully() {
        // Given
        CompanyViewDTO view = CompanyTestBuilder.builder()
            .withTechCompany()
            .buildView();
        
        // When
        CompanySimpleResponseDTO result = companyAdapterMapper.toSimpleResponseDTO(view);
        
        // Then
        assertNotNull(result);
        assertEquals(view.id(), result.getId());
        assertEquals(view.name(), result.getName());
        assertEquals(view.legalName(), result.getLegalName());
        assertEquals(view.cnpj(), result.getCnpj());
        assertEquals(view.stateRegistration(), result.getStateRegistration());
        assertEquals(view.municipalRegistration(), result.getMunicipalRegistration());
        assertEquals(view.taxRegime(), result.getTaxRegime());
        assertEquals(view.ein(), result.getEin());
        assertEquals(view.status(), result.getStatus());
        assertEquals(view.createdAt(), result.getCreatedAt());
        assertEquals(view.updatedAt(), result.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve retornar null quando CompanyViewDTO for null no toSimpleResponseDTO")
    void shouldReturnNullWhenCompanyViewDTOIsNullInToSimpleResponseDTO() {
        // When
        CompanySimpleResponseDTO result = companyAdapterMapper.toSimpleResponseDTO(null);
        
        // Then
        assertNull(result);
    }
}
