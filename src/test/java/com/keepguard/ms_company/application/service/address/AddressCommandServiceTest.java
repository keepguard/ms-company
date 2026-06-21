package com.keepguard.ms_company.application.service.address;

import com.keepguard.ms_company.application.dto.address.AddressCreateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressViewDTO;
import com.keepguard.ms_company.application.mapper.AddressApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.AddressRepositoryPort;
import com.keepguard.ms_company.application.port.out.persistence.CompanyRepositoryPort;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.application.service.exception.InvalidStatusForOperationException;
import com.keepguard.ms_company.domain.entity.Address;
import com.keepguard.ms_company.domain.entity.Company;
import com.keepguard.ms_company.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_company.test.builder.AddressTestBuilder;
import com.keepguard.ms_company.test.builder.BankAccountTestBuilder;
import com.keepguard.ms_company.test.builder.CnaeTestBuilder;
import com.keepguard.ms_company.test.builder.ContactTestBuilder;
import com.keepguard.ms_company.test.builder.CompanyTestBuilder;
import com.keepguard.ms_company.test.builder.RepresentativeTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Testes unitários para AddressCommandService
 * Inclui verificações de métricas usando o serviço genérico MetricsService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Address Command Service Tests")
class AddressCommandServiceTest {
    
    @Mock
    private AddressRepositoryPort addressRepository;
    
    @Mock
    private CompanyRepositoryPort companyRepository;
    
    @Mock
    private AddressApplicationMapper addressMapper;
    
    @Mock
    private MetricsPort metricsPort;
    
    @InjectMocks
    private AddressCommandService addressCommandService;
    
    private Address address;
    private AddressViewDTO addressView;
    private Company company;
    private UUID addressId;
    private UUID companyId;
    
    @BeforeEach
    void setUp() {
        addressId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        
        // Criar endereço de teste usando builder
        address = AddressTestBuilder.builder()
            .withId(addressId)
            .withCompanyId(companyId)
            .buildDomain();
        
        // Criar empresa de teste usando builder
        company = CompanyTestBuilder.builder()
            .withId(companyId)
            .buildDomain();
        
        // Criar view de teste usando builder
        addressView = AddressTestBuilder.builder()
            .withId(addressId)
            .withCompanyId(companyId)
            .buildView();
        
        // Configurar mocks comuns com lenient para evitar problemas de stubbing
        lenient().when(addressRepository.save(any(Address.class), any(UUID.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(addressMapper.toViewDTO(any(Address.class), any(UUID.class))).thenReturn(addressView);
        lenient().when(companyRepository.save(any(Company.class))).thenReturn(company);
    }
    
    /**
     * Configura uma empresa com todos os dados necessários para aprovação
     */
    private void setupCompanyWithRequiredData() {
        // Adiciona endereço ativo
        company.addAddress(address);
        
        // Adiciona conta bancária ativa
        var bankAccount = BankAccountTestBuilder.builder().buildDomain();
        company.addBankAccount(bankAccount);
        
        // Adiciona CNAE ativo e principal
        var cnae = CnaeTestBuilder.builder().asPrincipal().buildDomain();
        company.addCnae(cnae);
        
        // Adiciona contato ativo
        var contact = ContactTestBuilder.builder().build();
        company.addContact(contact);
        
        // Adiciona representante ativo
        var representative = RepresentativeTestBuilder.builder().buildDomain();
        company.addRepresentative(representative);
    }
    
    @Test
    @DisplayName("Deve criar endereço com sucesso e registrar métricas")
    void shouldCreateAddressSuccessfully() {
        // Given
        AddressCreateCommandDTO command = AddressTestBuilder.builder()
            .withCompanyId(companyId)
            .buildCreateCommand();
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(addressRepository.findActiveByCompanyId(companyId)).thenReturn(Optional.empty());
        when(addressMapper.toDomain(command)).thenReturn(address);
        
        // When
        AddressViewDTO result = addressCommandService.create(companyId, command);
        
        // Then
        assertNotNull(result);
        assertEquals(addressId, result.id());
        assertEquals(companyId, result.companyId());
        assertEquals("Rua das Flores", result.street());
        
        verify(companyRepository).findById(companyId);
        verify(addressRepository).findActiveByCompanyId(companyId);
        verify(addressMapper).toDomain(command);
        verify(addressRepository).save(any(Address.class), any(UUID.class));
        verify(companyRepository).save(company);
        verify(addressMapper).toViewDTO(address, companyId);
        verify(metricsPort).incrementCounter(eq("address_created_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção e registrar métrica de erro ao criar endereço para empresa inexistente")
    void shouldThrowExceptionAndRecordMetricWhenCreatingAddressForNonExistentCompany() {
        // Given
        AddressCreateCommandDTO command = AddressTestBuilder.builder()
            .withCompanyId(companyId)
            .buildCreateCommand();
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            addressCommandService.create(companyId, command);
        });
        
        assertEquals("Empresa não encontrada: " + companyId, exception.getMessage());
        
        verify(companyRepository).findById(companyId);
        verify(addressMapper, never()).toDomain(any());
        verify(addressRepository, never()).save(any());
        verify(metricsPort).incrementCounter(eq("address_business_errors_total"), any());
    }
    
    @Test
    @DisplayName("Deve desativar endereço ativo existente ao criar novo endereço")
    void shouldDeactivateExistingActiveAddressWhenCreatingNewAddress() {
        // Given
        AddressCreateCommandDTO command = AddressTestBuilder.builder()
            .withCompanyId(companyId)
            .buildCreateCommand();
        
        Address existingActiveAddress = AddressTestBuilder.builder()
            .withStreet("Rua Antiga")
            .withNumber("456")
            .withNullComplement()
            .buildDomain();
        
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(addressRepository.findActiveByCompanyId(companyId)).thenReturn(Optional.of(existingActiveAddress));
        when(addressMapper.toDomain(command)).thenReturn(address);
        
        // When
        AddressViewDTO result = addressCommandService.create(companyId, command);
        
        // Then
        assertNotNull(result);
        assertFalse(existingActiveAddress.isActive()); // Endereço anterior foi desativado
        
        verify(addressRepository).save(existingActiveAddress); // Endereço anterior foi salvo desativado
        verify(addressRepository).save(any(Address.class), any(UUID.class)); // Novo endereço foi salvo
        verify(metricsPort).incrementCounter(eq("address_created_total"), any());
    }
    
    @Test
    @DisplayName("Deve atualizar endereço com sucesso e registrar métricas")
    void shouldUpdateAddressSuccessfully() {
        // Given
        AddressUpdateCommandDTO command = AddressTestBuilder.builder()
            .withStreet("Rua Atualizada")
            .withNumber("456")
            .withComplement("Sala 2")
            .buildUpdateCommand();
        
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(addressRepository.findCompanyIdByAddressId(addressId)).thenReturn(Optional.of(companyId));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(addressMapper.toDomain(command, address)).thenReturn(address);
        when(addressRepository.save(any(Address.class), any(UUID.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(addressMapper.toViewDTO(any(Address.class), any(UUID.class))).thenReturn(addressView);
        
        // When
        AddressViewDTO result = addressCommandService.update(addressId, command);
        
        // Then
        assertNotNull(result);
        assertEquals(addressId, result.id());
        
        verify(addressRepository).findById(addressId);
        verify(addressRepository).findCompanyIdByAddressId(any(UUID.class));
        verify(addressMapper).toDomain(command, address);
        verify(addressRepository).save(any(Address.class), any(UUID.class));
        verify(addressMapper).toViewDTO(address, companyId);
        verify(metricsPort).incrementCounter(eq("address_updated_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção e registrar métrica ao atualizar endereço inexistente")
    void shouldThrowExceptionAndRecordMetricWhenUpdatingNonExistentAddress() {
        // Given
        AddressUpdateCommandDTO command = AddressTestBuilder.builder()
            .withStreet("Rua Atualizada")
            .withNumber("456")
            .withComplement("Sala 2")
            .buildUpdateCommand();
        
        when(addressRepository.findById(addressId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            addressCommandService.update(addressId, command);
        });
        
        assertEquals("Endereço não encontrado: " + addressId, exception.getMessage());
        
        verify(addressRepository).findById(addressId);
        verify(addressMapper, never()).toDomain(any(), any());
        verify(addressRepository, never()).save(any());
        verify(metricsPort).incrementCounter(eq("address_not_found_total"), any());
    }
    
    @Test
    @DisplayName("Deve ativar endereço com sucesso e registrar métricas")
    void shouldActivateAddressSuccessfully() {
        // Given
        address.deactivate(); // Primeiro desativa
        
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(addressRepository.findCompanyIdByAddressId(any(UUID.class))).thenReturn(Optional.of(companyId));
        when(addressRepository.findActiveByCompanyId(companyId)).thenReturn(Optional.empty());
        when(addressRepository.save(any(Address.class), any(UUID.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(addressMapper.toViewDTO(any(Address.class), any(UUID.class))).thenReturn(addressView);
        
        // When
        AddressViewDTO result = addressCommandService.activate(addressId);
        
        // Then
        assertNotNull(result);
        assertTrue(address.isActive());
        
        verify(addressRepository).findById(addressId);
        verify(addressRepository).findCompanyIdByAddressId(any(UUID.class));
        verify(addressRepository).save(any(Address.class), any(UUID.class));
        verify(addressMapper).toViewDTO(address, companyId);
        verify(metricsPort).incrementCounter(eq("address_activated_total"), any());
    }
    
    @Test
    @DisplayName("Deve desativar outros endereços ativos ao ativar um endereço")
    void shouldDeactivateOtherActiveAddressesWhenActivatingAddress() {
        // Given
        address.deactivate(); // Primeiro desativa
        
        Address otherActiveAddress = AddressTestBuilder.builder()
            .withStreet("Rua Outra")
            .withNumber("789")
            .withNullComplement()
            .buildDomain();
        
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(addressRepository.findCompanyIdByAddressId(any(UUID.class))).thenReturn(Optional.of(companyId));
        when(addressRepository.findActiveByCompanyId(companyId)).thenReturn(Optional.of(otherActiveAddress));
        
        // When
        AddressViewDTO result = addressCommandService.activate(addressId);
        
        // Then
        assertNotNull(result);
        assertTrue(address.isActive());
        assertFalse(otherActiveAddress.isActive()); // Outro endereço foi desativado
        
        verify(addressRepository).findCompanyIdByAddressId(any(UUID.class));
        verify(addressRepository).save(otherActiveAddress); // Outro endereço foi salvo desativado
        verify(addressRepository).save(any(Address.class), any(UUID.class)); // Endereço atual foi salvo ativado
        verify(metricsPort).incrementCounter(eq("address_activated_total"), any());
    }
    
    @Test
    @DisplayName("Deve desativar endereço com sucesso e registrar métricas")
    void shouldDeactivateAddressSuccessfully() {
        // Given
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(addressRepository.findCompanyIdByAddressId(any(UUID.class))).thenReturn(Optional.of(companyId));
        when(addressRepository.save(any(Address.class), any(UUID.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(addressMapper.toViewDTO(any(Address.class), any(UUID.class))).thenReturn(addressView);
        
        // When
        AddressViewDTO result = addressCommandService.deactivate(addressId);
        
        // Then
        assertNotNull(result);
        assertFalse(address.isActive());
        
        verify(addressRepository).findById(addressId);
        verify(addressRepository).findCompanyIdByAddressId(any(UUID.class));
        verify(addressRepository).save(any(Address.class), any(UUID.class));
        verify(addressMapper).toViewDTO(address, companyId);
        verify(metricsPort).incrementCounter(eq("address_deactivated_total"), any());
    }
    
    @Test
    @DisplayName("Deve deletar endereço com sucesso e registrar métricas")
    void shouldDeleteAddressSuccessfully() {
        // Given
        when(addressRepository.existsById(addressId)).thenReturn(true);
        
        // When
        addressCommandService.delete(addressId);
        
        // Then
        verify(addressRepository).existsById(addressId);
        verify(addressRepository).deleteById(addressId);
        verify(metricsPort).incrementCounter(eq("address_deleted_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção e registrar métrica ao deletar endereço inexistente")
    void shouldThrowExceptionAndRecordMetricWhenDeletingNonExistentAddress() {
        // Given
        when(addressRepository.existsById(addressId)).thenReturn(false);
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            addressCommandService.delete(addressId);
        });
        
        assertEquals("Endereço não encontrado: " + addressId, exception.getMessage());
        
        verify(addressRepository).existsById(addressId);
        verify(addressRepository, never()).deleteById(any());
        verify(metricsPort).incrementCounter(eq("address_not_found_total"), any());
    }
    
    // ==================== TESTES PARA VALIDAÇÃO DE STATUS EM OPERAÇÕES ====================
    
    @Test
    @DisplayName("Deve lançar exceção ao tentar criar endereço para empresa com status BLOCKED")
    void shouldThrowExceptionWhenCreatingAddressForBlockedCompany() {
        // Given - empresa bloqueada
        company.block();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        
        AddressCreateCommandDTO createCommand = AddressTestBuilder.builder()
            .withCompanyId(companyId)
            .buildCreateCommand();
        
        // When & Then
        InvalidStatusForOperationException exception = assertThrows(InvalidStatusForOperationException.class, 
            () -> addressCommandService.create(companyId, createCommand));
        
        assertTrue(exception.getMessage().contains("Não é possível realizar operações na empresa com status 'Bloqueada'"));
        
        verify(companyRepository).findById(companyId);
        verify(addressRepository, never()).findActiveByCompanyId(any());
        verify(addressRepository, never()).save(any(), any());
        verify(addressMapper, never()).toDomain(any());
        verify(addressMapper, never()).toViewDTO(any(), any());
        verify(metricsPort, never()).incrementCounter(eq("address_created_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao tentar criar endereço para empresa com status SUSPENDED")
    void shouldThrowExceptionWhenCreatingAddressForSuspendedCompany() {
        // Given - empresa suspensa (primeiro aprova para poder suspender)
        setupCompanyWithRequiredData();
        company.approve();
        company.suspend();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        
        AddressCreateCommandDTO createCommand = AddressTestBuilder.builder()
            .withCompanyId(companyId)
            .buildCreateCommand();
        
        // When & Then
        InvalidStatusForOperationException exception = assertThrows(InvalidStatusForOperationException.class, 
            () -> addressCommandService.create(companyId, createCommand));
        
        assertTrue(exception.getMessage().contains("Não é possível realizar operações na empresa com status 'Suspensa'"));
        
        verify(companyRepository).findById(companyId);
        verify(addressRepository, never()).findActiveByCompanyId(any());
        verify(addressRepository, never()).save(any(), any());
        verify(addressMapper, never()).toDomain(any());
        verify(addressMapper, never()).toViewDTO(any(), any());
        verify(metricsPort, never()).incrementCounter(eq("address_created_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar endereço de empresa com status BLOCKED")
    void shouldThrowExceptionWhenUpdatingAddressForBlockedCompany() {
        // Given - endereço existente e empresa bloqueada
        Address existingAddress = AddressTestBuilder.builder().buildDomain();
        company.block();
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(existingAddress));
        when(addressRepository.findCompanyIdByAddressId(addressId)).thenReturn(Optional.of(companyId));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        
        AddressUpdateCommandDTO updateCommand = AddressTestBuilder.builder().buildUpdateCommand();
        
        // When & Then
        InvalidStatusForOperationException exception = assertThrows(InvalidStatusForOperationException.class, 
            () -> addressCommandService.update(addressId, updateCommand));
        
        assertTrue(exception.getMessage().contains("Não é possível realizar operações na empresa com status 'Bloqueada'"));
        
        verify(addressRepository).findById(addressId);
        verify(addressRepository).findCompanyIdByAddressId(addressId);
        verify(companyRepository).findById(companyId);
        verify(addressRepository, never()).save(any(), any());
        verify(addressMapper, never()).toDomain(any(), any());
        verify(addressMapper, never()).toViewDTO(any(), any());
        verify(metricsPort, never()).incrementCounter(eq("address_updated_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar endereço de empresa com status SUSPENDED")
    void shouldThrowExceptionWhenUpdatingAddressForSuspendedCompany() {
        // Given - endereço existente e empresa suspensa
        Address existingAddress = AddressTestBuilder.builder().buildDomain();
        setupCompanyWithRequiredData();
        company.approve();
        company.suspend();
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(existingAddress));
        when(addressRepository.findCompanyIdByAddressId(addressId)).thenReturn(Optional.of(companyId));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        
        AddressUpdateCommandDTO updateCommand = AddressTestBuilder.builder().buildUpdateCommand();
        
        // When & Then
        InvalidStatusForOperationException exception = assertThrows(InvalidStatusForOperationException.class, 
            () -> addressCommandService.update(addressId, updateCommand));
        
        assertTrue(exception.getMessage().contains("Não é possível realizar operações na empresa com status 'Suspensa'"));
        
        verify(addressRepository).findById(addressId);
        verify(addressRepository).findCompanyIdByAddressId(addressId);
        verify(companyRepository).findById(companyId);
        verify(addressRepository, never()).save(any(), any());
        verify(addressMapper, never()).toDomain(any(), any());
        verify(addressMapper, never()).toViewDTO(any(), any());
        verify(metricsPort, never()).incrementCounter(eq("address_updated_total"), any());
    }
    
    @Test
    @DisplayName("Deve permitir criar endereço quando empresa está ACTIVE")
    void shouldAllowCreatingAddressWhenCompanyIsActive() {
        // Given - empresa ativa
        setupCompanyWithRequiredData();
        company.approve();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(addressRepository.findActiveByCompanyId(companyId)).thenReturn(Optional.empty());
        when(addressRepository.save(any(), eq(companyId))).thenReturn(address);
        when(addressMapper.toDomain(any())).thenReturn(address);
        when(addressMapper.toViewDTO(address, companyId)).thenReturn(addressView);
        
        AddressCreateCommandDTO createCommand = AddressTestBuilder.builder()
            .withCompanyId(companyId)
            .buildCreateCommand();
        
        // When
        AddressViewDTO result = addressCommandService.create(companyId, createCommand);
        
        // Then
        assertNotNull(result);
        verify(companyRepository).findById(companyId);
        verify(addressRepository).findActiveByCompanyId(companyId);
        verify(addressRepository).save(any(), eq(companyId));
        verify(addressMapper).toDomain(createCommand);
        verify(addressMapper).toViewDTO(address, companyId);
        verify(metricsPort).incrementCounter(eq("address_created_total"), any());
    }
    
    @Test
    @DisplayName("Deve permitir atualizar endereço quando empresa está ACTIVE")
    void shouldAllowUpdatingAddressWhenCompanyIsActive() {
        // Given - endereço existente e empresa ativa
        Address existingAddress = AddressTestBuilder.builder().buildDomain();
        setupCompanyWithRequiredData();
        company.approve();
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(existingAddress));
        when(addressRepository.findCompanyIdByAddressId(addressId)).thenReturn(Optional.of(companyId));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(addressRepository.save(any(), eq(companyId))).thenReturn(address);
        when(addressMapper.toDomain(any(), any())).thenReturn(address);
        when(addressMapper.toViewDTO(address, companyId)).thenReturn(addressView);
        
        AddressUpdateCommandDTO updateCommand = AddressTestBuilder.builder().buildUpdateCommand();
        
        // When
        AddressViewDTO result = addressCommandService.update(addressId, updateCommand);
        
        // Then
        assertNotNull(result);
        verify(addressRepository).findById(addressId);
        verify(addressRepository).findCompanyIdByAddressId(addressId);
        verify(companyRepository).findById(companyId);
        verify(addressRepository).save(any(), eq(companyId));
        verify(addressMapper).toDomain(updateCommand, existingAddress);
        verify(addressMapper).toViewDTO(address, companyId);
        verify(metricsPort).incrementCounter(eq("address_updated_total"), any());
    }
}
