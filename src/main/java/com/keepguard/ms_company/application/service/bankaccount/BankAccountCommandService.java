package com.keepguard.ms_company.application.service.bankaccount;

import com.keepguard.ms_company.application.dto.bankaccount.BankAccountCreateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountViewDTO;
import com.keepguard.ms_company.application.mapper.BankAccountApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.BankAccountRepositoryPort;
import com.keepguard.ms_company.application.port.out.persistence.CompanyRepositoryPort;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.domain.entity.BankAccount;
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
public class BankAccountCommandService {

    private final BankAccountRepositoryPort bankAccountRepository;
    private final CompanyRepositoryPort companyRepository;
    private final BankAccountApplicationMapper bankAccountMapper;
    private final MetricsPort metricsPort;

    @LogOperation(
        operation = "CREATE_BANK_ACCOUNT",
        description = "Criando novos dados bancários para empresa: {companyId}",
        audit = true,
        auditAction = "CREATE",
        auditEntityType = "BANK_ACCOUNT"
    )
    public BankAccountViewDTO create(UUID companyId, BankAccountCreateCommandDTO command) {
        // Verifica se a empresa existe
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("bank_account_business_errors_total",
                    Map.of("error_code", "COMPANY_NOT_FOUND", "operation", "create"));
                throw new NotFoundException("Empresa não encontrada: " + companyId);
            });

        // Valida se o status da empresa permite operações
        company.validateStatusForOperations();

        // Desativa dados bancários ativos existentes da empresa
        bankAccountRepository.findActiveByCompanyId(companyId)
            .ifPresent(activeBankAccount -> {
                activeBankAccount.deactivate();
                bankAccountRepository.save(activeBankAccount);
            });

        BankAccount bankAccount = bankAccountMapper.toDomain(command);
        BankAccount savedBankAccount = bankAccountRepository.save(bankAccount, companyId);

        // Adiciona os dados bancários à empresa
        company.addBankAccount(savedBankAccount);
        companyRepository.save(company);

        // Registra métricas específicas
        metricsPort.incrementCounter("bank_account_created_total",
            Map.of("entity_id", savedBankAccount.getId().toString(), "company_id", companyId.toString()));

        return bankAccountMapper.toViewDTO(savedBankAccount, companyId);
    }

    @LogOperation(
        operation = "UPDATE_BANK_ACCOUNT",
        description = "Atualizando dados bancários: {id}",
        audit = true,
        auditAction = "UPDATE",
        auditEntityType = "BANK_ACCOUNT"
    )
    public BankAccountViewDTO update(UUID id, BankAccountUpdateCommandDTO command) {
        BankAccount existingBankAccount = bankAccountRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("bank_account_not_found_total",
                    Map.of("entity_id", id.toString(), "operation", "update"));
                return new NotFoundException("Dados bancários não encontrados: " + id);
            });

        // Busca o companyId dos dados bancários existentes
        UUID companyId = findCompanyIdByBankAccountId(id);

        // Verifica se a empresa existe e valida seu status
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new NotFoundException("Empresa não encontrada: " + companyId));
        company.validateStatusForOperations();

        BankAccount updatedBankAccount = bankAccountMapper.toDomain(command, existingBankAccount);
        BankAccount savedBankAccount = bankAccountRepository.save(updatedBankAccount, companyId);

        // Registra métricas específicas
        metricsPort.incrementCounter("bank_account_updated_total",
            Map.of("entity_id", id.toString()));

        return bankAccountMapper.toViewDTO(savedBankAccount, companyId);
    }

    @LogOperation(
        operation = "ACTIVATE_BANK_ACCOUNT",
        description = "Ativando dados bancários: {id}",
        audit = true,
        auditAction = "ACTIVATE",
        auditEntityType = "BANK_ACCOUNT"
    )
    public BankAccountViewDTO activate(UUID id) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("bank_account_not_found_total",
                    Map.of("entity_id", id.toString(), "operation", "activate"));
                return new NotFoundException("Dados bancários não encontrados: " + id);
            });

        // Desativa outros dados bancários ativos da mesma empresa
        UUID companyId = findCompanyIdByBankAccountId(id);
        bankAccountRepository.findActiveByCompanyId(companyId)
            .ifPresent(activeBankAccount -> {
                if (!activeBankAccount.getId().equals(id)) {
                    activeBankAccount.deactivate();
                    bankAccountRepository.save(activeBankAccount);
                }
            });

        bankAccount.activate();
        BankAccount updatedBankAccount = bankAccountRepository.save(bankAccount, companyId);

        // Registra métricas específicas
        metricsPort.incrementCounter("bank_account_activated_total",
            Map.of("entity_id", id.toString()));

        return bankAccountMapper.toViewDTO(updatedBankAccount, companyId);
    }

    @LogOperation(
        operation = "DEACTIVATE_BANK_ACCOUNT",
        description = "Desativando dados bancários: {id}",
        audit = true,
        auditAction = "DEACTIVATE",
        auditEntityType = "BANK_ACCOUNT"
    )
    public BankAccountViewDTO deactivate(UUID id) {
        BankAccount bankAccount = bankAccountRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("bank_account_not_found_total",
                    Map.of("entity_id", id.toString(), "operation", "deactivate"));
                return new NotFoundException("Dados bancários não encontrados: " + id);
            });

        bankAccount.deactivate();
        UUID companyId = findCompanyIdByBankAccountId(id);
        BankAccount updatedBankAccount = bankAccountRepository.save(bankAccount, companyId);

        // Registra métricas específicas
        metricsPort.incrementCounter("bank_account_deactivated_total",
            Map.of("entity_id", id.toString()));

        return bankAccountMapper.toViewDTO(updatedBankAccount, companyId);
    }

    @LogOperation(
        operation = "DELETE_BANK_ACCOUNT",
        description = "Removendo dados bancários: {id}",
        audit = true,
        auditAction = "DELETE",
        auditEntityType = "BANK_ACCOUNT"
    )
    public void delete(UUID id) {
        if (!bankAccountRepository.existsById(id)) {
            metricsPort.incrementCounter("bank_account_not_found_total",
                Map.of("entity_id", id.toString(), "operation", "delete"));
            throw new NotFoundException("Dados bancários não encontrados: " + id);
        }

        bankAccountRepository.deleteById(id);

        // Registra métricas específicas
        metricsPort.incrementCounter("bank_account_deleted_total",
            Map.of("entity_id", id.toString()));
    }

    private UUID findCompanyIdByBankAccountId(UUID bankAccountId) {
        // Busca o companyId através do repository
        return bankAccountRepository.findCompanyIdByBankAccountId(bankAccountId)
            .orElseThrow(() -> new NotFoundException("Empresa não encontrada para os dados bancários: " + bankAccountId));
    }
}
