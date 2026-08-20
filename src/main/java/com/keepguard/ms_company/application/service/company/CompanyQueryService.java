package com.keepguard.ms_company.application.service.company;

import com.keepguard.ms_company.adapters.in.rest.company.dto.response.CompanySimpleResponseDTO;
import com.keepguard.ms_company.application.dto.company.CompanyViewDTO;
import com.keepguard.ms_company.application.dto.company.CompanySearchCriteriaDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.mapper.CompanyApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.CompanyRepositoryPort;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.domain.entity.Company;
import com.keepguard.ms_company.application.port.out.cache.CompanyCachePort;
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
public class CompanyQueryService {

    private final CompanyRepositoryPort companyRepository;
    private final CompanyApplicationMapper companyMapper;
    private final CompanyCachePort companyCachePort;
    private final MetricsPort metricsPort;

    public CompanyViewDTO getById(UUID id) {
        try {
            // Tentar buscar no cache primeiro
            CompanyViewDTO cachedCompany = companyCachePort.getCompanyByIdFromCache(id.toString());
            if (cachedCompany != null) {
                metricsPort.incrementCounter("company_queries_total",
                    Map.of("query_type", "GET_BY_ID", "status", "CACHE_HIT"));
                return cachedCompany;
            }

            // Se não encontrou no cache, buscar no banco
            Company company = companyRepository.findById(id)
                .orElseThrow(() -> {
                    metricsPort.incrementCounter("company_not_found_total",
                        Map.of("entity_id", id.toString(), "operation", "get_by_id"));
                    return new NotFoundException("Empresa não encontrada: " + id, "COMPANY_NOT_FOUND", Map.of("companyId", id));
                });

            CompanyViewDTO companyView = companyMapper.toViewDTO(company);

            // Cachear o resultado
            companyCachePort.cacheCompanyById(id.toString(), companyView);

            metricsPort.incrementCounter("company_queries_total",
                Map.of("query_type", "GET_BY_ID", "status", "SUCCESS"));

            return companyView;

        } catch (NotFoundException e) {
            // Re-throw exceções de negócio sem wrapping
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar empresa por ID: {} - Erro: {}", id, e.getMessage(), e);
            metricsPort.incrementCounter("company_queries_total",
                Map.of("query_type", "GET_BY_ID", "status", "ERROR"));
            throw new RuntimeException("Erro interno ao buscar empresa por ID", e);
        }
    }

    public CompanyViewDTO getByCodeCompany(UUID codeCompany) {
        try {
            // Tentar buscar no cache primeiro
            CompanyViewDTO cachedCompany = companyCachePort.getCompanyByCodeCompanyFromCache(codeCompany.toString());
            if (cachedCompany != null) {
                metricsPort.incrementCounter("company_queries_total",
                    Map.of("query_type", "GET_BY_CODE_COMPANY", "status", "CACHE_HIT"));
                return cachedCompany;
            }

            // Se não encontrou no cache, buscar no banco
            Company company = companyRepository.findByCodeCompany(codeCompany)
                .orElseThrow(() -> {
                    metricsPort.incrementCounter("company_not_found_total",
                        Map.of("entity_id", codeCompany.toString(), "operation", "get_by_code_company"));
                    return new NotFoundException("Empresa não encontrada: " + codeCompany, "COMPANY_NOT_FOUND", Map.of("codeCompany", codeCompany));
                });

            CompanyViewDTO companyView = companyMapper.toViewDTO(company);

            // Cachear o resultado
            companyCachePort.cacheCompanyByCodeCompany(codeCompany.toString(), companyView);

            metricsPort.incrementCounter("company_queries_total",
                Map.of("query_type", "GET_BY_CODE_COMPANY", "status", "SUCCESS"));

            return companyView;

        } catch (NotFoundException e) {
            // Re-throw exceções de negócio sem wrapping
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar empresa por CodeCompany: {} - Erro: {}", codeCompany, e.getMessage(), e);
            metricsPort.incrementCounter("company_queries_total",
                Map.of("query_type", "GET_BY_CODE_COMPANY", "status", "ERROR"));
            throw new RuntimeException("Erro interno ao buscar empresa por CodeCompany", e);
        }
    }

    public CompanyViewDTO getByCnpj(String cnpj) {
        try {
            // Tentar buscar no cache primeiro
            CompanyViewDTO cachedCompany = companyCachePort.getCompanyByCnpjFromCache(cnpj);
            if (cachedCompany != null) {
                metricsPort.incrementCounter("company_queries_total",
                    Map.of("query_type", "GET_BY_CNPJ", "status", "CACHE_HIT"));
                return cachedCompany;
            }

            // Se não encontrou no cache, buscar no banco
            Company company = companyRepository.findByCnpj(cnpj)
                .orElseThrow(() -> {
                    metricsPort.incrementCounter("company_not_found_total",
                        Map.of("entity_id", cnpj, "operation", "get_by_cnpj"));
                    return new NotFoundException("Empresa não encontrada: " + cnpj, "COMPANY_NOT_FOUND", Map.of("cnpj", cnpj));
                });

            CompanyViewDTO companyView = companyMapper.toViewDTO(company);

            // Cachear o resultado
            companyCachePort.cacheCompanyByCnpj(cnpj, companyView);

            metricsPort.incrementCounter("company_queries_total",
                Map.of("query_type", "GET_BY_CNPJ", "status", "SUCCESS"));

            return companyView;

        } catch (NotFoundException e) {
            // Re-throw exceções de negócio sem wrapping
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar empresa por CNPJ: {} - Erro: {}", cnpj, e.getMessage(), e);
            metricsPort.incrementCounter("company_queries_total",
                Map.of("query_type", "GET_BY_CNPJ", "status", "ERROR"));
            throw new RuntimeException("Erro interno ao buscar empresa por CNPJ", e);
        }
    }

    public PageResultDTO<CompanyViewDTO> search(CompanySearchCriteriaDTO criteria) {
        try {
            PageResultDTO<Company> companies = companyRepository.search(criteria);
            List<CompanyViewDTO> companyViews = companies.items().stream()
                .map(companyMapper::toViewDTO)
                .toList();

            metricsPort.incrementCounter("company_queries_total",
                Map.of("query_type", "SEARCH", "status", "SUCCESS"));

            return new PageResultDTO<>(
                companyViews,
                companies.total(),
                companies.page(),
                companies.size()
            );

        } catch (Exception e) {
            log.error("Erro ao buscar empresas com critérios: {} - Erro: {}", criteria, e.getMessage(), e);
            metricsPort.incrementCounter("company_queries_total",
                Map.of("query_type", "SEARCH", "status", "ERROR"));
            throw new RuntimeException("Erro interno ao buscar empresas", e);
        }
    }


    public CompanyViewDTO getByTenantId(UUID tenantId) {
        try {
            // Tentar buscar no cache primeiro
            CompanyViewDTO cachedCompany = companyCachePort.getCompanyByTenantIdFromCache(tenantId.toString());
            if (cachedCompany != null) {
                metricsPort.incrementCounter("company_queries_total",
                    Map.of("query_type", "GET_BY_X_APPLICATION", "status", "CACHE_HIT"));
                return cachedCompany;
            }

            // Se não encontrou no cache, buscar no banco
            Company company = companyRepository.findByTenantId(tenantId)
                .orElseThrow(() -> {
                    metricsPort.incrementCounter("company_not_found_total",
                        Map.of("entity_id", tenantId.toString(), "operation", "get_by_tenant_id"));
                    return new NotFoundException("Empresa não encontrada: " + tenantId, "COMPANY_NOT_FOUND", Map.of("tenantId", tenantId));
                });

            // Validar se a empresa está ativa
            if (!company.isActive()) {
                metricsPort.incrementCounter("company_invalid_status_total",
                    Map.of("entity_id", tenantId.toString(), "status", company.getStatus().toString(), "operation", "get_by_tenant_id"));
                throw new NotFoundException("Empresa não está ativa: " + tenantId, "COMPANY_NOT_ACTIVE", Map.of("tenantId", tenantId, "status", company.getStatus().getDescription()));
            }

            CompanyViewDTO companyView = companyMapper.toViewDTO(company);

            // Cachear o resultado
            companyCachePort.cacheCompanyByTenantId(tenantId.toString(), companyView);

            metricsPort.incrementCounter("company_queries_total",
                Map.of("query_type", "GET_BY_X_APPLICATION", "status", "SUCCESS"));

            return companyView;

        } catch (NotFoundException e) {
            // Re-throw exceções de negócio sem wrapping
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar empresa por TenantId: {} - Erro: {}", tenantId, e.getMessage(), e);
            metricsPort.incrementCounter("company_queries_total",
                Map.of("query_type", "GET_BY_X_APPLICATION", "status", "ERROR"));
            throw new RuntimeException("Erro interno ao buscar empresa por TenantId", e);
        }
    }

    public CompanySimpleResponseDTO getSimpleByTenantId(UUID tenantId) {
        try {
            // Tentar buscar no cache primeiro
            CompanySimpleResponseDTO cachedCompany = companyCachePort.getSimpleCompanyByTenantIdFromCache(tenantId.toString());
            if (cachedCompany != null) {
                metricsPort.incrementCounter("company_queries_total",
                    Map.of("query_type", "GET_SIMPLE_BY_X_APPLICATION", "status", "CACHE_HIT"));
                return cachedCompany;
            }

            // Se não encontrou no cache, buscar no banco
            Company company = companyRepository.findByTenantId(tenantId)
                .orElseThrow(() -> {
                    metricsPort.incrementCounter("company_not_found_total",
                        Map.of("entity_id", tenantId.toString(), "operation", "get_simple_by_tenant_id"));
                    return new NotFoundException("Empresa não encontrada: " + tenantId, "COMPANY_NOT_FOUND", Map.of("tenantId", tenantId));
                });

            // Validar se a empresa está ativa
            if (!company.isActive()) {
                metricsPort.incrementCounter("company_invalid_status_total",
                    Map.of("entity_id", tenantId.toString(), "status", company.getStatus().toString(), "operation", "get_simple_by_tenant_id"));
                throw new NotFoundException("Empresa não está ativa: " + tenantId, "COMPANY_NOT_ACTIVE", Map.of("tenantId", tenantId, "status", company.getStatus().getDescription()));
            }

            CompanyViewDTO companyView = companyMapper.toViewDTO(company);
            CompanySimpleResponseDTO simpleResponse = companyMapper.toSimpleResponseDTO(companyView);

            // Cachear o resultado
            companyCachePort.cacheSimpleCompanyByTenantId(tenantId.toString(), simpleResponse);

            metricsPort.incrementCounter("company_queries_total",
                Map.of("query_type", "GET_SIMPLE_BY_X_APPLICATION", "status", "SUCCESS"));

            return simpleResponse;

        } catch (NotFoundException e) {
            // Re-throw exceções de negócio sem wrapping
            throw e;
        } catch (Exception e) {
            log.error("Erro ao buscar empresa simples por TenantId: {} - Erro: {}", tenantId, e.getMessage(), e);
            metricsPort.incrementCounter("company_queries_total",
                Map.of("query_type", "GET_SIMPLE_BY_X_APPLICATION", "status", "ERROR"));
            throw new RuntimeException("Erro interno ao buscar empresa simples por TenantId", e);
        }
    }
}