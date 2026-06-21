package com.keepguard.ms_company.application.service.contact;

import com.keepguard.ms_company.application.dto.contact.ContactViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.contact.ContactSearchCriteriaDTO;
import com.keepguard.ms_company.application.mapper.ContactApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.ContactRepositoryPort;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.application.service.exception.QueryOperationException;
import com.keepguard.ms_company.domain.entity.Contact;
import com.keepguard.ms_company.application.port.out.cache.ContactCachePort;
import com.keepguard.ms_company.application.port.out.metrics.MetricsPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContactQueryService {

    private final ContactRepositoryPort contactRepository;
    private final ContactApplicationMapper contactMapper;
    private final ContactCachePort contactCachePort;
    private final MetricsPort metricsPort;

    public ContactViewDTO getById(UUID id) {

        try {
            Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> {
                    metricsPort.incrementCounter("contact_not_found_total",
                        Map.of("entity_id", id.toString(), "operation", "get_by_id"));
                    return new NotFoundException("Contato não encontrado: " + id, "CONTACT_NOT_FOUND", Map.of("contactId", id));
                });

            metricsPort.incrementCounter("contact_queries_total",
                Map.of("query_type", "GET_BY_ID", "status", "SUCCESS"));

            // Busca o companyId do contato
            UUID companyId = findCompanyIdByContactId(id);

            return contactMapper.toViewDTO(contact, companyId);

        } catch (NotFoundException e) {
            // Re-throw exceções de negócio sem wrapping
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar contato por ID: {} - Erro: {}", id, e.getMessage(), e);
            metricsPort.incrementCounter("contact_system_errors_total",
                Map.of("error_type", "GET_CONTACT_BY_ID_ERROR", "operation", "get_by_id"));
            throw new QueryOperationException("Falha ao buscar contato", "getById", "CONTACT_QUERY_ERROR", Map.of("contactId", id), e);
        }
    }

    public List<ContactViewDTO> listByCompanyId(UUID companyId) {

        try {
            // Tentar buscar no cache primeiro
            List<ContactViewDTO> cachedContacts = contactCachePort.getContactsByCompanyIdFromCache(companyId.toString());
            if (cachedContacts != null) {
                metricsPort.incrementCounter("contact_queries_total",
                    Map.of("query_type", "LIST_BY_COMPANY", "status", "CACHE_HIT", "count", String.valueOf(cachedContacts.size())));
                return cachedContacts;
            }

            // Se não encontrou no cache, buscar no banco
            List<Contact> contacts = contactRepository.findByCompanyId(companyId);
            List<ContactViewDTO> views = contacts.stream()
                .map(contact -> contactMapper.toViewDTO(contact, companyId))
                .toList();

            // Cachear o resultado
            contactCachePort.cacheContactsByCompanyId(companyId.toString(), views);

            metricsPort.incrementCounter("contact_queries_total",
                Map.of("query_type", "LIST_BY_COMPANY", "status", "SUCCESS", "count", String.valueOf(views.size())));

            return views;

        } catch (Exception e) {
            log.error("Erro ao listar contatos da empresa: {} - Erro: {}", companyId, e.getMessage(), e);
            metricsPort.incrementCounter("contact_system_errors_total",
                Map.of("error_type", "LIST_CONTACTS_BY_COMPANY_ERROR", "operation", "list_by_company"));
            throw new QueryOperationException("Falha ao listar contatos da empresa", "listByCompanyId", "CONTACT_QUERY_ERROR", Map.of("companyId", companyId), e);
        }
    }

    public List<ContactViewDTO> listActiveByCompanyId(UUID companyId) {

        try {
            // Tentar buscar no cache primeiro
            List<ContactViewDTO> cachedContacts = contactCachePort.getActiveContactsByCompanyIdFromCache(companyId.toString());
            if (cachedContacts != null) {
                metricsPort.incrementCounter("contact_queries_total",
                    Map.of("query_type", "LIST_ACTIVE_BY_COMPANY", "status", "CACHE_HIT", "count", String.valueOf(cachedContacts.size())));
                return cachedContacts;
            }

            // Se não encontrou no cache, buscar no banco
            List<Contact> contacts = contactRepository.findActiveByCompanyId(companyId);
            List<ContactViewDTO> views = contacts.stream()
                .map(contact -> contactMapper.toViewDTO(contact, companyId))
                .toList();

            // Cachear o resultado
            contactCachePort.cacheActiveContactsByCompanyId(companyId.toString(), views);

            metricsPort.incrementCounter("contact_queries_total",
                Map.of("query_type", "LIST_ACTIVE_BY_COMPANY", "status", "SUCCESS", "count", String.valueOf(views.size())));

            return views;

        } catch (Exception e) {
            log.error("Erro ao listar contatos ativos da empresa: {} - Erro: {}", companyId, e.getMessage(), e);
            metricsPort.incrementCounter("contact_system_errors_total",
                Map.of("error_type", "LIST_ACTIVE_CONTACTS_BY_COMPANY_ERROR", "operation", "list_active_by_company"));
            throw new QueryOperationException("Falha ao listar contatos ativos da empresa", "listActiveByCompanyId", "CONTACT_QUERY_ERROR", Map.of("companyId", companyId), e);
        }
    }

    public List<ContactViewDTO> listAll() {

        try {
            List<Contact> contacts = contactRepository.findAll();
            List<ContactViewDTO> views = contacts.stream()
                .map(contact -> {
                    UUID companyId = findCompanyIdByContactId(contact.getId());
                    return contactMapper.toViewDTO(contact, companyId);
                })
                .toList();

            metricsPort.incrementCounter("contact_queries_total",
                Map.of("query_type", "LIST_ALL", "status", "SUCCESS", "count", String.valueOf(views.size())));

            return views;

        } catch (Exception e) {
            log.error("Erro ao listar todos os contatos - Erro: {}", e.getMessage(), e);
            metricsPort.incrementCounter("contact_system_errors_total",
                Map.of("error_type", "LIST_ALL_CONTACTS_ERROR", "operation", "list_all"));
            throw new QueryOperationException("Falha ao listar todos os contatos", "listAll", "CONTACT_QUERY_ERROR", Map.of(), e);
        }
    }

    public PageResultDTO<ContactViewDTO> search(ContactSearchCriteriaDTO criteria) {

        try {
            PageResultDTO<Contact> contacts = contactRepository.search(criteria);
            PageResultDTO<ContactViewDTO> views = new PageResultDTO<>(
                contacts.items().stream()
                    .map(contact -> {
                        UUID companyId = findCompanyIdByContactId(contact.getId());
                        return contactMapper.toViewDTO(contact, companyId);
                    })
                    .toList(),
                contacts.total(),
                contacts.page(),
                contacts.size()
            );

            metricsPort.incrementCounter("contact_queries_total",
                Map.of("query_type", "SEARCH", "status", "SUCCESS", "count", String.valueOf(views.total())));

            return views;

        } catch (Exception e) {
            log.error("Erro ao buscar contatos com critérios - Erro: {}", e.getMessage(), e);
            metricsPort.incrementCounter("contact_system_errors_total",
                Map.of("error_type", "SEARCH_CONTACTS_ERROR", "operation", "search"));
            throw new QueryOperationException("Falha ao buscar contatos com critérios", "search", "CONTACT_QUERY_ERROR", Map.of("criteria", criteria), e);
        }
    }

    private UUID findCompanyIdByContactId(UUID contactId) {
        // Busca o companyId através do repository
        return contactRepository.findCompanyIdByContactId(contactId)
            .orElseThrow(() -> new NotFoundException("Empresa não encontrada para o contato: " + contactId, "COMPANY_NOT_FOUND", Map.of("contactId", contactId)));
    }
}
