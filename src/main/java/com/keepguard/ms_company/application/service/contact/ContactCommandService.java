package com.keepguard.ms_company.application.service.contact;

import com.keepguard.ms_company.application.dto.contact.ContactCreateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactViewDTO;
import com.keepguard.ms_company.application.mapper.ContactApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.ContactRepositoryPort;
import com.keepguard.ms_company.application.port.out.persistence.CompanyRepositoryPort;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.application.service.exception.AlreadyExistsException;
import com.keepguard.ms_company.domain.entity.Contact;
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
public class ContactCommandService {

    private final ContactRepositoryPort contactRepository;
    private final CompanyRepositoryPort companyRepository;
    private final ContactApplicationMapper contactMapper;
    private final MetricsPort metricsPort;

    @LogOperation(
        operation = "CREATE_CONTACT",
        description = "Criando novo contato para empresa: {companyId}",
        audit = true,
        auditAction = "CREATE",
        auditEntityType = "CONTACT"
    )
    public ContactViewDTO create(UUID companyId, ContactCreateCommandDTO command) {
        // Verifica se a empresa existe
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("contact_business_errors_total",
                    Map.of("error_code", "COMPANY_NOT_FOUND", "operation", "create"));
                throw new NotFoundException("Empresa não encontrada: " + companyId);
            });

        // Valida se o status da empresa permite operações
        company.validateStatusForOperations();

        // Verifica se já existe contato com o mesmo email
        contactRepository.findByEmail(command.email())
            .ifPresent(existingContact -> {
                metricsPort.incrementCounter("contact_business_errors_total",
                    Map.of("error_code", "EMAIL_ALREADY_EXISTS", "operation", "create"));
                throw new AlreadyExistsException("Já existe um contato com o email: " + command.email());
            });

        Contact contact = contactMapper.toDomain(command);
        Contact savedContact = contactRepository.save(contact, companyId);

        // Adiciona o contato à empresa
        company.addContact(savedContact);
        companyRepository.save(company);

        // Registra métricas específicas
        metricsPort.incrementCounter("contact_created_total",
            Map.of("entity_id", savedContact.getId().toString(), "company_id", companyId.toString()));

        return contactMapper.toViewDTO(savedContact, companyId);
    }

    @LogOperation(
        operation = "UPDATE_CONTACT",
        description = "Atualizando contato: {id}",
        audit = true,
        auditAction = "UPDATE",
        auditEntityType = "CONTACT"
    )
    public ContactViewDTO update(UUID id, ContactUpdateCommandDTO command) {
        Contact existingContact = contactRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("contact_not_found_total",
                    Map.of("entity_id", id.toString(), "operation", "update"));
                return new NotFoundException("Contato não encontrado: " + id);
            });

        // Busca o companyId do contato existente
        UUID companyId = findCompanyIdByContactId(id);

        // Verifica se a empresa existe e valida seu status
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new NotFoundException("Empresa não encontrada: " + companyId));
        company.validateStatusForOperations();

        // Se o email está sendo alterado, verifica se já existe outro contato com o mesmo email
        if (command.email() != null && !command.email().equals(existingContact.getEmail())) {
            contactRepository.findByEmail(command.email())
                .ifPresent(contactWithSameEmail -> {
                    if (!contactWithSameEmail.getId().equals(id)) {
                        metricsPort.incrementCounter("contact_business_errors_total",
                            Map.of("error_code", "EMAIL_ALREADY_EXISTS", "operation", "update"));
                        throw new AlreadyExistsException("Já existe um contato com o email: " + command.email());
                    }
                });
        }

        Contact updatedContact = contactMapper.toDomain(command, existingContact);
        Contact savedContact = contactRepository.save(updatedContact, companyId);

        // Registra métricas específicas
        metricsPort.incrementCounter("contact_updated_total",
            Map.of("entity_id", id.toString()));

        return contactMapper.toViewDTO(savedContact, companyId);
    }

    @LogOperation(
        operation = "ACTIVATE_CONTACT",
        description = "Ativando contato: {id}",
        audit = true,
        auditAction = "ACTIVATE",
        auditEntityType = "CONTACT"
    )
    public ContactViewDTO activate(UUID id) {
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("contact_not_found_total",
                    Map.of("entity_id", id.toString(), "operation", "activate"));
                return new NotFoundException("Contato não encontrado: " + id);
            });

        contact.activate();
        UUID companyId = findCompanyIdByContactId(id);
        Contact updatedContact = contactRepository.save(contact, companyId);

        // Registra métricas específicas
        metricsPort.incrementCounter("contact_activated_total",
            Map.of("entity_id", id.toString()));

        return contactMapper.toViewDTO(updatedContact, companyId);
    }

    @LogOperation(
        operation = "DEACTIVATE_CONTACT",
        description = "Desativando contato: {id}",
        audit = true,
        auditAction = "DEACTIVATE",
        auditEntityType = "CONTACT"
    )
    public ContactViewDTO deactivate(UUID id) {
        Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("contact_not_found_total",
                    Map.of("entity_id", id.toString(), "operation", "deactivate"));
                return new NotFoundException("Contato não encontrado: " + id);
            });

        contact.deactivate();
        UUID companyId = findCompanyIdByContactId(id);
        Contact updatedContact = contactRepository.save(contact, companyId);

        // Registra métricas específicas
        metricsPort.incrementCounter("contact_deactivated_total",
            Map.of("entity_id", id.toString()));

        return contactMapper.toViewDTO(updatedContact, companyId);
    }

    @LogOperation(
        operation = "DELETE_CONTACT",
        description = "Removendo contato: {id}",
        audit = true,
        auditAction = "DELETE",
        auditEntityType = "CONTACT"
    )
    public void delete(UUID id) {
        if (!contactRepository.existsById(id)) {
            metricsPort.incrementCounter("contact_not_found_total",
                Map.of("entity_id", id.toString(), "operation", "delete"));
            throw new NotFoundException("Contato não encontrado: " + id);
        }

        contactRepository.deleteById(id);

        // Registra métricas específicas
        metricsPort.incrementCounter("contact_deleted_total",
            Map.of("entity_id", id.toString()));
    }

    private UUID findCompanyIdByContactId(UUID contactId) {
        // Busca o companyId através do repository
        return contactRepository.findCompanyIdByContactId(contactId)
            .orElseThrow(() -> new NotFoundException("Empresa não encontrada para o contato: " + contactId));
    }
}
