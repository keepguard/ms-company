package com.keepguard.ms_company.application.service.representative;

import com.keepguard.lib_common.logging.annotation.LogOperation;
import com.keepguard.ms_company.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_company.application.dto.representative.RepresentativeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeViewDTO;
import com.keepguard.ms_company.application.mapper.RepresentativeApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.CompanyRepositoryPort;
import com.keepguard.ms_company.application.port.out.persistence.RepresentativeRepositoryPort;
import com.keepguard.ms_company.domain.entity.Representative;
import com.keepguard.ms_company.domain.entity.Company;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
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
public class RepresentativeCommandService {

    private final RepresentativeRepositoryPort representativeRepository;
    private final CompanyRepositoryPort companyRepository;
    private final RepresentativeApplicationMapper representativeMapper;
    private final MetricsPort metricsPort;

    @LogOperation(
        operation = "CREATE_REPRESENTATIVE",
        description = "Criando novo representante para empresa: {companyId}",
        audit = true,
        auditAction = "CREATE",
        auditEntityType = "REPRESENTATIVE"
    )
    public RepresentativeViewDTO create(UUID companyId, RepresentativeCreateCommandDTO command) {
        log.info("Iniciando criação de representante para empresa: {}", companyId);

        // Verifica se a empresa existe
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> {
                log.warn("Empresa não encontrada: {}", companyId);
                return new NotFoundException("Empresa não encontrada");
            });

        // Valida se o status da empresa permite operações
        company.validateStatusForOperations();

        // Verifica se já existe representante ativo com o mesmo CPF
        if (representativeRepository.existsByCompanyIdAndCpf(companyId, command.cpf())) {
            log.warn("Representante com CPF {} já existe para empresa: {}", command.cpf(), companyId);
            throw new IllegalArgumentException("Representante com este CPF já existe para esta empresa");
        }

        // Cria a entidade de domínio
        Representative representative = Representative.create(
            command.name(),
            command.cpf(),
            command.rg(),
            command.birthDate(),
            command.email(),
            command.phone(),
            command.role()
        );

        // Salva no repositório
        Representative savedRepresentative = representativeRepository.save(representative, companyId);

        log.info("Representante criado com sucesso: {}", savedRepresentative.getId());

        // Registra métrica
        metricsPort.incrementCounter("representative.created", Map.of("entity_id", savedRepresentative.getId().toString()));

        return representativeMapper.toViewDTO(savedRepresentative);
    }

    @LogOperation(
        operation = "UPDATE_REPRESENTATIVE",
        description = "Atualizando representante: {id}",
        audit = true,
        auditAction = "UPDATE",
        auditEntityType = "REPRESENTATIVE"
    )
    public RepresentativeViewDTO update(UUID id, RepresentativeUpdateCommandDTO command) {
        log.info("Iniciando atualização de representante: {}", id);

        Representative representative = representativeRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Representante não encontrado: {}", id);
                return new NotFoundException("Representante não encontrado");
            });

        // Busca o companyId do representante
        UUID companyId = representativeRepository.findCompanyIdByRepresentativeId(id)
            .orElseThrow(() -> new NotFoundException("Empresa não encontrada para o representante: " + id));

        // Verifica se a empresa existe e valida seu status
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new NotFoundException("Empresa não encontrada: " + companyId));
        company.validateStatusForOperations();

        // Cria nova instância com dados atualizados
        Representative updatedRepresentative = Representative.of(
            representative.getId(),
            command.name(),
            command.cpf(),
            command.rg(),
            command.birthDate(),
            command.email(),
            command.phone(),
            command.role(),
            representative.isActive()
        );

        // Salva no repositório
        Representative savedRepresentative = representativeRepository.save(updatedRepresentative);

        log.info("Representante atualizado com sucesso: {}", savedRepresentative.getId());

        // Registra métrica
        metricsPort.incrementCounter("representative.updated", Map.of("entity_id", savedRepresentative.getId().toString()));

        return representativeMapper.toViewDTO(savedRepresentative);
    }

    @LogOperation(
        operation = "ACTIVATE_REPRESENTATIVE",
        description = "Ativando representante: {id}",
        audit = true,
        auditAction = "ACTIVATE",
        auditEntityType = "REPRESENTATIVE"
    )
    public RepresentativeViewDTO activate(UUID id) {
        log.info("Ativando representante: {}", id);

        Representative representative = representativeRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Representante não encontrado: {}", id);
                return new NotFoundException("Representante não encontrado");
            });

        representative.activate();
        Representative savedRepresentative = representativeRepository.save(representative);

        log.info("Representante ativado com sucesso: {}", savedRepresentative.getId());

        // Registra métrica
        metricsPort.incrementCounter("representative.activated", Map.of("entity_id", savedRepresentative.getId().toString()));

        return representativeMapper.toViewDTO(savedRepresentative);
    }

    @LogOperation(
        operation = "DEACTIVATE_REPRESENTATIVE",
        description = "Desativando representante: {id}",
        audit = true,
        auditAction = "DEACTIVATE",
        auditEntityType = "REPRESENTATIVE"
    )
    public RepresentativeViewDTO deactivate(UUID id) {
        log.info("Desativando representante: {}", id);

        Representative representative = representativeRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Representante não encontrado: {}", id);
                return new NotFoundException("Representante não encontrado");
            });

        representative.deactivate();
        Representative savedRepresentative = representativeRepository.save(representative);

        log.info("Representante desativado com sucesso: {}", savedRepresentative.getId());

        // Registra métrica
        metricsPort.incrementCounter("representative.deactivated", Map.of("entity_id", savedRepresentative.getId().toString()));

        return representativeMapper.toViewDTO(savedRepresentative);
    }

    @LogOperation(
        operation = "DELETE_REPRESENTATIVE",
        description = "Removendo representante: {id}",
        audit = true,
        auditAction = "DELETE",
        auditEntityType = "REPRESENTATIVE"
    )
    public void delete(UUID id) {
        log.info("Removendo representante: {}", id);

        Representative representative = representativeRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Representante não encontrado: {}", id);
                return new NotFoundException("Representante não encontrado");
            });

        representativeRepository.delete(representative);

        log.info("Representante removido com sucesso: {}", id);

        // Registra métrica
        metricsPort.incrementCounter("representative.deleted", Map.of("entity_id", id.toString()));
    }
}
