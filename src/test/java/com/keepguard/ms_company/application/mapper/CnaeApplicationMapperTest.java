package com.keepguard.ms_company.application.mapper;

import com.keepguard.ms_company.adapters.in.rest.cnae.dto.CnaeCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.cnae.dto.CnaeResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.cnae.dto.CnaeUpdateDTO;
import com.keepguard.ms_company.application.mapper.CnaeApplicationMapper;
import com.keepguard.ms_company.application.dto.cnae.CnaeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeViewDTO;
import com.keepguard.ms_company.domain.entity.Cnae;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para CnaeMapper
 */
@DisplayName("Cnae Application Mapper Tests")
class CnaeApplicationMapperTest {
    
    private CnaeApplicationMapper cnaeMapper;
    private UUID cnaeId;
    private UUID companyId;
    private LocalDateTime now;
    
    @BeforeEach
    void setUp() {
        cnaeMapper = new CnaeApplicationMapper();
        cnaeId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        now = LocalDateTime.now();
    }
    
    @Test
    @DisplayName("Deve converter CnaeCreateDTO para CnaeCreateCommandDTO com companyId")
    void shouldConvertCreateDTOToCreateCommandWithCompanyId() {
        // Given
        CnaeCreateDTO dto = createTestCreateDTO();
        
        // When
        CnaeCreateCommandDTO command = cnaeMapper.toCreateCommand(dto, companyId);
        
        // Then
        assertNotNull(command);
        assertEquals(dto.getCode(), command.code());
        assertEquals(dto.getDescription(), command.description());
        assertEquals(dto.getSection(), command.section());
        assertEquals(dto.getDivision(), command.division());
        assertEquals(dto.getGroupCode(), command.groupCode());
        assertEquals(dto.getClassCode(), command.classCode());
        assertEquals(dto.getSubclassCode(), command.subclassCode());
        assertEquals(dto.isPrincipal(), command.principal());
        assertEquals(companyId, command.companyId());
    }
    
    @Test
    @DisplayName("Deve converter CnaeUpdateDTO para CnaeUpdateCommandDTO")
    void shouldConvertUpdateDTOToUpdateCommand() {
        // Given
        CnaeUpdateDTO dto = createTestUpdateDTO();
        
        // When
        CnaeUpdateCommandDTO command = cnaeMapper.toUpdateCommand(dto);
        
        // Then
        assertNotNull(command);
        assertEquals(dto.getCode(), command.code());
        assertEquals(dto.getDescription(), command.description());
        assertEquals(dto.getSection(), command.section());
        assertEquals(dto.getDivision(), command.division());
        assertEquals(dto.getGroupCode(), command.groupCode());
        assertEquals(dto.getClassCode(), command.classCode());
        assertEquals(dto.getSubclassCode(), command.subclassCode());
    }
    
    @Test
    @DisplayName("Deve converter Cnae para CnaeViewDTO")
    void shouldConvertCnaeToView() {
        // Given
        Cnae cnae = createTestCnae();
        
        // When
        CnaeViewDTO view = cnaeMapper.toView(cnae);
        
        // Then
        assertNotNull(view);
        assertEquals(cnae.getId(), view.id());
        assertEquals(cnae.getCompanyId(), view.companyId());
        assertEquals(cnae.getCode(), view.code());
        assertEquals(cnae.getDescription(), view.description());
        assertEquals(cnae.getSection(), view.section());
        assertEquals(cnae.getDivision(), view.division());
        assertEquals(cnae.getGroupCode(), view.groupCode());
        assertEquals(cnae.getClassCode(), view.classCode());
        assertEquals(cnae.getSubclassCode(), view.subclassCode());
        assertEquals(cnae.isActive(), view.active());
        assertEquals(cnae.isPrincipal(), view.principal());
        assertEquals(cnae.getCreatedAt(), view.createdAt());
        assertEquals(cnae.getUpdatedAt(), view.updatedAt());
    }
    
    @Test
    @DisplayName("Deve converter CnaeViewDTO para CnaeResponseDTO")
    void shouldConvertViewToResponseDTO() {
        // Given
        CnaeViewDTO view = createTestView();
        
        // When
        CnaeResponseDTO responseDTO = cnaeMapper.toResponseDTO(view);
        
        // Then
        assertNotNull(responseDTO);
        assertEquals(view.id(), responseDTO.getId());
        assertEquals(view.companyId(), responseDTO.getCompanyId());
        assertEquals(view.code(), responseDTO.getCode());
        assertEquals(view.description(), responseDTO.getDescription());
        assertEquals(view.section(), responseDTO.getSection());
        assertEquals(view.division(), responseDTO.getDivision());
        assertEquals(view.groupCode(), responseDTO.getGroupCode());
        assertEquals(view.classCode(), responseDTO.getClassCode());
        assertEquals(view.subclassCode(), responseDTO.getSubclassCode());
        assertEquals(view.active(), responseDTO.isActive());
        assertEquals(view.principal(), responseDTO.isPrincipal());
        assertEquals(view.createdAt(), responseDTO.getCreatedAt());
        assertEquals(view.updatedAt(), responseDTO.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve converter Cnae para CnaeResponseDTO")
    void shouldConvertCnaeToResponseDTO() {
        // Given
        Cnae cnae = createTestCnae();
        
        // When
        CnaeResponseDTO responseDTO = cnaeMapper.toResponseDTO(cnae);
        
        // Then
        assertNotNull(responseDTO);
        assertEquals(cnae.getId(), responseDTO.getId());
        assertEquals(cnae.getCompanyId(), responseDTO.getCompanyId());
        assertEquals(cnae.getCode(), responseDTO.getCode());
        assertEquals(cnae.getDescription(), responseDTO.getDescription());
        assertEquals(cnae.getSection(), responseDTO.getSection());
        assertEquals(cnae.getDivision(), responseDTO.getDivision());
        assertEquals(cnae.getGroupCode(), responseDTO.getGroupCode());
        assertEquals(cnae.getClassCode(), responseDTO.getClassCode());
        assertEquals(cnae.getSubclassCode(), responseDTO.getSubclassCode());
        assertEquals(cnae.isActive(), responseDTO.isActive());
        assertEquals(cnae.isPrincipal(), responseDTO.isPrincipal());
        assertEquals(cnae.getCreatedAt(), responseDTO.getCreatedAt());
        assertEquals(cnae.getUpdatedAt(), responseDTO.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve converter CnaeCreateDTO com campos nulos")
    void shouldConvertCreateDTOWithNullFields() {
        // Given
        CnaeCreateDTO dto = createTestCreateDTOWithNulls();
        
        // When
        CnaeCreateCommandDTO command = cnaeMapper.toCreateCommand(dto, companyId);
        
        // Then
        assertNotNull(command);
        assertEquals(dto.getCode(), command.code());
        assertEquals(dto.getDescription(), command.description());
        assertNull(command.section());
        assertNull(command.division());
        assertNull(command.groupCode());
        assertNull(command.classCode());
        assertNull(command.subclassCode());
        assertEquals(dto.isPrincipal(), command.principal());
        assertEquals(companyId, command.companyId());
    }
    
    @Test
    @DisplayName("Deve converter CnaeUpdateDTO com campos nulos")
    void shouldConvertUpdateDTOWithNullFields() {
        // Given
        CnaeUpdateDTO dto = createTestUpdateDTOWithNulls();
        
        // When
        CnaeUpdateCommandDTO command = cnaeMapper.toUpdateCommand(dto);
        
        // Then
        assertNotNull(command);
        assertEquals(dto.getCode(), command.code());
        assertEquals(dto.getDescription(), command.description());
        assertNull(command.section());
        assertNull(command.division());
        assertNull(command.groupCode());
        assertNull(command.classCode());
        assertNull(command.subclassCode());
    }
    
    @Test
    @DisplayName("Deve converter Cnae inativo")
    void shouldConvertInactiveCnae() {
        // Given
        Cnae cnae = createTestCnae();
        cnae.deactivate();
        
        // When
        CnaeViewDTO view = cnaeMapper.toView(cnae);
        
        // Then
        assertNotNull(view);
        assertFalse(view.active());
    }
    
    @Test
    @DisplayName("Deve converter Cnae principal")
    void shouldConvertPrincipalCnae() {
        // Given
        Cnae cnae = createTestCnae();
        cnae.setAsPrincipal();
        
        // When
        CnaeViewDTO view = cnaeMapper.toView(cnae);
        
        // Then
        assertNotNull(view);
        assertTrue(view.principal());
    }
    
    // Métodos auxiliares para criar objetos de teste
    
    private CnaeCreateDTO createTestCreateDTO() {
        CnaeCreateDTO dto = new CnaeCreateDTO();
        dto.setCode("1234567");
        dto.setDescription("Atividade de desenvolvimento de software");
        dto.setSection("J");
        dto.setDivision("62");
        dto.setGroupCode("620");
        dto.setClassCode("6201");
        dto.setSubclassCode("62015");
        dto.setPrincipal(true);
        return dto;
    }
    
    private CnaeCreateDTO createTestCreateDTOWithNulls() {
        CnaeCreateDTO dto = new CnaeCreateDTO();
        dto.setCode("1234567");
        dto.setDescription("Atividade de desenvolvimento de software");
        dto.setSection(null);
        dto.setDivision(null);
        dto.setGroupCode(null);
        dto.setClassCode(null);
        dto.setSubclassCode(null);
        dto.setPrincipal(false);
        return dto;
    }
    
    private CnaeUpdateDTO createTestUpdateDTO() {
        CnaeUpdateDTO dto = new CnaeUpdateDTO();
        dto.setCode("1234567");
        dto.setDescription("Atividade de desenvolvimento de software");
        dto.setSection("J");
        dto.setDivision("62");
        dto.setGroupCode("620");
        dto.setClassCode("6201");
        dto.setSubclassCode("62015");
        return dto;
    }
    
    private CnaeUpdateDTO createTestUpdateDTOWithNulls() {
        CnaeUpdateDTO dto = new CnaeUpdateDTO();
        dto.setCode("1234567");
        dto.setDescription("Atividade de desenvolvimento de software");
        dto.setSection(null);
        dto.setDivision(null);
        dto.setGroupCode(null);
        dto.setClassCode(null);
        dto.setSubclassCode(null);
        return dto;
    }
    
    private Cnae createTestCnae() {
        return Cnae.of(
            cnaeId,
            "1234567",
            "Atividade de desenvolvimento de software",
            "J",
            "62",
            "620",
            "6201",
            "62015",
            true,
            false,
            companyId,
            now,
            now
        );
    }
    
    private CnaeViewDTO createTestView() {
        return new CnaeViewDTO(
            cnaeId,
            companyId,
            "1234567",
            "Atividade de desenvolvimento de software",
            "J",
            "62",
            "620",
            "6201",
            "62015",
            true,
            false,
            now,
            now
        );
    }
}
