package com.keepguard.ms_company.adapters.in.rest.company;

import com.keepguard.ms_company.adapters.in.rest.company.dto.request.CompanyCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.request.CompanyUpdateDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.response.CompanyResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.company.mapper.CompanyAdapterMapper;
import com.keepguard.ms_company.application.dto.company.CompanyCreateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.company.CompanySearchCriteriaDTO;
import com.keepguard.ms_company.application.port.in.CompanyPort;
import com.keepguard.ms_company.test.builder.CompanyTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testes unitários para CompanyController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Company Controller Tests")
class CompanyControllerTest {

    @Mock
    private CompanyPort companyPort;

    @Mock
    private CompanyAdapterMapper companyAdapterMapper;

    @InjectMocks
    private CompanyController companyController;

    private CompanyViewDTO companyView;
    private CompanyResponseDTO companyResponse;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        
        // Criar objetos de teste usando builders
        companyView = CompanyTestBuilder.builder()
            .withId(companyId)
            .buildView();
            
        companyResponse = CompanyTestBuilder.builder()
            .withId(companyId)
            .buildResponseDTO();
    }

    @Test
    @DisplayName("Deve criar empresa com DTO válido")
    void shouldCreateCompanyWithValidDTO() {
        // Given
        CompanyCreateDTO createDTO = CompanyTestBuilder.builder()
            .buildCreateDTO();
            
        CompanyCreateCommandDTO createCommand = CompanyTestBuilder.builder()
            .buildCreateCommand();

        when(companyAdapterMapper.toCreateCommand(any(CompanyCreateDTO.class)))
            .thenReturn(createCommand);
        when(companyPort.create(any(CompanyCreateCommandDTO.class)))
            .thenReturn(companyView);
        when(companyAdapterMapper.toResponseDTO(any(CompanyViewDTO.class)))
            .thenReturn(companyResponse);

        // When
        ResponseEntity<CompanyResponseDTO> response = companyController.create(createDTO);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        CompanyResponseDTO responseBody = response.getBody();
        assertEquals(companyId, responseBody.getId());
        assertEquals(createDTO.getName(), responseBody.getName());
        assertEquals(createDTO.getLegalName(), responseBody.getLegalName());
        assertEquals(createDTO.getCnpj(), responseBody.getCnpj());
        
        verify(companyAdapterMapper, times(1)).toCreateCommand(any(CompanyCreateDTO.class));
        verify(companyPort, times(1)).create(any(CompanyCreateCommandDTO.class));
        verify(companyAdapterMapper, times(1)).toResponseDTO(any(CompanyViewDTO.class));
    }

    @Test
    @DisplayName("Deve lidar com exceções durante criação")
    void shouldHandleExceptionsDuringCreation() {
        // Given
        CompanyCreateDTO createDTO = CompanyTestBuilder.builder()
            .buildCreateDTO();
            
        CompanyCreateCommandDTO createCommand = CompanyTestBuilder.builder()
            .buildCreateCommand();

        when(companyAdapterMapper.toCreateCommand(any(CompanyCreateDTO.class)))
            .thenReturn(createCommand);
        when(companyPort.create(any(CompanyCreateCommandDTO.class)))
            .thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            companyController.create(createDTO);
        });
        
        verify(companyAdapterMapper, times(1)).toCreateCommand(any(CompanyCreateDTO.class));
        verify(companyPort, times(1)).create(any(CompanyCreateCommandDTO.class));
    }

    @Test
    @DisplayName("Deve atualizar empresa com DTO válido")
    void shouldUpdateCompanyWithValidDTO() {
        // Given
        CompanyUpdateDTO updateDTO = CompanyTestBuilder.builder()
            .withName("Empresa Atualizada")
            .buildUpdateDTO();
            
        CompanyUpdateCommandDTO updateCommand = CompanyTestBuilder.builder()
            .withName("Empresa Atualizada")
            .buildUpdateCommand();

        when(companyAdapterMapper.toUpdateCommand(any(CompanyUpdateDTO.class)))
            .thenReturn(updateCommand);
        when(companyPort.update(eq(companyId), any(CompanyUpdateCommandDTO.class)))
            .thenReturn(companyView);
        CompanyResponseDTO updatedResponse = CompanyTestBuilder.builder()
            .withId(companyId)
            .withName("Empresa Atualizada")
            .buildResponseDTO();
        when(companyAdapterMapper.toResponseDTO(any(CompanyViewDTO.class)))
            .thenReturn(updatedResponse);

        // When
        ResponseEntity<CompanyResponseDTO> response = companyController.update(companyId, updateDTO);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        CompanyResponseDTO responseBody = response.getBody();
        assertEquals(companyId, responseBody.getId());
        assertEquals(updateDTO.getName(), responseBody.getName());
        
        verify(companyAdapterMapper, times(1)).toUpdateCommand(any(CompanyUpdateDTO.class));
        verify(companyPort, times(1)).update(eq(companyId), any(CompanyUpdateCommandDTO.class));
        verify(companyAdapterMapper, times(1)).toResponseDTO(any(CompanyViewDTO.class));
    }

    @Test
    @DisplayName("Deve lidar com exceções durante atualização")
    void shouldHandleExceptionsDuringUpdate() {
        // Given
        CompanyUpdateDTO updateDTO = CompanyTestBuilder.builder()
            .withName("Empresa Atualizada")
            .buildUpdateDTO();
            
        CompanyUpdateCommandDTO updateCommand = CompanyTestBuilder.builder()
            .withName("Empresa Atualizada")
            .buildUpdateCommand();

        when(companyAdapterMapper.toUpdateCommand(any(CompanyUpdateDTO.class)))
            .thenReturn(updateCommand);
        when(companyPort.update(eq(companyId), any(CompanyUpdateCommandDTO.class)))
            .thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            companyController.update(companyId, updateDTO);
        });
        
        verify(companyAdapterMapper, times(1)).toUpdateCommand(any(CompanyUpdateDTO.class));
        verify(companyPort, times(1)).update(eq(companyId), any(CompanyUpdateCommandDTO.class));
    }

    @Test
    @DisplayName("Deve buscar empresa por ID com sucesso")
    void shouldFindCompanyById() {
        // Given
        when(companyPort.getById(companyId))
            .thenReturn(companyView);
        when(companyAdapterMapper.toResponseDTO(any(CompanyViewDTO.class)))
            .thenReturn(companyResponse);

        // When
        ResponseEntity<CompanyResponseDTO> response = companyController.getById(companyId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        CompanyResponseDTO responseBody = response.getBody();
        assertEquals(companyId, responseBody.getId());
        assertEquals(companyResponse.getName(), responseBody.getName());
        
        verify(companyPort, times(1)).getById(companyId);
        verify(companyAdapterMapper, times(1)).toResponseDTO(any(CompanyViewDTO.class));
    }

    @Test
    @DisplayName("Deve buscar empresas com critérios válidos")
    void shouldSearchCompaniesWithValidCriteria() {
        // Given
        PageResultDTO<CompanyViewDTO> pageResult = new PageResultDTO<>(
            List.of(companyView), 1L, 0, 10
        );

        when(companyPort.search(any(CompanySearchCriteriaDTO.class)))
            .thenReturn(pageResult);
        when(companyAdapterMapper.toResponseDTO(any(CompanyViewDTO.class)))
            .thenReturn(companyResponse);

        // When
        ResponseEntity<PageResultDTO<CompanyResponseDTO>> response = companyController.search(
            "Empresa Teste", "11222333000181", "ACTIVE", "São Paulo", "SP", "ACTIVE", 0, 10, List.of(), "name"
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        PageResultDTO<CompanyResponseDTO> responseBody = response.getBody();
        assertEquals(1, responseBody.items().size());
        assertEquals(1L, responseBody.total());
        assertEquals(0, responseBody.page());
        assertEquals(10, responseBody.size());
        
        verify(companyPort, times(1)).search(any(CompanySearchCriteriaDTO.class));
        verify(companyAdapterMapper, times(1)).toResponseDTO(any(CompanyViewDTO.class));
    }

    @Test
    @DisplayName("Deve deletar empresa por ID")
    void shouldDeleteCompanyById() {
        // Given
        doNothing().when(companyPort).delete(companyId);

        // When
        ResponseEntity<Void> response = companyController.delete(companyId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(companyPort, times(1)).delete(companyId);
    }

    @Test
    @DisplayName("Deve lidar com exceções durante busca por ID")
    void shouldHandleExceptionsDuringGetById() {
        // Given
        when(companyPort.getById(companyId))
            .thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            companyController.getById(companyId);
        });
        
        verify(companyPort, times(1)).getById(companyId);
    }

    @Test
    @DisplayName("Deve lidar com exceções durante busca")
    void shouldHandleExceptionsDuringSearch() {
        // Given
        when(companyPort.search(any(CompanySearchCriteriaDTO.class)))
            .thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            companyController.search("Empresa Teste", "11222333000181", "ACTIVE", "São Paulo", "SP", "ACTIVE", 0, 10, List.of(), "name");
        });
        
        verify(companyPort, times(1)).search(any(CompanySearchCriteriaDTO.class));
    }

    @Test
    @DisplayName("Deve lidar com exceções durante exclusão")
    void shouldHandleExceptionsDuringDelete() {
        // Given
        doThrow(new RuntimeException("Service error")).when(companyPort).delete(companyId);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            companyController.delete(companyId);
        });
        
        verify(companyPort, times(1)).delete(companyId);
    }
}
