package com.keepguard.ms_company.application.service.cnae;

import com.keepguard.ms_company.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_company.application.dto.cnae.CnaeViewDTO;
import com.keepguard.ms_company.application.mapper.CnaeApplicationMapper;
import com.keepguard.ms_company.domain.entity.Cnae;
import com.keepguard.ms_company.application.port.out.persistence.CnaeRepositoryPort;
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
public class CnaeQueryService {

    private final CnaeRepositoryPort cnaeRepository;
    private final CnaeApplicationMapper cnaeMapper;
    private final MetricsPort metricsPort;

    public CnaeViewDTO getById(UUID id) {
        Cnae cnae = cnaeRepository.findById(id)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("cnae_query_errors_total",
                    Map.of("error_code", "CNAE_NOT_FOUND", "operation", "get_by_id"));
                throw new RuntimeException("CNAE não encontrado: " + id);
            });

        metricsPort.incrementCounter("cnae_queried_total",
            Map.of("entity_id", cnae.getId().toString(), "operation", "get_by_id"));

        return cnaeMapper.toViewDTO(cnae);
    }

    public List<CnaeViewDTO> listByCompanyId(UUID companyId) {
        List<Cnae> cnaes = cnaeRepository.findByCompanyId(companyId);

        metricsPort.incrementCounter("cnae_queried_total",
            Map.of("company_id", companyId.toString(), "operation", "list_by_company"));

        return cnaes.stream()
            .map(cnaeMapper::toViewDTO)
            .toList();
    }

    public CnaeViewDTO getPrincipalByCompanyId(UUID companyId) {
        Cnae cnae = cnaeRepository.findPrincipalByCompanyId(companyId)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("cnae_query_errors_total",
                    Map.of("error_code", "PRINCIPAL_CNAE_NOT_FOUND", "operation", "get_principal"));
                throw new RuntimeException("CNAE principal não encontrado para empresa: " + companyId);
            });

        metricsPort.incrementCounter("cnae_queried_total",
            Map.of("company_id", companyId.toString(), "operation", "get_principal"));

        return cnaeMapper.toViewDTO(cnae);
    }

    public List<CnaeViewDTO> listActiveByCompanyId(UUID companyId) {
        List<Cnae> cnaes = cnaeRepository.findActiveByCompanyId(companyId);

        metricsPort.incrementCounter("cnae_queried_total",
            Map.of("company_id", companyId.toString(), "operation", "list_active_by_company"));

        return cnaes.stream()
            .map(cnaeMapper::toViewDTO)
            .toList();
    }

    public List<CnaeViewDTO> listAll() {
        List<Cnae> cnaes = cnaeRepository.findAll();

        metricsPort.incrementCounter("cnae_queried_total",
            Map.of("operation", "list_all"));

        return cnaes.stream()
            .map(cnaeMapper::toViewDTO)
            .toList();
    }

    public List<CnaeViewDTO> listAllActive() {
        List<Cnae> cnaes = cnaeRepository.findAllActive();

        metricsPort.incrementCounter("cnae_queried_total",
            Map.of("operation", "list_all_active"));

        return cnaes.stream()
            .map(cnaeMapper::toViewDTO)
            .toList();
    }

    public boolean existsById(UUID id) {
        boolean exists = cnaeRepository.existsById(id);

        metricsPort.incrementCounter("cnae_queried_total",
            Map.of("entity_id", id.toString(), "operation", "exists_by_id"));

        return exists;
    }

    public boolean existsByCompanyIdAndCode(UUID companyId, String code) {
        boolean exists = cnaeRepository.existsByCompanyIdAndCode(companyId, code);

        metricsPort.incrementCounter("cnae_queried_total",
            Map.of("company_id", companyId.toString(), "operation", "exists_by_company_and_code"));

        return exists;
    }

    public CnaeViewDTO findByCompanyIdAndCode(UUID companyId, String code) {
        Cnae cnae = cnaeRepository.findByCompanyIdAndCode(companyId, code)
            .orElseThrow(() -> {
                metricsPort.incrementCounter("cnae_query_errors_total",
                    Map.of("error_code", "CNAE_NOT_FOUND", "operation", "find_by_company_and_code"));
                throw new RuntimeException("CNAE não encontrado para empresa " + companyId + " com código " + code);
            });

        metricsPort.incrementCounter("cnae_queried_total",
            Map.of("company_id", companyId.toString(), "operation", "find_by_company_and_code"));

        return cnaeMapper.toViewDTO(cnae);
    }

    public long countActiveByCompanyId(UUID companyId) {
        long count = cnaeRepository.countActiveByCompanyId(companyId);

        metricsPort.incrementCounter("cnae_queried_total",
            Map.of("company_id", companyId.toString(), "operation", "count_active_by_company"));

        return count;
    }
}
