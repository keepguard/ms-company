package com.keepguard.ms_company.application.service.address;

import com.keepguard.ms_company.application.dto.address.AddressCreateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressViewDTO;
import com.keepguard.ms_company.application.mapper.AddressApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.AddressRepositoryPort;
import com.keepguard.ms_company.application.port.out.persistence.CompanyRepositoryPort;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.domain.entity.Address;
import com.keepguard.ms_company.domain.entity.Company;
import com.keepguard.ms_company.application.port.out.metrics.MetricsPort;
import com.keepguard.lib_common.logging.annotation.LogOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AddressCommandService {

    private final AddressRepositoryPort addressRepository;
    private final CompanyRepositoryPort companyRepository;
    private final AddressApplicationMapper addressMapper;
    private final MetricsPort metricsPort;

    @LogOperation(
        operation = "CREATE_ADDRESS",
        description = "Criando novo endereço para empresa: {companyId}",
        audit = true,
        auditAction = "CREATE",
        auditEntityType = "ADDRESS"
    )
    public AddressViewDTO create(UUID companyId, AddressCreateCommandDTO command) {
        // Verifica se a empresa existe
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("address_business_errors_total",
                    Map.of("error_code", "COMPANY_NOT_FOUND", "operation", "create"));
                throw new NotFoundException("Empresa não encontrada: " + companyId);
            });

        // Valida se o status da empresa permite operações
        company.validateStatusForOperations();

        // Desativa endereços ativos existentes da empresa
        addressRepository.findActiveByCompanyId(companyId)
            .ifPresent(activeAddress -> {
                activeAddress.deactivate();
                addressRepository.save(activeAddress);
            });

        Address address = addressMapper.toDomain(command);
        Address savedAddress = addressRepository.save(address, companyId);

        // Adiciona o endereço à empresa
        company.addAddress(savedAddress);
        companyRepository.save(company);

        // Registra métricas específicas
        metricsPort.incrementCounter("address_created_total",
            Map.of("entity_id", savedAddress.getId().toString(), "company_id", companyId.toString()));

        return addressMapper.toViewDTO(savedAddress, companyId);
    }

    @LogOperation(
        operation = "UPDATE_ADDRESS",
        description = "Atualizando endereço: {id}",
        audit = true,
        auditAction = "UPDATE",
        auditEntityType = "ADDRESS"
    )
    public AddressViewDTO update(UUID id, AddressUpdateCommandDTO command) {
        Address existingAddress = addressRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("address_not_found_total",
                    Map.of("entity_id", id.toString(), "operation", "update"));
                return new NotFoundException("Endereço não encontrado: " + id);
            });

        // Busca o companyId do endereço existente
        UUID companyId = findCompanyIdByAddressId(id);

        // Verifica se a empresa existe e valida seu status
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new NotFoundException("Empresa não encontrada: " + companyId));
        company.validateStatusForOperations();

        Address updatedAddress = addressMapper.toDomain(command, existingAddress);
        Address savedAddress = addressRepository.save(updatedAddress, companyId);

        // Registra métricas específicas
        metricsPort.incrementCounter("address_updated_total",
            Map.of("entity_id", id.toString()));

        return addressMapper.toViewDTO(savedAddress, companyId);
    }

    @LogOperation(
        operation = "ACTIVATE_ADDRESS",
        description = "Ativando endereço: {id}",
        audit = true,
        auditAction = "ACTIVATE",
        auditEntityType = "ADDRESS"
    )
    public AddressViewDTO activate(UUID id) {
        Address address = addressRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("address_not_found_total",
                    Map.of("entity_id", id.toString(), "operation", "activate"));
                return new NotFoundException("Endereço não encontrado: " + id);
            });

        // Desativa outros endereços ativos da mesma empresa
        UUID companyId = findCompanyIdByAddressId(id);
        addressRepository.findActiveByCompanyId(companyId)
            .ifPresent(activeAddress -> {
                if (!activeAddress.getId().equals(id)) {
                    activeAddress.deactivate();
                    addressRepository.save(activeAddress);
                }
            });

        address.activate();
        Address updatedAddress = addressRepository.save(address, companyId);

        // Registra métricas específicas
        metricsPort.incrementCounter("address_activated_total",
            Map.of("entity_id", id.toString()));

        return addressMapper.toViewDTO(updatedAddress, companyId);
    }

    @LogOperation(
        operation = "DEACTIVATE_ADDRESS",
        description = "Desativando endereço: {id}",
        audit = true,
        auditAction = "DEACTIVATE",
        auditEntityType = "ADDRESS"
    )
    public AddressViewDTO deactivate(UUID id) {
        Address address = addressRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("address_not_found_total",
                    Map.of("entity_id", id.toString(), "operation", "deactivate"));
                return new NotFoundException("Endereço não encontrado: " + id);
            });

        address.deactivate();
        UUID companyId = findCompanyIdByAddressId(id);
        Address updatedAddress = addressRepository.save(address, companyId);

        // Registra métricas específicas
        metricsPort.incrementCounter("address_deactivated_total",
            Map.of("entity_id", id.toString()));

        return addressMapper.toViewDTO(updatedAddress, companyId);
    }

    @LogOperation(
        operation = "DELETE_ADDRESS",
        description = "Removendo endereço: {id}",
        audit = true,
        auditAction = "DELETE",
        auditEntityType = "ADDRESS"
    )
    public void delete(UUID id) {
        if (!addressRepository.existsById(id)) {
            metricsPort.incrementCounter("address_not_found_total",
                Map.of("entity_id", id.toString(), "operation", "delete"));
            throw new NotFoundException("Endereço não encontrado: " + id);
        }

        addressRepository.deleteById(id);

        // Registra métricas específicas
        metricsPort.incrementCounter("address_deleted_total",
            Map.of("entity_id", id.toString()));
    }

    private UUID findCompanyIdByAddressId(UUID addressId) {
        // Busca o companyId através do repository
        return addressRepository.findCompanyIdByAddressId(addressId)
            .orElseThrow(() -> new NotFoundException("Empresa não encontrada para o endereço: " + addressId));
    }
}
