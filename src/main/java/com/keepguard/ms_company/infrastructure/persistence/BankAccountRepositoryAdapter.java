package com.keepguard.ms_company.infrastructure.persistence;

import com.keepguard.ms_company.application.dto.bankaccount.BankAccountSearchCriteriaDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.port.out.persistence.BankAccountRepositoryPort;
import com.keepguard.ms_company.domain.entity.BankAccount;
import com.keepguard.ms_company.infrastructure.persistence.mapper.BankAccountJpaMapper;
import com.keepguard.ms_company.infrastructure.persistence.spring.BankAccountSpringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class BankAccountRepositoryAdapter implements BankAccountRepositoryPort {

    private final BankAccountSpringRepository springRepository;
    private final BankAccountJpaMapper mapper;

    @Override
    public BankAccount save(BankAccount bankAccount) {
        var entity = mapper.toEntity(bankAccount);
        var savedEntity = springRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    public BankAccount save(BankAccount bankAccount, UUID companyId) {
        var entity = mapper.toEntity(bankAccount, companyId);
        var savedEntity = springRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<BankAccount> findById(UUID id) {
        return springRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<BankAccount> findAll() {
        return springRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        springRepository.deleteById(id);
    }

    @Override
    public void delete(BankAccount bankAccount) {
        var entity = mapper.toEntity(bankAccount);
        springRepository.delete(entity);
    }

    @Override
    public List<BankAccount> findByCompanyId(UUID companyId) {
        return springRepository.findByCompanyId(companyId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BankAccount> findActiveByCompanyId(UUID companyId) {
        return springRepository.findActiveByCompanyId(companyId)
                .map(mapper::toDomain);
    }

    @Override
    public List<BankAccount> findAllActive() {
        return springRepository.findAllActive().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(UUID id) {
        return springRepository.existsById(id);
    }

    @Override
    public List<BankAccount> findByBankCode(String bankCode) {
        return springRepository.findByBankCode(bankCode).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<BankAccount> findByAccountType(String accountType) {
        return springRepository.findByAccountType(accountType).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public PageResultDTO<BankAccount> search(BankAccountSearchCriteriaDTO criteria) {
        // Cria o objeto de paginação
        Sort sort = Sort.unsorted();
        if (criteria.sortFields() != null && !criteria.sortFields().isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(criteria.sortDirection())
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, criteria.sortFields().toArray(new String[0]));
        }

        Pageable pageable = PageRequest.of(criteria.page(), criteria.size(), sort);

        // Executa a busca com filtros
        var page = springRepository.findByFilters(
            criteria.companyId(),
            criteria.bankCode(),
            criteria.accountType(),
            criteria.active(),
            pageable
        );

        // Converte para domínio
        List<BankAccount> content = page.getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());

        return new PageResultDTO<>(
            content,
            page.getTotalElements(),
            page.getNumber(),
            page.getSize()
        );
    }

    @Override
    public Optional<UUID> findCompanyIdByBankAccountId(UUID bankAccountId) {
        return springRepository.findCompanyIdByBankAccountId(bankAccountId);
    }
}
