package com.keepguard.ms_company.application.mapper;

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
