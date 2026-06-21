package com.keepguard.ms_company.application.service.bankaccount;

import com.keepguard.ms_company.application.dto.bankaccount.BankAccountViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountSearchCriteriaDTO;
import com.keepguard.ms_company.application.mapper.BankAccountApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.BankAccountRepositoryPort;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.application.service.exception.QueryOperationException;
import com.keepguard.ms_company.domain.entity.BankAccount;
import com.keepguard.ms_company.application.port.out.cache.BankAccountCachePort;
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
public class BankAccountQueryService {

    private final BankAccountRepositoryPort bankAccountRepository;
    private final BankAccountApplicationMapper bankAccountMapper;
    private final BankAccountCachePort bankAccountCachePort;
    private final MetricsPort metricsPort;

    public BankAccountViewDTO getById(UUID id) {

        try {
            BankAccount bankAccount = bankAccountRepository.findById(id)
                .orElseThrow(() -> {
                    metricsPort.incrementCounter("bank_account_not_found_total",
                        Map.of("entity_id", id.toString(), "operation", "get_by_id"));
                    return new NotFoundException("Dados bancários não encontrados: " + id);
                });

            metricsPort.incrementCounter("bank_account_queries_total",
                Map.of("query_type", "GET_BY_ID", "status", "SUCCESS"));

            // Busca o companyId dos dados bancários
            UUID companyId = findCompanyIdByBankAccountId(id);

            return bankAccountMapper.toViewDTO(bankAccount, companyId);

        } catch (NotFoundException e) {
            // Re-throw exceções de negócio sem wrapping
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar dados bancários por ID: {} - Erro: {}", id, e.getMessage(), e);
            metricsPort.incrementCounter("bank_account_system_errors_total",
                Map.of("error_type", "GET_BANK_ACCOUNT_BY_ID_ERROR", "operation", "get_by_id"));
            throw new QueryOperationException("Falha ao buscar dados bancários", "getById", "BANK_ACCOUNT_QUERY_ERROR", Map.of("bankAccountId", id), e);
        }
    }

    public List<BankAccountViewDTO> listByCompanyId(UUID companyId) {

        try {
            // Tentar buscar no cache primeiro
            List<BankAccountViewDTO> cachedBankAccounts = bankAccountCachePort.getBankAccountsByCompanyIdFromCache(companyId.toString());
            if (cachedBankAccounts != null) {
                metricsPort.incrementCounter("bank_account_queries_total",
                    Map.of("query_type", "LIST_BY_COMPANY", "status", "CACHE_HIT", "count", String.valueOf(cachedBankAccounts.size())));
                return cachedBankAccounts;
            }

            // Se não encontrou no cache, buscar no banco
            List<BankAccount> bankAccounts = bankAccountRepository.findByCompanyId(companyId);
            List<BankAccountViewDTO> views = bankAccounts.stream()
                .map(bankAccount -> bankAccountMapper.toViewDTO(bankAccount, companyId))
                .toList();

            // Cachear o resultado
            bankAccountCachePort.cacheBankAccountsByCompanyId(companyId.toString(), views);

            metricsPort.incrementCounter("bank_account_queries_total",
                Map.of("query_type", "LIST_BY_COMPANY", "status", "SUCCESS", "count", String.valueOf(views.size())));

            return views;

        } catch (Exception e) {
            log.error("Erro ao listar dados bancários da empresa: {} - Erro: {}", companyId, e.getMessage(), e);
            metricsPort.incrementCounter("bank_account_system_errors_total",
                Map.of("error_type", "LIST_BANK_ACCOUNTS_BY_COMPANY_ERROR", "operation", "list_by_company"));
            throw new QueryOperationException("Falha ao listar dados bancários da empresa", "listByCompanyId", "BANK_ACCOUNT_QUERY_ERROR", Map.of("companyId", companyId), e);
        }
    }

    public BankAccountViewDTO getActiveByCompanyId(UUID companyId) {

        try {
            // Tentar buscar no cache primeiro
            BankAccountViewDTO cachedBankAccount = bankAccountCachePort.getActiveBankAccountByCompanyIdFromCache(companyId.toString());
            if (cachedBankAccount != null) {
                metricsPort.incrementCounter("bank_account_queries_total",
                    Map.of("query_type", "GET_ACTIVE_BY_COMPANY", "status", "CACHE_HIT"));
                return cachedBankAccount;
            }

            // Se não encontrou no cache, buscar no banco
            BankAccount bankAccount = bankAccountRepository.findActiveByCompanyId(companyId)
                .orElseThrow(() -> {
                    metricsPort.incrementCounter("bank_account_not_found_total",
                        Map.of("company_id", companyId.toString(), "operation", "get_active_by_company"));
                    return new NotFoundException("Dados bancários ativos não encontrados para a empresa: " + companyId);
                });

            BankAccountViewDTO bankAccountView = bankAccountMapper.toViewDTO(bankAccount, companyId);

            // Cachear o resultado
            bankAccountCachePort.cacheActiveBankAccountByCompanyId(companyId.toString(), bankAccountView);

            metricsPort.incrementCounter("bank_account_queries_total",
                Map.of("query_type", "GET_ACTIVE_BY_COMPANY", "status", "SUCCESS"));

            return bankAccountView;

        } catch (NotFoundException e) {
            // Re-throw exceções de negócio sem wrapping
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar dados bancários ativos da empresa: {} - Erro: {}", companyId, e.getMessage(), e);
            metricsPort.incrementCounter("bank_account_system_errors_total",
                Map.of("error_type", "GET_ACTIVE_BANK_ACCOUNT_BY_COMPANY_ERROR", "operation", "get_active_by_company"));
            throw new QueryOperationException("Falha ao buscar dados bancários ativos da empresa", "getActiveByCompanyId", "BANK_ACCOUNT_QUERY_ERROR", Map.of("companyId", companyId), e);
        }
    }

    public List<BankAccountViewDTO> listAll() {

        try {
            List<BankAccount> bankAccounts = bankAccountRepository.findAll();
            List<BankAccountViewDTO> views = bankAccounts.stream()
                .map(bankAccount -> {
                    UUID companyId = findCompanyIdByBankAccountId(bankAccount.getId());
                    return bankAccountMapper.toViewDTO(bankAccount, companyId);
                })
                .toList();

            metricsPort.incrementCounter("bank_account_queries_total",
                Map.of("query_type", "LIST_ALL", "status", "SUCCESS", "count", String.valueOf(views.size())));

            return views;

        } catch (Exception e) {
            log.error("Erro ao listar todos os dados bancários - Erro: {}", e.getMessage(), e);
            metricsPort.incrementCounter("bank_account_system_errors_total",
                Map.of("error_type", "LIST_ALL_BANK_ACCOUNTS_ERROR", "operation", "list_all"));
            throw new QueryOperationException("Falha ao listar todos os dados bancários", "listAll", "BANK_ACCOUNT_QUERY_ERROR", Map.of(), e);
        }
    }

    public PageResultDTO<BankAccountViewDTO> search(BankAccountSearchCriteriaDTO criteria) {

        try {
            PageResultDTO<BankAccount> bankAccounts = bankAccountRepository.search(criteria);
            PageResultDTO<BankAccountViewDTO> views = new PageResultDTO<>(
                bankAccounts.items().stream()
                    .map(bankAccount -> {
                        UUID companyId = findCompanyIdByBankAccountId(bankAccount.getId());
                        return bankAccountMapper.toViewDTO(bankAccount, companyId);
                    })
                    .toList(),
                bankAccounts.total(),
                bankAccounts.page(),
                bankAccounts.size()
            );

            metricsPort.incrementCounter("bank_account_queries_total",
                Map.of("query_type", "SEARCH", "status", "SUCCESS", "count", String.valueOf(views.total())));

            return views;

        } catch (Exception e) {
            log.error("Erro ao buscar dados bancários com critérios - Erro: {}", e.getMessage(), e);
            metricsPort.incrementCounter("bank_account_system_errors_total",
                Map.of("error_type", "SEARCH_BANK_ACCOUNTS_ERROR", "operation", "search"));
            throw new RuntimeException("Erro ao buscar dados bancários com critérios", e);
        }
    }

    private UUID findCompanyIdByBankAccountId(UUID bankAccountId) {
        // Busca o companyId através do repository
        return bankAccountRepository.findCompanyIdByBankAccountId(bankAccountId)
            .orElseThrow(() -> new NotFoundException("Empresa não encontrada para os dados bancários: " + bankAccountId));
    }
}
