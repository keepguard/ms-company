package com.keepguard.ms_company.application.service.representative;

import com.keepguard.ms_company.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_company.application.dto.representative.RepresentativeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeViewDTO;
import com.keepguard.ms_company.application.mapper.RepresentativeApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.CompanyRepositoryPort;
import com.keepguard.ms_company.application.port.out.persistence.RepresentativeRepositoryPort;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.domain.entity.Company;
import com.keepguard.ms_company.domain.entity.Representative;
import com.keepguard.ms_company.domain.enums.TaxRegimeEnum;
import com.keepguard.ms_company.test.builder.RepresentativeTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RepresentativeCommandService
 * Testa operações de escrita (create, update, delete)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Representative Command Service Tests")
class RepresentativeCommandServiceTest {
    
    @Mock
    private RepresentativeRepositoryPort representativeRepository;
    
    @Mock
    private CompanyRepositoryPort companyRepository;
    
    @Mock
    private RepresentativeApplicationMapper representativeMapper;
    
    @Mock
    private MetricsPort metricsPort;
    
    @InjectMocks
    private RepresentativeCommandService representativeCommandService;
    
    private UUID companyId;
    private UUID representativeId;
    private Representative representative;
    private RepresentativeViewDTO representativeView;
    private Company company;
    
    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        representativeId = UUID.randomUUID();
        representative = RepresentativeTestBuilder.createDefaultRepresentative();
        representativeView = RepresentativeTestBuilder.createDefaultRepresentativeViewDTO();
        company = createTestCompany();
    }
    
    @Test
    @DisplayName("Deve criar representante com sucesso")
    void shouldCreateRepresentativeSuccessfully() {
        // Given
        RepresentativeCreateCommandDTO command = RepresentativeTestBuilder.createDefaultCreateCommand();
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(representativeRepository.existsByCompanyIdAndCpf(companyId, command.cpf())).thenReturn(false);
        when(representativeRepository.save(any(Representative.class), eq(companyId))).thenReturn(representative);
        when(representativeMapper.toViewDTO(representative)).thenReturn(representativeView);
        
        // When
        RepresentativeViewDTO result = representativeCommandService.create(companyId, command);
        
        // Then
        assertNotNull(result);
        assertEquals(representativeView.id(), result.id());
        assertEquals(representativeView.name(), result.name());
        
        verify(companyRepository).findById(companyId);
        verify(representativeRepository).existsByCompanyIdAndCpf(companyId, command.cpf());
        verify(representativeRepository).save(any(Representative.class), eq(companyId));
        verify(representativeMapper).toViewDTO(representative);
        verify(metricsPort).incrementCounter(eq("representative.created"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando empresa não encontrada")
    void shouldThrowExceptionWhenCompanyNotFound() {
        // Given
        RepresentativeCreateCommandDTO command = RepresentativeTestBuilder.createDefaultCreateCommand();
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> representativeCommandService.create(companyId, command)
        );
        
        assertEquals("Empresa não encontrada", exception.getMessage());
        
        verify(companyRepository).findById(companyId);
        verify(representativeRepository, never()).save(any(Representative.class), any(UUID.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando CPF já existe para a empresa")
    void shouldThrowExceptionWhenCpfAlreadyExists() {
        // Given
        RepresentativeCreateCommandDTO command = RepresentativeTestBuilder.createDefaultCreateCommand();
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(representativeRepository.existsByCompanyIdAndCpf(companyId, command.cpf())).thenReturn(true);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> representativeCommandService.create(companyId, command)
        );
        
        assertEquals("Representante com este CPF já existe para esta empresa", exception.getMessage());
        
        verify(companyRepository).findById(companyId);
        verify(representativeRepository).existsByCompanyIdAndCpf(companyId, command.cpf());
        verify(representativeRepository, never()).save(any(Representative.class), any(UUID.class));
    }
    
    @Test
    @DisplayName("Deve atualizar representante com sucesso")
    void shouldUpdateRepresentativeSuccessfully() {
        // Given
        RepresentativeUpdateCommandDTO command = RepresentativeTestBuilder.createDefaultUpdateCommand();
        
        when(representativeRepository.findById(representativeId)).thenReturn(Optional.of(representative));
        when(representativeRepository.findCompanyIdByRepresentativeId(representativeId)).thenReturn(Optional.of(companyId));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(representativeRepository.save(any(Representative.class))).thenReturn(representative);
        when(representativeMapper.toViewDTO(representative)).thenReturn(representativeView);
        
        // When
        RepresentativeViewDTO result = representativeCommandService.update(representativeId, command);
        
        // Then
        assertNotNull(result);
        assertEquals(representativeView.id(), result.id());
        assertEquals(representativeView.name(), result.name());
        
        verify(representativeRepository).findById(representativeId);
        verify(representativeRepository).findCompanyIdByRepresentativeId(representativeId);
        verify(companyRepository).findById(companyId);
        verify(representativeRepository).save(any(Representative.class));
        verify(representativeMapper).toViewDTO(representative);
        verify(metricsPort).incrementCounter(eq("representative.updated"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando representante não encontrado na atualização")
    void shouldThrowExceptionWhenRepresentativeNotFoundInUpdate() {
        // Given
        RepresentativeUpdateCommandDTO command = RepresentativeTestBuilder.createDefaultUpdateCommand();
        
        when(representativeRepository.findById(representativeId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> representativeCommandService.update(representativeId, command)
        );
        
        assertEquals("Representante não encontrado", exception.getMessage());
        
        verify(representativeRepository).findById(representativeId);
        verify(representativeRepository, never()).save(any(Representative.class));
    }
    
    @Test
    @DisplayName("Deve ativar representante com sucesso")
    void shouldActivateRepresentativeSuccessfully() {
        // Given
        Representative inactiveRepresentative = RepresentativeTestBuilder.createInactiveRepresentative();
        RepresentativeViewDTO activatedView = RepresentativeTestBuilder.builder()
            .withId(inactiveRepresentative.getId())
            .buildView();
        
        when(representativeRepository.findById(representativeId)).thenReturn(Optional.of(inactiveRepresentative));
        when(representativeRepository.save(any(Representative.class))).thenReturn(inactiveRepresentative);
        when(representativeMapper.toViewDTO(inactiveRepresentative)).thenReturn(activatedView);
        
        // When
        RepresentativeViewDTO result = representativeCommandService.activate(representativeId);
        
        // Then
        assertNotNull(result);
        assertEquals(activatedView.id(), result.id());
        
        verify(representativeRepository).findById(representativeId);
        verify(representativeRepository).save(any(Representative.class));
        verify(representativeMapper).toViewDTO(inactiveRepresentative);
        verify(metricsPort).incrementCounter(eq("representative.activated"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando representante não encontrado na ativação")
    void shouldThrowExceptionWhenRepresentativeNotFoundInActivation() {
        // Given
        when(representativeRepository.findById(representativeId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> representativeCommandService.activate(representativeId)
        );
        
        assertEquals("Representante não encontrado", exception.getMessage());
        
        verify(representativeRepository).findById(representativeId);
        verify(representativeRepository, never()).save(any(Representative.class));
    }
    
    @Test
    @DisplayName("Deve desativar representante com sucesso")
    void shouldDeactivateRepresentativeSuccessfully() {
        // Given
        RepresentativeViewDTO deactivatedView = RepresentativeTestBuilder.builder()
            .withId(representative.getId())
            .inactive()
            .buildView();
        
        when(representativeRepository.findById(representativeId)).thenReturn(Optional.of(representative));
        when(representativeRepository.save(any(Representative.class))).thenReturn(representative);
        when(representativeMapper.toViewDTO(representative)).thenReturn(deactivatedView);
        
        // When
        RepresentativeViewDTO result = representativeCommandService.deactivate(representativeId);
        
        // Then
        assertNotNull(result);
        assertEquals(deactivatedView.id(), result.id());
        
        verify(representativeRepository).findById(representativeId);
        verify(representativeRepository).save(any(Representative.class));
        verify(representativeMapper).toViewDTO(representative);
        verify(metricsPort).incrementCounter(eq("representative.deactivated"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando representante não encontrado na desativação")
    void shouldThrowExceptionWhenRepresentativeNotFoundInDeactivation() {
        // Given
        when(representativeRepository.findById(representativeId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> representativeCommandService.deactivate(representativeId)
        );
        
        assertEquals("Representante não encontrado", exception.getMessage());
        
        verify(representativeRepository).findById(representativeId);
        verify(representativeRepository, never()).save(any(Representative.class));
    }
    
    @Test
    @DisplayName("Deve deletar representante com sucesso")
    void shouldDeleteRepresentativeSuccessfully() {
        // Given
        when(representativeRepository.findById(representativeId)).thenReturn(Optional.of(representative));
        
        // When
        representativeCommandService.delete(representativeId);
        
        // Then
        verify(representativeRepository).findById(representativeId);
        verify(representativeRepository).delete(representative);
        verify(metricsPort).incrementCounter(eq("representative.deleted"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve lançar exceção quando representante não encontrado na deleção")
    void shouldThrowExceptionWhenRepresentativeNotFoundInDeletion() {
        // Given
        when(representativeRepository.findById(representativeId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> representativeCommandService.delete(representativeId)
        );
        
        assertEquals("Representante não encontrado", exception.getMessage());
        
        verify(representativeRepository).findById(representativeId);
        verify(representativeRepository, never()).delete(any(Representative.class));
    }
    
    @Test
    @DisplayName("Deve criar representante com dados de Maria Silva")
    void shouldCreateRepresentativeWithMariaSilvaData() {
        // Given
        RepresentativeCreateCommandDTO command = RepresentativeTestBuilder.builder()
            .withMariaSilva()
            .buildCreateCommand();
        
        Representative mariaRepresentative = RepresentativeTestBuilder.createMariaSilvaRepresentative();
        RepresentativeViewDTO mariaView = RepresentativeTestBuilder.createMariaSilvaRepresentativeViewDTO();
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(representativeRepository.existsByCompanyIdAndCpf(companyId, command.cpf())).thenReturn(false);
        when(representativeRepository.save(any(Representative.class), eq(companyId))).thenReturn(mariaRepresentative);
        when(representativeMapper.toViewDTO(mariaRepresentative)).thenReturn(mariaView);
        
        // When
        RepresentativeViewDTO result = representativeCommandService.create(companyId, command);
        
        // Then
        assertEquals("Maria Silva", result.name());
        assertEquals("98765432100", result.cpf());
        assertEquals("maria.silva@empresa.com", result.email());
        assertEquals("Gerente", result.role());
        
        verify(metricsPort).incrementCounter(eq("representative.created"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve atualizar representante com dados de Pedro Santos")
    void shouldUpdateRepresentativeWithPedroSantosData() {
        // Given
        RepresentativeUpdateCommandDTO command = RepresentativeTestBuilder.builder()
            .withPedroSantos()
            .buildUpdateCommand();
        
        Representative pedroRepresentative = RepresentativeTestBuilder.createPedroSantosRepresentative();
        RepresentativeViewDTO pedroView = RepresentativeTestBuilder.createPedroSantosRepresentativeViewDTO();
        
        when(representativeRepository.findById(representativeId)).thenReturn(Optional.of(representative));
        when(representativeRepository.findCompanyIdByRepresentativeId(representativeId)).thenReturn(Optional.of(companyId));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(representativeRepository.save(any(Representative.class))).thenReturn(pedroRepresentative);
        when(representativeMapper.toViewDTO(pedroRepresentative)).thenReturn(pedroView);
        
        // When
        RepresentativeViewDTO result = representativeCommandService.update(representativeId, command);
        
        // Then
        assertEquals("Pedro Santos", result.name());
        assertEquals("12345678909", result.cpf());
        assertEquals("pedro.santos@empresa.com", result.email());
        assertEquals("Supervisor", result.role());
        
        verify(metricsPort).incrementCounter(eq("representative.updated"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve criar representante com RG nulo")
    void shouldCreateRepresentativeWithNullRg() {
        // Given
        RepresentativeCreateCommandDTO command = RepresentativeTestBuilder.builder()
            .withNullRg()
            .buildCreateCommand();
        
        Representative representativeWithNullRg = RepresentativeTestBuilder.createRepresentativeWithNullRg();
        RepresentativeViewDTO viewWithNullRg = RepresentativeTestBuilder.createRepresentativeViewDTOWithNullRg();
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(representativeRepository.existsByCompanyIdAndCpf(companyId, command.cpf())).thenReturn(false);
        when(representativeRepository.save(any(Representative.class), eq(companyId))).thenReturn(representativeWithNullRg);
        when(representativeMapper.toViewDTO(representativeWithNullRg)).thenReturn(viewWithNullRg);
        
        // When
        RepresentativeViewDTO result = representativeCommandService.create(companyId, command);
        
        // Then
        assertNull(result.rg());
        assertEquals("João Silva", result.name());
        assertEquals("11144477735", result.cpf());
        
        verify(metricsPort).incrementCounter(eq("representative.created"), any(Map.class));
    }
    
    @Test
    @DisplayName("Deve criar representante com cargo nulo")
    void shouldCreateRepresentativeWithNullRole() {
        // Given
        RepresentativeCreateCommandDTO command = RepresentativeTestBuilder.builder()
            .withNullRole()
            .buildCreateCommand();
        
        Representative representativeWithNullRole = RepresentativeTestBuilder.createRepresentativeWithNullRole();
        RepresentativeViewDTO viewWithNullRole = RepresentativeTestBuilder.createRepresentativeViewDTOWithNullRole();
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(representativeRepository.existsByCompanyIdAndCpf(companyId, command.cpf())).thenReturn(false);
        when(representativeRepository.save(any(Representative.class), eq(companyId))).thenReturn(representativeWithNullRole);
        when(representativeMapper.toViewDTO(representativeWithNullRole)).thenReturn(viewWithNullRole);
        
        // When
        RepresentativeViewDTO result = representativeCommandService.create(companyId, command);
        
        // Then
        assertNull(result.role());
        assertEquals("João Silva", result.name());
        assertEquals("11144477735", result.cpf());
        
        verify(metricsPort).incrementCounter(eq("representative.created"), any(Map.class));
    }
    
    private Company createTestCompany() {
        return Company.create(
            "Empresa Teste",
            "Empresa Teste Ltda",
            "11222333000181",
            "123456789",
            "987654321",
            TaxRegimeEnum.SIMPLES_NACIONAL,
            "123456789"
        );
    }
}
