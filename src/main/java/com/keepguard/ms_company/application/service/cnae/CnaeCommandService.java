package com.keepguard.ms_company.application.service.cnae;

import com.keepguard.lib_common.logging.annotation.LogOperation;
import com.keepguard.ms_company.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_company.application.dto.cnae.CnaeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeViewDTO;
import com.keepguard.ms_company.application.mapper.CnaeApplicationMapper;
import com.keepguard.ms_company.domain.entity.Cnae;
import com.keepguard.ms_company.application.port.out.persistence.CnaeRepositoryPort;
import com.keepguard.ms_company.application.port.out.persistence.CompanyRepositoryPort;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.domain.entity.Company;
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
@Transactional
public class CnaeCommandService {

    private final CnaeRepositoryPort cnaeRepository;
    private final CompanyRepositoryPort companyRepository;
    private final CnaeApplicationMapper cnaeMapper;
    private final MetricsPort metricsPort;

    @LogOperation(
        operation = "CREATE_CNAE",
        description = "Criando novo CNAE para empresa: {companyId}",
        audit = true,
        auditAction = "CREATE",
        auditEntityType = "CNAE"
    )
    public CnaeViewDTO create(UUID companyId, CnaeCreateCommandDTO command) {
        // Verifica se a empresa existe
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("cnae_business_errors_total",
                    Map.of("error_code", "COMPANY_NOT_FOUND", "operation", "create"));
                return new NotFoundException("Empresa não encontrada: " + companyId);
            });

        // Valida se o status da empresa permite operações
        company.validateStatusForOperations();

        // Verifica se já existe CNAE com o mesmo código para a empresa
        if (cnaeRepository.existsByCompanyIdAndCode(companyId, command.code())) {
            metricsPort.incrementCounter("cnae_business_errors_total",
                Map.of("error_code", "CNAE_ALREADY_EXISTS", "operation", "create"));
            throw new RuntimeException("CNAE já existe para esta empresa: " + command.code());
        }

        // Se for principal, desativa outros principais
        if (command.principal()) {
            List<Cnae> existingPrincipals = cnaeRepository.findByCompanyId(companyId).stream()
                .filter(Cnae::isPrincipal)
                .toList();

            existingPrincipals.forEach(cnae -> {
                cnae.unsetAsPrincipal();
                cnaeRepository.save(cnae);
            });
        }

        Cnae cnae = Cnae.create(
            command.code(),
            command.description(),
            command.section(),
            command.division(),
            command.groupCode(),
            command.classCode(),
            command.subclassCode(),
            command.principal(),
            companyId
        );

        Cnae savedCnae = cnaeRepository.save(cnae);

        // Registra métricas específicas
        metricsPort.incrementCounter("cnae_created_total",
            Map.of("entity_id", savedCnae.getId().toString(), "company_id", companyId.toString()));

        return cnaeMapper.toViewDTO(savedCnae);
    }

    @LogOperation(
        operation = "UPDATE_CNAE",
        description = "Atualizando CNAE: {id}",
        audit = true,
        auditAction = "UPDATE",
        auditEntityType = "CNAE"
    )
    public CnaeViewDTO update(UUID id, CnaeUpdateCommandDTO command) {
        Cnae cnae = cnaeRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("cnae_business_errors_total",
                    Map.of("error_code", "CNAE_NOT_FOUND", "operation", "update"));
                throw new RuntimeException("CNAE não encontrado: " + id);
            });

        // Verifica se a empresa existe e valida seu status
        Company company = companyRepository.findById(cnae.getCompanyId())
            .orElseThrow(() -> new NotFoundException("Empresa não encontrada: " + cnae.getCompanyId()));
        company.validateStatusForOperations();

        // Verifica se o novo código já existe para a empresa
        if (!cnae.getCode().equals(command.code()) &&
            cnaeRepository.existsByCompanyIdAndCode(cnae.getCompanyId(), command.code())) {
            metricsPort.incrementCounter("cnae_business_errors_total",
                Map.of("error_code", "CNAE_CODE_ALREADY_EXISTS", "operation", "update"));
            throw new RuntimeException("Código CNAE já existe para esta empresa: " + command.code());
        }

        cnae.updateCode(command.code());
        cnae.updateDescription(command.description());

        Cnae savedCnae = cnaeRepository.save(cnae);

        metricsPort.incrementCounter("cnae_updated_total",
            Map.of("entity_id", savedCnae.getId().toString()));

        return cnaeMapper.toViewDTO(savedCnae);
    }

    @LogOperation(
        operation = "ACTIVATE_CNAE",
        description = "Ativando CNAE: {id}",
        audit = true,
        auditAction = "UPDATE",
        auditEntityType = "CNAE"
    )
    public CnaeViewDTO activate(UUID id) {
        Cnae cnae = cnaeRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("cnae_business_errors_total",
                    Map.of("error_code", "CNAE_NOT_FOUND", "operation", "activate"));
                throw new RuntimeException("CNAE não encontrado: " + id);
            });

        cnae.activate();
        Cnae savedCnae = cnaeRepository.save(cnae);

        metricsPort.incrementCounter("cnae_activated_total",
            Map.of("entity_id", savedCnae.getId().toString()));

        return cnaeMapper.toViewDTO(savedCnae);
    }

    @LogOperation(
        operation = "DEACTIVATE_CNAE",
        description = "Desativando CNAE: {id}",
        audit = true,
        auditAction = "UPDATE",
        auditEntityType = "CNAE"
    )
    public CnaeViewDTO deactivate(UUID id) {
        Cnae cnae = cnaeRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("cnae_business_errors_total",
                    Map.of("error_code", "CNAE_NOT_FOUND", "operation", "deactivate"));
                throw new RuntimeException("CNAE não encontrado: " + id);
            });

        cnae.deactivate();
        Cnae savedCnae = cnaeRepository.save(cnae);

        metricsPort.incrementCounter("cnae_deactivated_total",
            Map.of("entity_id", savedCnae.getId().toString()));

        return cnaeMapper.toViewDTO(savedCnae);
    }

    @LogOperation(
        operation = "SET_PRINCIPAL_CNAE",
        description = "Definindo CNAE como principal: {id}",
        audit = true,
        auditAction = "UPDATE",
        auditEntityType = "CNAE"
    )
    public CnaeViewDTO setAsPrincipal(UUID id) {
        Cnae cnae = cnaeRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("cnae_business_errors_total",
                    Map.of("error_code", "CNAE_NOT_FOUND", "operation", "set_principal"));
                throw new RuntimeException("CNAE não encontrado: " + id);
            });

        // Desativa outros principais da empresa
        List<Cnae> existingPrincipals = cnaeRepository.findByCompanyId(cnae.getCompanyId()).stream()
            .filter(Cnae::isPrincipal)
            .toList();

        existingPrincipals.forEach(existingPrincipal -> {
            existingPrincipal.unsetAsPrincipal();
            cnaeRepository.save(existingPrincipal);
        });

        cnae.setAsPrincipal();
        Cnae savedCnae = cnaeRepository.save(cnae);

        metricsPort.incrementCounter("cnae_set_principal_total",
            Map.of("entity_id", savedCnae.getId().toString()));

        return cnaeMapper.toViewDTO(savedCnae);
    }

    @LogOperation(
        operation = "DELETE_CNAE",
        description = "Removendo CNAE: {id}",
        audit = true,
        auditAction = "DELETE",
        auditEntityType = "CNAE"
    )
    public void delete(UUID id) {
        Cnae cnae = cnaeRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("cnae_business_errors_total",
                    Map.of("error_code", "CNAE_NOT_FOUND", "operation", "delete"));
                throw new RuntimeException("CNAE não encontrado: " + id);
            });

        // Verifica se é o último CNAE ativo da empresa
        long activeCount = cnaeRepository.countActiveByCompanyId(cnae.getCompanyId());
        if (activeCount <= 1 && cnae.isActive()) {
            metricsPort.incrementCounter("cnae_business_errors_total",
                Map.of("error_code", "CANNOT_DELETE_LAST_ACTIVE_CNAE", "operation", "delete"));
            throw new RuntimeException("Não é possível remover o último CNAE ativo da empresa");
        }

        cnaeRepository.deleteById(id);

        metricsPort.incrementCounter("cnae_deleted_total",
            Map.of("entity_id", cnae.getId().toString()));
    }
}
