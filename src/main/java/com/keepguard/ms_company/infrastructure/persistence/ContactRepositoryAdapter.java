package com.keepguard.ms_company.infrastructure.persistence;

import com.keepguard.ms_company.application.dto.contact.ContactSearchCriteriaDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.port.out.persistence.ContactRepositoryPort;
import com.keepguard.ms_company.domain.entity.Contact;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyContactJpaEntity;
import com.keepguard.ms_company.infrastructure.persistence.mapper.ContactJpaMapper;
import com.keepguard.ms_company.infrastructure.persistence.spring.ContactSpringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;

@Repository
@RequiredArgsConstructor
@Retry(name = "databaseOperation")
@Bulkhead(name = "databaseOperation")
public class ContactRepositoryAdapter implements ContactRepositoryPort {

    private final ContactSpringRepository springRepository;
    private final ContactJpaMapper mapper;

    @Override
    public Contact save(Contact contact) {
        var entity = mapper.toEntity(contact);
        var savedEntity = springRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    public Contact save(Contact contact, UUID companyId) {
        CompanyContactJpaEntity entity = mapper.toEntity(contact, companyId);
        CompanyContactJpaEntity savedEntity = springRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Contact> findById(UUID id) {
        return springRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Contact> findAll() {
        return springRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        springRepository.deleteById(id);
    }

    @Override
    public void delete(Contact contact) {
        CompanyContactJpaEntity entity = mapper.toEntity(contact);
        springRepository.delete(entity);
    }

    @Override
    public List<Contact> findByCompanyId(UUID companyId) {
        return springRepository.findByCompanyId(companyId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Contact> findActiveByCompanyId(UUID companyId) {
        return springRepository.findActiveByCompanyId(companyId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Contact> findAllActive() {
        return springRepository.findAllActive().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(UUID id) {
        return springRepository.existsById(id);
    }

    @Override
    public Optional<Contact> findByEmail(String email) {
        return springRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public List<Contact> findByNameContainingIgnoreCase(String name) {
        return springRepository.findByNameContainingIgnoreCase(name).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Contact> findByPositionContainingIgnoreCase(String position) {
        return springRepository.findByPositionContainingIgnoreCase(position).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Contact> findByDepartmentContainingIgnoreCase(String department) {
        return springRepository.findByDepartmentContainingIgnoreCase(department).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public PageResultDTO<Contact> search(ContactSearchCriteriaDTO criteria) {
        Specification<CompanyContactJpaEntity> spec = buildSpecification(criteria);

        Sort sort = buildSort(criteria.sortFields(), criteria.sortDirection());
        Pageable pageable = PageRequest.of(criteria.page(), criteria.size(), sort);

        Page<CompanyContactJpaEntity> page = springRepository.findAll(spec, pageable);

        List<Contact> contacts = page.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        return new PageResultDTO<>(contacts, page.getTotalElements(), criteria.page(), criteria.size());
    }

    @Override
    public Optional<UUID> findCompanyIdByContactId(UUID contactId) {
        return springRepository.findCompanyIdByContactId(contactId);
    }

    private Specification<CompanyContactJpaEntity> buildSpecification(ContactSearchCriteriaDTO criteria) {
        return Specification.where(companyIdEquals(criteria.companyId()))
                .and(nameContains(criteria.name()))
                .and(emailContains(criteria.email()))
                .and(positionContains(criteria.position()))
                .and(departmentContains(criteria.department()))
                .and(activeEquals(criteria.active()));
    }

    private Specification<CompanyContactJpaEntity> companyIdEquals(UUID companyId) {
        return (root, query, cb) -> companyId != null ?
            cb.equal(root.get("company").get("id"), companyId) : null;
    }

    private Specification<CompanyContactJpaEntity> nameContains(String name) {
        return (root, query, cb) -> name != null && !name.trim().isEmpty() ?
            cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%") : null;
    }

    private Specification<CompanyContactJpaEntity> emailContains(String email) {
        return (root, query, cb) -> email != null && !email.trim().isEmpty() ?
            cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%") : null;
    }

    private Specification<CompanyContactJpaEntity> positionContains(String position) {
        return (root, query, cb) -> position != null && !position.trim().isEmpty() ?
            cb.like(cb.lower(root.get("position")), "%" + position.toLowerCase() + "%") : null;
    }

    private Specification<CompanyContactJpaEntity> departmentContains(String department) {
        return (root, query, cb) -> department != null && !department.trim().isEmpty() ?
            cb.like(cb.lower(root.get("department")), "%" + department.toLowerCase() + "%") : null;
    }

    private Specification<CompanyContactJpaEntity> activeEquals(Boolean active) {
        return (root, query, cb) -> active != null ?
            cb.equal(root.get("active"), active) : null;
    }

    private Sort buildSort(List<String> sortFields, String sortDirection) {
        if (sortFields == null || sortFields.isEmpty()) {
            return Sort.by(Sort.Direction.ASC, "name");
        }

        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection) ?
            Sort.Direction.DESC : Sort.Direction.ASC;

        return Sort.by(direction, sortFields.toArray(new String[0]));
    }
}
