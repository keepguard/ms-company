package com.keepguard.ms_company.application.port.out.persistence;

import com.keepguard.ms_company.application.dto.bankaccount.BankAccountSearchCriteriaDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.domain.entity.BankAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepositoryPort {

    BankAccount save(BankAccount bankAccount);

    BankAccount save(BankAccount bankAccount, UUID companyId);

    Optional<BankAccount> findById(UUID id);

    List<BankAccount> findAll();

    void deleteById(UUID id);

    void delete(BankAccount bankAccount);

    List<BankAccount> findByCompanyId(UUID companyId);

    Optional<BankAccount> findActiveByCompanyId(UUID companyId);

    List<BankAccount> findAllActive();

    boolean existsById(UUID id);

    List<BankAccount> findByBankCode(String bankCode);

    List<BankAccount> findByAccountType(String accountType);

    PageResultDTO<BankAccount> search(BankAccountSearchCriteriaDTO criteria);

    Optional<UUID> findCompanyIdByBankAccountId(UUID bankAccountId);
}

