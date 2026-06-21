package com.keepguard.ms_company.application.service.companypolicy;

import com.keepguard.lib_common.logging.annotation.LogOperation;
import com.keepguard.ms_company.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_company.application.dto.companypolicy.CompanyPolicyViewDTO;
import com.keepguard.ms_company.application.dto.companypolicy.CreateCompanyPolicyCommandDTO;
import com.keepguard.ms_company.application.dto.companypolicy.DeactivateCompanyPolicyCommandDTO;
import com.keepguard.ms_company.application.dto.companypolicy.UpdateCompanyPolicyCommandDTO;
import com.keepguard.ms_company.application.mapper.CompanyPolicyApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.CompanyPolicyRepositoryPort;
import com.keepguard.ms_company.application.service.exception.AlreadyExistsException;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.domain.entity.CompanyPolicy;
import com.keepguard.ms_company.domain.enums.PolicyStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyPolicyCommandService {

    private final CompanyPolicyRepositoryPort repository;
    private final CompanyPolicyApplicationMapper mapper;
    private final MetricsPort metricsPort;

    @LogOperation(
        operation = "CREATE_COMPANY_POLICY",
        description = "Criando nova política da empresa: {code}",
        audit = true,
        auditAction = "CREATE",
        auditEntityType = "COMPANY_POLICY"
    )
    @Transactional
    public CompanyPolicyViewDTO create(CreateCompanyPolicyCommandDTO command) {
        // Verifica se já existe uma política ativa com o mesmo código para a empresa
        if (repository.existsByCompanyIdAndCodeAndStatus(command.companyId(), command.code(), PolicyStatusEnum.ACTIVE)) {
            metricsPort.incrementCounter("company_policy_business_errors_total",
                Map.of("error_code", "POLICY_CODE_ALREADY_EXISTS", "operation", "create"));
            throw new AlreadyExistsException("Já existe uma política ativa com o código: " + command.code());
        }

        // Se a nova política será ativa, desativa todas as outras políticas ativas da empresa
        if (PolicyStatusEnum.ACTIVE.equals(command.status())) {
            deactivateAllActivePolicies(command.companyId(), command.createdBy());
        }

        CompanyPolicy policy = mapper.toDomain(command);
        CompanyPolicy savedPolicy = repository.save(policy);

        metricsPort.incrementCounter("company_policy_created_total",
            Map.of("entity_id", savedPolicy.getId().toString()));

        return mapper.toView(savedPolicy);
    }

    @LogOperation(
        operation = "UPDATE_COMPANY_POLICY",
        description = "Atualizando política da empresa: {id}",
        audit = true,
        auditAction = "UPDATE",
        auditEntityType = "COMPANY_POLICY"
    )
    @Transactional
    public CompanyPolicyViewDTO update(UpdateCompanyPolicyCommandDTO command) {
        CompanyPolicy policy = repository.findById(command.id())
            .orElseThrow(() -> new NotFoundException("Política não encontrada: " + command.id()));

        // Se está ativando a política, desativa todas as outras políticas ativas da empresa
        if (PolicyStatusEnum.ACTIVE.equals(command.status()) && !policy.isActive()) {
            deactivateAllActivePolicies(policy.getCompanyId(), command.updatedBy());
        }

        // Atualiza os campos
        policy.updateDescription(command.description(), command.updatedBy());
        policy.updateStatus(command.status(), command.updatedBy());
        if (command.effectiveTo() != null) {
            policy.updateEffectiveTo(command.effectiveTo(), command.updatedBy());
        }

        // Incrementa a versão
        policy.incrementVersion(command.updatedBy());

        CompanyPolicy savedPolicy = repository.save(policy);

        metricsPort.incrementCounter("company_policy_updated_total",
            Map.of("entity_id", savedPolicy.getId().toString()));

        return mapper.toView(savedPolicy);
    }

    @LogOperation(
        operation = "DEACTIVATE_COMPANY_POLICY",
        description = "Desativando política da empresa: {id}",
        audit = true,
        auditAction = "UPDATE",
        auditEntityType = "COMPANY_POLICY"
    )
    @Transactional
    public CompanyPolicyViewDTO deactivate(DeactivateCompanyPolicyCommandDTO command) {
        CompanyPolicy policy = repository.findById(command.id())
            .orElseThrow(() -> new NotFoundException("Política não encontrada: " + command.id()));

        policy.deactivate(command.updatedBy());
        CompanyPolicy savedPolicy = repository.save(policy);

        metricsPort.incrementCounter("company_policy_deactivated_total",
            Map.of("entity_id", savedPolicy.getId().toString()));

        return mapper.toView(savedPolicy);
    }

    private void deactivateAllActivePolicies(UUID companyId, String updatedBy) {
        repository.findByCompanyIdAndStatus(companyId, PolicyStatusEnum.ACTIVE)
            .forEach(policy -> {
                policy.deactivate(updatedBy);
                repository.save(policy);
            });
    }
}
