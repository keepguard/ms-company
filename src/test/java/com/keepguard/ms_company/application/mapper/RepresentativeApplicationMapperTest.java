package com.keepguard.ms_company.application.mapper;

import com.keepguard.ms_company.adapters.in.rest.representative.dto.RepresentativeCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.representative.dto.RepresentativeResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.representative.dto.RepresentativeUpdateDTO;
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
    @DisplayName("Deve converter RepresentativeCreateDTO para RepresentativeCreateCommandDTO")
    void shouldConvertRepresentativeCreateDTOToCreateCommand() {
        // Given
        RepresentativeCreateDTO dto = RepresentativeTestBuilder.createDefaultCreateDTO();
        
        // When
        RepresentativeCreateCommandDTO command = representativeMapper.toCreateCommand(dto, companyId);
        
        // Then
        assertNotNull(command);
        assertEquals(dto.getName(), command.name());
        assertEquals(dto.getCpf(), command.cpf());
        assertEquals(dto.getRg(), command.rg());
        assertEquals(dto.getBirthDate(), command.birthDate());
        assertEquals(dto.getEmail(), command.email());
        assertEquals(dto.getPhone(), command.phone());
        assertEquals(dto.getRole(), command.role());
        assertEquals(companyId, command.companyId());
    }
    
    @Test
    @DisplayName("Deve converter RepresentativeUpdateDTO para RepresentativeUpdateCommandDTO")
    void shouldConvertRepresentativeUpdateDTOToUpdateCommand() {
        // Given
        RepresentativeUpdateDTO dto = RepresentativeTestBuilder.createDefaultUpdateDTO();
        
        // When
        RepresentativeUpdateCommandDTO command = representativeMapper.toUpdateCommand(dto);
        
        // Then
        assertNotNull(command);
        assertEquals(dto.getName(), command.name());
        assertEquals(dto.getCpf(), command.cpf());
        assertEquals(dto.getRg(), command.rg());
        assertEquals(dto.getBirthDate(), command.birthDate());
        assertEquals(dto.getEmail(), command.email());
        assertEquals(dto.getPhone(), command.phone());
        assertEquals(dto.getRole(), command.role());
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
    @DisplayName("Deve converter RepresentativeViewDTO para RepresentativeResponseDTO")
    void shouldConvertRepresentativeViewDTOToResponseDTO() {
        // Given
        RepresentativeViewDTO view = RepresentativeTestBuilder.createDefaultRepresentativeViewDTO();
        
        // When
        RepresentativeResponseDTO dto = representativeMapper.toResponseDTO(view);
        
        // Then
        assertNotNull(dto);
        assertEquals(view.id(), dto.getId());
        assertEquals(view.name(), dto.getName());
        assertEquals(view.cpf(), dto.getCpf());
        assertEquals(view.rg(), dto.getRg());
        assertEquals(view.birthDate(), dto.getBirthDate());
        assertEquals(view.email(), dto.getEmail());
        assertEquals(view.phone(), dto.getPhone());
        assertEquals(view.role(), dto.getRole());
        assertEquals(view.active(), dto.getActive());
        assertEquals(view.createdAt(), dto.getCreatedAt());
        assertEquals(view.updatedAt(), dto.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve converter Representative para RepresentativeResponseDTO")
    void shouldConvertRepresentativeToResponseDTO() {
        // Given
        Representative representative = RepresentativeTestBuilder.createDefaultRepresentative();
        
        // When
        RepresentativeResponseDTO dto = representativeMapper.toResponseDTO(representative);
        
        // Then
        assertNotNull(dto);
        assertEquals(representative.getId(), dto.getId());
        assertEquals(representative.getName(), dto.getName());
        assertEquals(representative.getCpf(), dto.getCpf());
        assertEquals(representative.getRg(), dto.getRg());
        assertEquals(representative.getBirthDate(), dto.getBirthDate());
        assertEquals(representative.getEmail(), dto.getEmail());
        assertEquals(representative.getPhone(), dto.getPhone());
        assertEquals(representative.getRole(), dto.getRole());
        assertEquals(representative.isActive(), dto.getActive());
        assertNull(dto.getCreatedAt()); // Não disponível na entidade de domínio
        assertNull(dto.getUpdatedAt()); // Não disponível na entidade de domínio
    }
    
    @Test
    @DisplayName("Deve retornar null quando Representative for null no toView")
    void shouldReturnNullWhenRepresentativeIsNullInToView() {
        // When
        RepresentativeViewDTO view = representativeMapper.toView(null);
        
        // Then
        assertNull(view);
    }
    
    @Test
    @DisplayName("Deve retornar null quando RepresentativeViewDTO for null no toResponseDTO")
    void shouldReturnNullWhenRepresentativeViewDTOIsNullInToResponseDTO() {
        // When
        RepresentativeResponseDTO dto = representativeMapper.toResponseDTO((RepresentativeViewDTO) null);
        
        // Then
        assertNull(dto);
    }
    
    @Test
    @DisplayName("Deve retornar null quando Representative for null no toResponseDTO")
    void shouldReturnNullWhenRepresentativeIsNullInToResponseDTO() {
        // When
        RepresentativeResponseDTO dto = representativeMapper.toResponseDTO((Representative) null);
        
        // Then
        assertNull(dto);
    }
    
    @Test
    @DisplayName("Deve converter DTO com RG nulo")
    void shouldConvertDTOWithNullRg() {
        // Given
        RepresentativeCreateDTO dto = RepresentativeTestBuilder.builder()
            .withNullRg()
            .buildCreateDTO();
        
        // When
        RepresentativeCreateCommandDTO command = representativeMapper.toCreateCommand(dto, companyId);
        
        // Then
        assertNull(command.rg());
        assertEquals("João Silva", command.name());
        assertEquals("11144477735", command.cpf());
    }
    
    @Test
    @DisplayName("Deve converter DTO com cargo nulo")
    void shouldConvertDTOWithNullRole() {
        // Given
        RepresentativeUpdateDTO dto = RepresentativeTestBuilder.builder()
            .withNullRole()
            .buildUpdateDTO();
        
        // When
        RepresentativeUpdateCommandDTO command = representativeMapper.toUpdateCommand(dto);
        
        // Then
        assertNull(command.role());
        assertEquals("João Silva", command.name());
        assertEquals("11144477735", command.cpf());
    }
    
    @Test
    @DisplayName("Deve converter DTO com dados de Maria Silva")
    void shouldConvertDTOWithMariaSilvaData() {
        // Given
        RepresentativeCreateDTO dto = RepresentativeTestBuilder.builder()
            .withMariaSilva()
            .buildCreateDTO();
        
        // When
        RepresentativeCreateCommandDTO command = representativeMapper.toCreateCommand(dto, companyId);
        
        // Then
        assertEquals("Maria Silva", command.name());
        assertEquals("98765432100", command.cpf());
        assertEquals("987654321", command.rg());
        assertEquals(LocalDate.of(1985, 5, 15), command.birthDate());
        assertEquals("maria.silva@empresa.com", command.email());
        assertEquals("11888888888", command.phone());
        assertEquals("Gerente", command.role());
        assertEquals(companyId, command.companyId());
    }
    
    @Test
    @DisplayName("Deve converter DTO com dados de Pedro Santos")
    void shouldConvertDTOWithPedroSantosData() {
        // Given
        RepresentativeUpdateDTO dto = RepresentativeTestBuilder.builder()
            .withPedroSantos()
            .buildUpdateDTO();
        
        // When
        RepresentativeUpdateCommandDTO command = representativeMapper.toUpdateCommand(dto);
        
        // Then
        assertEquals("Pedro Santos", command.name());
        assertEquals("12345678909", command.cpf());
        assertEquals("111222333", command.rg());
        assertEquals(LocalDate.of(1992, 8, 20), command.birthDate());
        assertEquals("pedro.santos@empresa.com", command.email());
        assertEquals("11777777777", command.phone());
        assertEquals("Supervisor", command.role());
    }
    
    @Test
    @DisplayName("Deve converter DTO com dados de Ana Costa")
    void shouldConvertDTOWithAnaCostaData() {
        // Given
        RepresentativeCreateDTO dto = RepresentativeTestBuilder.builder()
            .withAnaCosta()
            .buildCreateDTO();
        
        // When
        RepresentativeCreateCommandDTO command = representativeMapper.toCreateCommand(dto, companyId);
        
        // Then
        assertEquals("Ana Costa", command.name());
        assertEquals("55566677720", command.cpf());
        assertEquals("555666777", command.rg());
        assertEquals(LocalDate.of(1988, 12, 10), command.birthDate());
        assertEquals("ana.costa@empresa.com", command.email());
        assertEquals("11666666666", command.phone());
        assertEquals("Coordenadora", command.role());
        assertEquals(companyId, command.companyId());
    }
    
    @Test
    @DisplayName("Deve converter DTO com dados de Carlos Oliveira")
    void shouldConvertDTOWithCarlosOliveiraData() {
        // Given
        RepresentativeUpdateDTO dto = RepresentativeTestBuilder.builder()
            .withCarlosOliveira()
            .buildUpdateDTO();
        
        // When
        RepresentativeUpdateCommandDTO command = representativeMapper.toUpdateCommand(dto);
        
        // Then
        assertEquals("Carlos Oliveira", command.name());
        assertEquals("99988877714", command.cpf());
        assertEquals("999888777", command.rg());
        assertEquals(LocalDate.of(1980, 3, 25), command.birthDate());
        assertEquals("carlos.oliveira@empresa.com", command.email());
        assertEquals("11555555555", command.phone());
        assertEquals("Presidente", command.role());
    }
    
    @Test
    @DisplayName("Deve converter representante inativo")
    void shouldConvertInactiveRepresentative() {
        // Given
        Representative representative = RepresentativeTestBuilder.createInactiveRepresentative();
        
        // When
        RepresentativeViewDTO view = representativeMapper.toView(representative);
        RepresentativeResponseDTO dto = representativeMapper.toResponseDTO(view);
        
        // Then
        assertFalse(view.active());
        assertFalse(dto.getActive());
        assertEquals("João Silva", view.name());
        assertEquals("João Silva", dto.getName());
    }
    
    @Test
    @DisplayName("Deve converter view com timestamps")
    void shouldConvertViewWithTimestamps() {
        // Given
        RepresentativeViewDTO view = RepresentativeTestBuilder.builder()
            .buildView();
        
        // When
        RepresentativeResponseDTO dto = representativeMapper.toResponseDTO(view);
        
        // Then
        assertEquals(view.createdAt(), dto.getCreatedAt());
        assertEquals(view.updatedAt(), dto.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve converter view sem timestamps")
    void shouldConvertViewWithoutTimestamps() {
        // Given
        RepresentativeViewDTO view = RepresentativeTestBuilder.builder()
            .buildView();
        
        // When
        RepresentativeResponseDTO dto = representativeMapper.toResponseDTO(view);
        
        // Then
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }
}
