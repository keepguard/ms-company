package com.keepguard.ms_company.application.service.representative;

import com.keepguard.ms_company.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_company.application.dto.representative.RepresentativeViewDTO;
import com.keepguard.ms_company.application.mapper.RepresentativeApplicationMapper;
import com.keepguard.ms_company.application.port.out.persistence.RepresentativeRepositoryPort;
import com.keepguard.ms_company.domain.entity.Representative;
import com.keepguard.ms_company.application.service.exception.NotFoundException;
import com.keepguard.ms_company.application.port.out.cache.RepresentativeCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RepresentativeQueryService {

    private final RepresentativeRepositoryPort representativeRepository;
    private final RepresentativeApplicationMapper representativeMapper;
    private final RepresentativeCachePort representativeCachePort;
    private final MetricsPort metricsPort;

    public RepresentativeViewDTO findById(UUID id) {
        log.info("Buscando representante por ID: {}", id);

        Representative representative = representativeRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Representante não encontrado: {}", id);
                return new NotFoundException("Representante não encontrado");
            });

        // Registra métrica
        metricsPort.incrementCounter("representative.found.by_id", Map.of("entity_id", representative.getId().toString()));

        return representativeMapper.toViewDTO(representative);
    }

    public List<RepresentativeViewDTO> findAll() {
        log.info("Listando todos os representantes");

        List<Representative> representatives = representativeRepository.findAll();

        log.info("Encontrados {} representantes", representatives.size());

        // Registra métrica
        metricsPort.incrementCounter("representative.found.all", Map.of("count", String.valueOf(representatives.size())));

        return representatives.stream()
            .map(representativeMapper::toViewDTO)
            .collect(Collectors.toList());
    }

    public List<RepresentativeViewDTO> findByCompanyId(UUID companyId) {
        log.info("Listando representantes da empresa: {}", companyId);

        // Tentar buscar no cache primeiro
        List<RepresentativeViewDTO> cachedRepresentatives = representativeCachePort.getRepresentativesByCompanyIdFromCache(companyId.toString());
        if (cachedRepresentatives != null) {
            log.info("Representantes encontrados no cache para empresa: {}", companyId);
            metricsPort.incrementCounter("representative.found.by_company", Map.of("company_id", companyId.toString(), "count", String.valueOf(cachedRepresentatives.size()), "status", "CACHE_HIT"));
            return cachedRepresentatives;
        }

        // Se não encontrou no cache, buscar no banco
        List<Representative> representatives = representativeRepository.findByCompanyId(companyId);
        List<RepresentativeViewDTO> views = representatives.stream()
            .map(representativeMapper::toViewDTO)
            .collect(Collectors.toList());

        // Cachear o resultado
        representativeCachePort.cacheRepresentativesByCompanyId(companyId.toString(), views);

        log.info("Encontrados {} representantes para empresa: {}", views.size(), companyId);

        // Registra métrica
        metricsPort.incrementCounter("representative.found.by_company", Map.of("company_id", companyId.toString(), "count", String.valueOf(views.size()), "status", "SUCCESS"));

        return views;
    }

    public RepresentativeViewDTO findActiveByCompanyId(UUID companyId) {
        log.info("Buscando representante ativo da empresa: {}", companyId);

        // Tentar buscar no cache primeiro
        RepresentativeViewDTO cachedRepresentative = representativeCachePort.getActiveRepresentativeByCompanyIdFromCache(companyId.toString());
        if (cachedRepresentative != null) {
            log.info("Representante ativo encontrado no cache para empresa: {}", companyId);
            metricsPort.incrementCounter("representative.found.active_by_company", Map.of("company_id", companyId.toString(), "entity_id", cachedRepresentative.id().toString(), "status", "CACHE_HIT"));
            return cachedRepresentative;
        }

        // Se não encontrou no cache, buscar no banco
        Representative representative = representativeRepository.findActiveByCompanyId(companyId)
            .orElseThrow(() -> {
                log.warn("Representante ativo não encontrado para empresa: {}", companyId);
                return new NotFoundException("Representante ativo não encontrado para esta empresa");
            });

        RepresentativeViewDTO representativeView = representativeMapper.toViewDTO(representative);

        // Cachear o resultado
        representativeCachePort.cacheActiveRepresentativeByCompanyId(companyId.toString(), representativeView);

        // Registra métrica
        metricsPort.incrementCounter("representative.found.active_by_company", Map.of("company_id", companyId.toString(), "entity_id", representativeView.id().toString(), "status", "SUCCESS"));

        return representativeView;
    }

    public List<RepresentativeViewDTO> findAllActive() {
        log.info("Listando todos os representantes ativos");

        List<Representative> representatives = representativeRepository.findAllActive();

        log.info("Encontrados {} representantes ativos", representatives.size());

        // Registra métrica
        metricsPort.incrementCounter("representative.found.all_active", Map.of("count", String.valueOf(representatives.size())));

        return representatives.stream()
            .map(representativeMapper::toViewDTO)
            .collect(Collectors.toList());
    }

    public RepresentativeViewDTO findByCpf(String cpf) {
        log.info("Buscando representante por CPF: {}", cpf);

        Representative representative = representativeRepository.findByCpf(cpf)
            .orElseThrow(() -> {
                log.warn("Representante não encontrado com CPF: {}", cpf);
                return new NotFoundException("Representante não encontrado com este CPF");
            });

        // Registra métrica
        metricsPort.incrementCounter("representative.found.by_cpf", Map.of("entity_id", representative.getId().toString()));

        return representativeMapper.toViewDTO(representative);
    }

    public RepresentativeViewDTO findByEmail(String email) {
        log.info("Buscando representante por email: {}", email);

        Representative representative = representativeRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.warn("Representante não encontrado com email: {}", email);
                return new NotFoundException("Representante não encontrado com este email");
            });

        // Registra métrica
        metricsPort.incrementCounter("representative.found.by_email", Map.of("entity_id", representative.getId().toString()));

        return representativeMapper.toViewDTO(representative);
    }

    public List<RepresentativeViewDTO> findByNameContaining(String name) {
        log.info("Buscando representantes com nome contendo: {}", name);

        List<Representative> representatives = representativeRepository.findByNameContainingIgnoreCase(name);

        log.info("Encontrados {} representantes com nome contendo: {}", representatives.size(), name);

        // Registra métrica
        metricsPort.incrementCounter("representative.found.by_name", Map.of("count", String.valueOf(representatives.size())));

        return representatives.stream()
            .map(representativeMapper::toViewDTO)
            .collect(Collectors.toList());
    }

    public List<RepresentativeViewDTO> findByRoleContaining(String role) {
        log.info("Buscando representantes com cargo contendo: {}", role);

        List<Representative> representatives = representativeRepository.findByRoleContainingIgnoreCase(role);

        log.info("Encontrados {} representantes com cargo contendo: {}", representatives.size(), role);

        // Registra métrica
        metricsPort.incrementCounter("representative.found.by_role", Map.of("count", String.valueOf(representatives.size())));

        return representatives.stream()
            .map(representativeMapper::toViewDTO)
            .collect(Collectors.toList());
    }

    public boolean existsById(UUID id) {
        log.info("Verificando existência de representante: {}", id);

        boolean exists = representativeRepository.existsById(id);

        log.info("Representante {} existe: {}", id, exists);

        return exists;
    }

    public long countActiveByCompanyId(UUID companyId) {
        log.info("Contando representantes ativos da empresa: {}", companyId);

        long count = representativeRepository.countActiveByCompanyId(companyId);

        log.info("Empresa {} tem {} representantes ativos", companyId, count);

        return count;
    }
}
