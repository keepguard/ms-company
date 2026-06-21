package com.keepguard.ms_company.application.port.out.persistence;

import com.keepguard.ms_company.application.dto.contact.ContactSearchCriteriaDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.domain.entity.Contact;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactRepositoryPort {

    Contact save(Contact contact);

    Contact save(Contact contact, UUID companyId);

    Optional<Contact> findById(UUID id);

    List<Contact> findAll();

    void deleteById(UUID id);

    void delete(Contact contact);

    List<Contact> findByCompanyId(UUID companyId);

    List<Contact> findActiveByCompanyId(UUID companyId);

    List<Contact> findAllActive();

    boolean existsById(UUID id);

    Optional<Contact> findByEmail(String email);

    List<Contact> findByNameContainingIgnoreCase(String name);

    List<Contact> findByPositionContainingIgnoreCase(String position);

    List<Contact> findByDepartmentContainingIgnoreCase(String department);

    PageResultDTO<Contact> search(ContactSearchCriteriaDTO criteria);

    Optional<UUID> findCompanyIdByContactId(UUID contactId);
}

