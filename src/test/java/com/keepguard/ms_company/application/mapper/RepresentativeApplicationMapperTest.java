package com.keepguard.ms_company.application.mapper;

import com.keepguard.ms_company.application.dto.representative.RepresentativeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeViewDTO;
import com.keepguard.ms_company.domain.entity.Representative;
import com.keepguard.ms_company.test.builder.RepresentativeTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para RepresentativeMapper
 * Testa conversões entre DTOs e entidades
 */
@DisplayName("Representative Application Mapper Tests")
class RepresentativeApplicationMapperTest {
    
    private RepresentativeApplicationMapper representativeMapper;
    private UUID companyId;
    
    @BeforeEach
    void setUp() {
        representativeMapper = new RepresentativeApplicationMapper();
        companyId = UUID.randomUUID();
    }
    
    
    
    @Test
    @DisplayName("Deve converter Representative para RepresentativeViewDTO")
    void shouldConvertRepresentativeToView() {
        // Given
        Representative representative = RepresentativeTestBuilder.createDefaultRepresentative();
        
        // When
        RepresentativeViewDTO view = representativeMapper.toView(representative);
        
        // Then
        assertNotNull(view);
        assertEquals(representative.getId(), view.id());
        assertEquals(representative.getName(), view.name());
        assertEquals(representative.getCpf(), view.cpf());
        assertEquals(representative.getRg(), view.rg());
        assertEquals(representative.getBirthDate(), view.birthDate());
        assertEquals(representative.getEmail(), view.email());
        assertEquals(representative.getPhone(), view.phone());
        assertEquals(representative.getRole(), view.role());
        assertEquals(representative.isActive(), view.active());
    }
    
    
    
    @Test
    @DisplayName("Deve retornar null quando Representative for null no toView")
    void shouldReturnNullWhenRepresentativeIsNullInToView() {
        // When
        RepresentativeViewDTO view = representativeMapper.toView(null);
        
        // Then
        assertNull(view);
    }
    
    
    
    
    
    
    
    
    
    
    
}
