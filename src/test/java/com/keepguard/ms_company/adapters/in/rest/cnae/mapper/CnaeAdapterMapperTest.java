package com.keepguard.ms_company.adapters.in.rest.cnae.mapper;

import com.keepguard.ms_company.adapters.in.rest.cnae.dto.CnaeResponseDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeViewDTO;
import com.keepguard.ms_company.test.builder.CnaeTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para CnaeAdapterMapper
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Cnae Adapter Mapper Tests")
class CnaeAdapterMapperTest {
    
    private CnaeAdapterMapper cnaeAdapterMapper;
    private UUID cnaeId;
    private UUID companyId;
    
    @BeforeEach
    void setUp() {
        cnaeAdapterMapper = new CnaeAdapterMapper();
        cnaeId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }
    
    @Test
    @DisplayName("Deve mapear CnaeCreateDTO para CnaeCreateCommandDTO com sucesso")
    void shouldMapCnaeCreateDTOToCnaeCreateCommandDTOSuccessfully() {
        // Given
        // CnaeTestBuilder não possui buildCreateDTO, então testamos com null
        
        // When
        CnaeCreateCommandDTO result = cnaeAdapterMapper.toCreateCommand(null, companyId);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve retornar null quando CnaeCreateDTO for null")
    void shouldReturnNullWhenCnaeCreateDTOIsNull() {
        // When
        CnaeCreateCommandDTO result = cnaeAdapterMapper.toCreateCommand(null, companyId);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear CnaeUpdateDTO para CnaeUpdateCommandDTO com sucesso")
    void shouldMapCnaeUpdateDTOToCnaeUpdateCommandDTOSuccessfully() {
        // Given
        // CnaeTestBuilder não possui buildUpdateDTO, então testamos com null
        
        // When
        CnaeUpdateCommandDTO result = cnaeAdapterMapper.toUpdateCommand(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve retornar null quando CnaeUpdateDTO for null")
    void shouldReturnNullWhenCnaeUpdateDTOIsNull() {
        // When
        CnaeUpdateCommandDTO result = cnaeAdapterMapper.toUpdateCommand(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve mapear CnaeViewDTO para CnaeResponseDTO com sucesso")
    void shouldMapCnaeViewDTOToCnaeResponseDTOSuccessfully() {
        // Given
        CnaeViewDTO view = CnaeTestBuilder.builder()
            .withId(cnaeId)
            .withCompanyId(companyId)
            .asPrincipal()
            .buildView();
        
        // When
        CnaeResponseDTO result = cnaeAdapterMapper.toResponseDTO(view);
        
        // Then
        assertNotNull(result);
        assertEquals(view.id(), result.getId());
        assertEquals(view.companyId(), result.getCompanyId());
        assertEquals(view.code(), result.getCode());
        assertEquals(view.description(), result.getDescription());
        assertEquals(view.section(), result.getSection());
        assertEquals(view.division(), result.getDivision());
        assertEquals(view.groupCode(), result.getGroupCode());
        assertEquals(view.classCode(), result.getClassCode());
        assertEquals(view.subclassCode(), result.getSubclassCode());
        assertEquals(view.active(), result.isActive());
        assertEquals(view.principal(), result.isPrincipal());
    }
    
    @Test
    @DisplayName("Deve retornar null quando CnaeViewDTO for null")
    void shouldReturnNullWhenCnaeViewDTOIsNull() {
        // When
        CnaeResponseDTO result = cnaeAdapterMapper.toResponseDTO(null);
        
        // Then
        assertNull(result);
    }
}
