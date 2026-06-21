package com.keepguard.ms_company.adapters.in.rest.representative.mapper;

import com.keepguard.ms_company.adapters.in.rest.representative.dto.RepresentativeCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.representative.dto.RepresentativeResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.representative.dto.RepresentativeUpdateDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.RepresentativeDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeViewDTO;
import com.keepguard.ms_company.test.builder.RepresentativeTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para RepresentativeAdapterMapper
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Representative Adapter Mapper Tests")
class RepresentativeAdapterMapperTest {
    
    private RepresentativeAdapterMapper representativeAdapterMapper;
    private UUID representativeId;
    private UUID companyId;
    
    @BeforeEach
    void setUp() {
        representativeAdapterMapper = new RepresentativeAdapterMapper();
        representativeId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }
    
    @Test
    @DisplayName("Deve mapear RepresentativeCreateDTO para RepresentativeCreateCommandDTO com sucesso")
    void shouldMapRepresentativeCreateDTOToRepresentativeCreateCommandDTOSuccessfully() {
        // Given
        RepresentativeCreateDTO dto = RepresentativeTestBuilder.builder()
            .buildCreateDTO();
        
        // When
        RepresentativeCreateCommandDTO result = representativeAdapterMapper.toCreateCommand(dto, companyId);
        
        // Then
        assertNotNull(result);
        assertEquals(dto.getName(), result.name());
        assertEquals(dto.getCpf(), result.cpf());
        assertEquals(dto.getRg(), result.rg());
        assertEquals(dto.getBirthDate(), result.birthDate());
        assertEquals(dto.getEmail(), result.email());
        assertEquals(dto.getPhone(), result.phone());
        assertEquals(dto.getRole(), result.role());
    }
    
    @Test
    @DisplayName("Deve retornar null quando RepresentativeCreateDTO for null")
    void shouldReturnNullWhenRepresentativeCreateDTOIsNull() {
        // When
        RepresentativeCreateCommandDTO result = representativeAdapterMapper.toCreateCommand(null, companyId);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear RepresentativeUpdateDTO para RepresentativeUpdateCommandDTO com sucesso")
    void shouldMapRepresentativeUpdateDTOToRepresentativeUpdateCommandDTOSuccessfully() {
        // Given
        RepresentativeUpdateDTO dto = RepresentativeTestBuilder.builder()
            .withMariaSilva()
            .buildUpdateDTO();
        
        // When
        RepresentativeUpdateCommandDTO result = representativeAdapterMapper.toUpdateCommand(dto);
        
        // Then
        assertNotNull(result);
        assertEquals(dto.getName(), result.name());
        assertEquals(dto.getCpf(), result.cpf());
        assertEquals(dto.getRg(), result.rg());
        assertEquals(dto.getBirthDate(), result.birthDate());
        assertEquals(dto.getEmail(), result.email());
        assertEquals(dto.getPhone(), result.phone());
        assertEquals(dto.getRole(), result.role());
    }
    
    @Test
    @DisplayName("Deve retornar null quando RepresentativeUpdateDTO for null")
    void shouldReturnNullWhenRepresentativeUpdateDTOIsNull() {
        // When
        RepresentativeUpdateCommandDTO result = representativeAdapterMapper.toUpdateCommand(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear RepresentativeViewDTO para RepresentativeResponseDTO com sucesso")
    void shouldMapRepresentativeViewDTOToRepresentativeResponseDTOSuccessfully() {
        // Given
        RepresentativeViewDTO view = RepresentativeTestBuilder.builder()
            .withId(representativeId)
            .buildView();
        
        // When
        RepresentativeResponseDTO result = representativeAdapterMapper.toResponseDTO(view);
        
        // Then
        assertNotNull(result);
        assertEquals(view.id(), result.getId());
        assertEquals(view.name(), result.getName());
        assertEquals(view.cpf(), result.getCpf());
        assertEquals(view.rg(), result.getRg());
        assertEquals(view.birthDate(), result.getBirthDate());
        assertEquals(view.email(), result.getEmail());
        assertEquals(view.phone(), result.getPhone());
        assertEquals(view.role(), result.getRole());
        assertEquals(view.active(), result.getActive());
        assertEquals(view.createdAt(), result.getCreatedAt());
        assertEquals(view.updatedAt(), result.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve retornar null quando RepresentativeViewDTO for null")
    void shouldReturnNullWhenRepresentativeViewDTOIsNull() {
        // When
        RepresentativeResponseDTO result = representativeAdapterMapper.toResponseDTO((RepresentativeViewDTO) null);
        
        // Then
        assertNull(result);
    }
    
    
    @Test
    @DisplayName("Deve mapear RepresentativeViewDTO para RepresentativeDTO com sucesso")
    void shouldMapRepresentativeViewDTOToRepresentativeDTOSuccessfully() {
        // Given
        RepresentativeViewDTO view = RepresentativeTestBuilder.builder()
            .withId(representativeId)
            .buildView();
        
        // When
        RepresentativeDTO result = representativeAdapterMapper.toCompanyRepresentativeDTO(view);
        
        // Then
        assertNotNull(result);
        assertEquals(view.name(), result.getName());
        assertEquals(view.cpf(), result.getCpf());
        assertEquals(view.rg(), result.getRg());
        assertEquals(view.birthDate(), result.getBirthDate());
        assertEquals(view.email(), result.getEmail());
        assertEquals(view.phone(), result.getPhone());
        assertEquals(view.role(), result.getRole());
    }
    
    @Test
    @DisplayName("Deve retornar null quando RepresentativeViewDTO for null no toCompanyRepresentativeDTO")
    void shouldReturnNullWhenRepresentativeViewDTOIsNullInToCompanyRepresentativeDTO() {
        // When
        RepresentativeDTO result = representativeAdapterMapper.toCompanyRepresentativeDTO(null);
        
        // Then
        assertNull(result);
    }
}
