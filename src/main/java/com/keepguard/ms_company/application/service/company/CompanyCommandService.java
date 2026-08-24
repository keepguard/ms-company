package com.keepguard.ms_company.application.service.company;

import com.keepguard.lib_common.utils.BrazilianValidationUtils;
import com.keepguard.ms_company.application.dto.company.CompanyCreateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyViewDTO;
import com.keepguard.ms_company.application.mapper.CompanyApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.CompanyRepositoryPort;
import com.keepguard.ms_company.application.port.out.persistence.CompanyPolicyRepositoryPort;
import com.keepguard.ms_company.application.service.exception.AlreadyExistsException;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.domain.entity.Company;
import com.keepguard.ms_company.application.port.out.cache.CompanyCachePort;
import com.keepguard.lib_common.exception.ValidationException;
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
public class CompanyCommandService {

    private final CompanyRepositoryPort companyRepository;
    private final CompanyPolicyRepositoryPort companyPolicyRepository;
    private final CompanyApplicationMapper companyMapper;
    private final CompanyCachePort companyCachePort;
    private final MetricsPort metricsPort;

    @LogOperation(
        operation = "CREATE_COMPANY",
        description = "Criando nova empresa: {name}",
        audit = true,
        auditAction = "CREATE",
        auditEntityType = "COMPANY"
    )
    public CompanyViewDTO create(CompanyCreateCommandDTO command) {
        // Validação prévia do CNPJ (fora da transação)
        try {
            BrazilianValidationUtils.validateCnpj(command.cnpj());
        } catch (com.keepguard.lib_common.exception.ValidationException e) {
            metricsPort.incrementCounter("company_business_errors_total",
                Map.of("error_code", "INVALID_CNPJ", "operation", "create"));
            throw e; // Re-lança a ValidationException para ser capturada pelo handler
        }

        // Verifica se CNPJ já existe
        if (companyRepository.existsByCnpj(command.cnpj())) {
            metricsPort.incrementCounter("company_business_errors_total",
                Map.of("error_code", "CNPJ_ALREADY_EXISTS", "operation", "create"));
            throw new AlreadyExistsException("CNPJ já cadastrado: " + command.cnpj());
        }

        Company company = companyMapper.toDomain(command);
        Company savedCompany = saveCompany(company);

        // Registra métricas específicas (não cobertas pelo @LogOperation)
        metricsPort.incrementCounter("company_created_total",
            Map.of("entity_id", savedCompany.getId().toString()));

        return companyMapper.toViewDTO(savedCompany);
    }

    @Transactional
    private Company saveCompany(Company company) {
        return companyRepository.save(company);
    }

    @LogOperation(
        operation = "UPDATE_COMPANY",
        description = "Atualizando empresa: {id}",
        audit = true,
        auditAction = "UPDATE",
        auditEntityType = "COMPANY"
    )
    public CompanyViewDTO update(UUID id, CompanyUpdateCommandDTO command) {
        Company existingCompany = companyRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("company_not_found_total",
                    Map.of("entity_id", id.toString(), "operation", "update"));
                return new NotFoundException("Empresa não encontrada: " + id);
            });

        // Valida se o status permite operações de edição
        existingCompany.validateStatusForOperations();

        Company updatedCompany = companyMapper.toDomain(command, existingCompany);
        Company savedCompany = companyRepository.save(updatedCompany);

        // Limpar cache relacionado à empresa
        companyCachePort.clearAllCompanyCache(
            savedCompany.getId().toString(),
            savedCompany.getCnpj(),
            savedCompany.getCodeCompany().toString(),
            savedCompany.getTenantId().toString()
        );

        // Registra métricas específicas (não cobertas pelo @LogOperation)
        metricsPort.incrementCounter("company_updated_total",
            Map.of("entity_id", id.toString()));

        return companyMapper.toViewDTO(savedCompany);
    }

    @LogOperation(
        operation = "APPROVE_COMPANY",
        description = "Aprovando empresa: {id}",
        audit = true,
        auditAction = "APPROVE",
        auditEntityType = "COMPANY"
    )
    public CompanyViewDTO approve(UUID id) {
        Company company = companyRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("company_not_found_total",
                    Map.of("entity_id", id.toString(), "operation", "approve"));
                return new NotFoundException("Empresa não encontrada: " + id);
            });

        // Validação adicional: deve ter pelo menos uma política ativa
        if (!companyPolicyRepository.existsActivePolicyByCompanyId(id)) {
            metricsPort.incrementCounter("company_business_errors_total",
                Map.of("error_code", "NO_ACTIVE_POLICY", "operation", "approve"));
            throw new ValidationException("Empresa deve ter pelo menos uma política ativa para ser aprovada");
        }

        company.approve();
        Company updatedCompany = companyRepository.save(company);

        // Limpar cache relacionado à empresa
        companyCachePort.clearAllCompanyCache(
            updatedCompany.getId().toString(),
            updatedCompany.getCnpj(),
            updatedCompany.getCodeCompany().toString(),
            updatedCompany.getTenantId().toString()
        );

        // Registra métricas específicas (não cobertas pelo @LogOperation)
        metricsPort.incrementCounter("company_approved_total",
            Map.of("entity_id", id.toString()));

        return companyMapper.toViewDTO(updatedCompany);
    }

    @LogOperation(
        operation = "REJECT_COMPANY",
        description = "Rejeitando empresa: {id}",
        audit = true,
        auditAction = "REJECT",
        auditEntityType = "COMPANY"
    )
    public CompanyViewDTO reject(UUID id) {
        Company company = companyRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("company_not_found_total",
                    Map.of("entity_id", id.toString(), "operation", "reject"));
                return new NotFoundException("Empresa não encontrada: " + id);
            });

        company.reject();
        Company updatedCompany = companyRepository.save(company);

        // Limpar cache relacionado à empresa
        companyCachePort.clearAllCompanyCache(
            updatedCompany.getId().toString(),
            updatedCompany.getCnpj(),
            updatedCompany.getCodeCompany().toString(),
            updatedCompany.getTenantId().toString()
        );

        // Registra métricas específicas (não cobertas pelo @LogOperation)
        metricsPort.incrementCounter("company_rejected_total",
            Map.of("entity_id", id.toString()));

        return companyMapper.toViewDTO(updatedCompany);
    }

    @LogOperation(
        operation = "ACTIVATE_COMPANY",
        description = "Ativando empresa: {id}",
        audit = true,
        auditAction = "ACTIVATE",
        auditEntityType = "COMPANY"
    )
    public CompanyViewDTO activate(UUID id) {
        Company company = companyRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("company_not_found_total",
                    Map.of("entity_id", id.toString(), "operation", "activate"));
                return new NotFoundException("Empresa não encontrada: " + id);
            });

        company.activate();
        Company updatedCompany = companyRepository.save(company);

        // Limpar cache relacionado à empresa
        companyCachePort.clearAllCompanyCache(
            updatedCompany.getId().toString(),
            updatedCompany.getCnpj(),
            updatedCompany.getCodeCompany().toString(),
            updatedCompany.getTenantId().toString()
        );

        // Registra métricas específicas (não cobertas pelo @LogOperation)
        metricsPort.incrementCounter("company_activated_total",
            Map.of("entity_id", id.toString()));

        return companyMapper.toViewDTO(updatedCompany);
    }

    @LogOperation(
        operation = "DEACTIVATE_COMPANY",
        description = "Desativando empresa: {id}",
        audit = true,
        auditAction = "DEACTIVATE",
        auditEntityType = "COMPANY"
    )
    public CompanyViewDTO deactivate(UUID id) {
        Company company = companyRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("company_not_found_total",
                    Map.of("entity_id", id.toString(), "operation", "deactivate"));
                return new NotFoundException("Empresa não encontrada: " + id);
            });

        company.deactivate();
        Company updatedCompany = companyRepository.save(company);

        // Limpar cache relacionado à empresa
        companyCachePort.clearAllCompanyCache(
            updatedCompany.getId().toString(),
            updatedCompany.getCnpj(),
            updatedCompany.getCodeCompany().toString(),
            updatedCompany.getTenantId().toString()
        );

        // Registra métricas específicas (não cobertas pelo @LogOperation)
        metricsPort.incrementCounter("company_deactivated_total",
            Map.of("entity_id", id.toString()));

        return companyMapper.toViewDTO(updatedCompany);
    }

    @LogOperation(
        operation = "UPDATE_MFA_CHANNELS",
        description = "Atualizando canais de MFA da empresa: {id}",
        audit = true,
        auditAction = "UPDATE",
        auditEntityType = "COMPANY"
    )
    public CompanyViewDTO updateMfaChannels(UUID id, java.util.List<com.keepguard.ms_company.adapters.in.rest.company.dto.request.CompanyMfaChannelRequestDTO> channels) {
        Company company = companyRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Empresa não encontrada: " + id));

        java.util.List<com.keepguard.ms_company.domain.entity.CompanyMfaChannel> domainChannels = channels != null ?
            channels.stream()
                .map(ch -> com.keepguard.ms_company.domain.entity.CompanyMfaChannel.create(ch.channel(), ch.required(), ch.enabled()))
                .toList() : new java.util.ArrayList<>();

        company.setMfaChannels(domainChannels);
        Company savedCompany = companyRepository.save(company);

        // Limpar e atualizar cache
        companyCachePort.clearAllCompanyCache(
            savedCompany.getId().toString(),
            savedCompany.getCnpj(),
            savedCompany.getCodeCompany().toString(),
            savedCompany.getTenantId().toString()
        );

        return companyMapper.toViewDTO(savedCompany);
    }

    @LogOperation(
        operation = "SUSPEND_COMPANY",
        description = "Suspendo empresa: {id}",
        audit = true,
        auditAction = "SUSPEND",
        auditEntityType = "COMPANY"
    )
    public CompanyViewDTO suspend(UUID id) {
        Company company = companyRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("company_not_found_total",
                    Map.of("entity_id", id.toString(), "operation", "suspend"));
                return new NotFoundException("Empresa não encontrada: " + id);
            });

        company.suspend();
        Company updatedCompany = companyRepository.save(company);

        // Limpar cache relacionado à empresa
        companyCachePort.clearAllCompanyCache(
            updatedCompany.getId().toString(),
            updatedCompany.getCnpj(),
            updatedCompany.getCodeCompany().toString(),
            updatedCompany.getTenantId().toString()
        );

        // Registra métricas específicas (não cobertas pelo @LogOperation)
        metricsPort.incrementCounter("company_suspended_total",
            Map.of("entity_id", id.toString()));

        return companyMapper.toViewDTO(updatedCompany);
    }

    @LogOperation(
        operation = "BLOCK_COMPANY",
        description = "Bloqueando empresa: {id}",
        audit = true,
        auditAction = "BLOCK",
        auditEntityType = "COMPANY"
    )
    public CompanyViewDTO block(UUID id) {
        Company company = companyRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("company_not_found_total",
                    Map.of("entity_id", id.toString(), "operation", "block"));
                return new NotFoundException("Empresa não encontrada: " + id);
            });

        company.block();
        Company updatedCompany = companyRepository.save(company);

        // Limpar cache relacionado à empresa
        companyCachePort.clearAllCompanyCache(
            updatedCompany.getId().toString(),
            updatedCompany.getCnpj(),
            updatedCompany.getCodeCompany().toString(),
            updatedCompany.getTenantId().toString()
        );

        // Registra métricas específicas (não cobertas pelo @LogOperation)
        metricsPort.incrementCounter("company_blocked_total",
            Map.of("entity_id", id.toString()));

        return companyMapper.toViewDTO(updatedCompany);
    }

    @LogOperation(
        operation = "DELETE_COMPANY",
        description = "Removendo empresa: {id}",
        audit = true,
        auditAction = "DELETE",
        auditEntityType = "COMPANY"
    )
    public void delete(UUID id) {
        if (!companyRepository.findById(id).isPresent()) {
            metricsPort.incrementCounter("company_not_found_total",
                Map.of("entity_id", id.toString(), "operation", "delete"));
            throw new NotFoundException("Empresa não encontrada: " + id);
        }

        companyRepository.deleteById(id);

        // Limpar cache relacionado à empresa
        companyCachePort.clearAllCompanyCacheById(id.toString());

        // Registra métricas específicas (não cobertas pelo @LogOperation)
        metricsPort.incrementCounter("company_deleted_total",
            Map.of("entity_id", id.toString()));
    }

}