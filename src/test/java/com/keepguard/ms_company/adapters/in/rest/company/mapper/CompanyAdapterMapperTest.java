package com.keepguard.ms_company.adapters.in.rest.company.mapper;

import com.keepguard.ms_company.adapters.in.rest.address.mapper.AddressAdapterMapper;
import com.keepguard.ms_company.adapters.in.rest.bankaccount.mapper.BankAccountAdapterMapper;
import com.keepguard.ms_company.adapters.in.rest.cnae.mapper.CnaeAdapterMapper;
import com.keepguard.ms_company.adapters.in.rest.contact.mapper.ContactAdapterMapper;
import com.keepguard.ms_company.adapters.in.rest.representative.mapper.RepresentativeAdapterMapper;
import com.keepguard.ms_company.adapters.in.rest.company.dto.request.CompanyCreateRequestDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.request.CompanyUpdateRequestDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.response.CompanyResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.response.CompanySimpleResponseDTO;
import com.keepguard.ms_company.application.dto.company.CompanyCreateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanySimpleViewDTO;
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
    @DisplayName("Deve mapear CompanyCreateRequestDTO para CompanyCreateCommandDTO com sucesso")
    void shouldMapCompanyCreateDTOToCompanyCreateCommandDTOSuccessfully() {
        // Given
        CompanyCreateRequestDTO dto = CompanyTestBuilder.builder()
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
    @DisplayName("Deve retornar null quando CompanyCreateRequestDTO for null")
    void shouldReturnNullWhenCompanyCreateDTOIsNull() {
        // When
        CompanyCreateCommandDTO result = companyAdapterMapper.toCreateCommand(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear CompanyUpdateRequestDTO para CompanyUpdateCommandDTO com sucesso")
    void shouldMapCompanyUpdateDTOToCompanyUpdateCommandDTOSuccessfully() {
        // Given
        CompanyUpdateRequestDTO dto = CompanyTestBuilder.builder()
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
    @DisplayName("Deve retornar null quando CompanyUpdateRequestDTO for null")
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
    @DisplayName("Deve mapear CompanySimpleViewDTO para CompanySimpleResponseDTO com sucesso")
    void shouldMapCompanyViewDTOToCompanySimpleResponseDTOSuccessfully() {
        // Given
        CompanySimpleViewDTO view = CompanyTestBuilder.builder()
            .withTechCompany()
            .buildSimpleViewDTO();
        
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
    @DisplayName("Deve retornar null quando CompanySimpleViewDTO for null no toSimpleResponseDTO")
    void shouldReturnNullWhenCompanyViewDTOIsNullInToSimpleResponseDTO() {
        // When
        CompanySimpleResponseDTO result = companyAdapterMapper.toSimpleResponseDTO(null);
        
        // Then
        assertNull(result);
    }
}
