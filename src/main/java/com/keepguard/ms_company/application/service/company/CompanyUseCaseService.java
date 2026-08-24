package com.keepguard.ms_company.application.service.company;

import com.keepguard.ms_company.adapters.in.rest.company.dto.response.CompanySimpleResponseDTO;
import com.keepguard.ms_company.application.dto.company.CompanyCreateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.company.CompanySearchCriteriaDTO;
import com.keepguard.ms_company.application.port.in.CompanyPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyUseCaseService implements CompanyPort {

    private final CompanyCommandService commandService;
    private final CompanyQueryService queryService;

    // === OPERAÇÕES DE COMANDO ===

    @Override
    public CompanyViewDTO create(CompanyCreateCommandDTO command) {
        return commandService.create(command);
    }

    @Override
    public CompanyViewDTO update(UUID id, CompanyUpdateCommandDTO command) {
        return commandService.update(id, command);
    }

    @Override
    public CompanyViewDTO approve(UUID id) {
        return commandService.approve(id);
    }

    @Override
    public CompanyViewDTO reject(UUID id) {
        return commandService.reject(id);
    }

    @Override
    public CompanyViewDTO activate(UUID id) {
        return commandService.activate(id);
    }

    @Override
    public CompanyViewDTO deactivate(UUID id) {
        return commandService.deactivate(id);
    }

    @Override
    public CompanyViewDTO suspend(UUID id) {
        return commandService.suspend(id);
    }

    @Override
    public CompanyViewDTO block(UUID id) {
        return commandService.block(id);
    }

    @Override
    public CompanyViewDTO updateMfaChannels(UUID id, java.util.List<com.keepguard.ms_company.adapters.in.rest.company.dto.request.CompanyMfaChannelRequestDTO> channels) {
        return commandService.updateMfaChannels(id, channels);
    }

    @Override
    public void delete(UUID id) {
        commandService.delete(id);
    }

    // === OPERAÇÕES DE CONSULTA ===

    @Override
    public CompanyViewDTO getById(UUID id) {
        return queryService.getById(id);
    }

    @Override
    public CompanyViewDTO getByCodeCompany(UUID codeCompany) {
        return queryService.getByCodeCompany(codeCompany);
    }

    @Override
    public CompanyViewDTO getByCnpj(String cnpj) {
        return queryService.getByCnpj(cnpj);
    }

    @Override
    public CompanyViewDTO getByTenantId(UUID tenantId) {
        return queryService.getByTenantId(tenantId);
    }

    @Override
    public CompanySimpleResponseDTO getSimpleByTenantId(UUID tenantId) {
        return queryService.getSimpleByTenantId(tenantId);
    }

    @Override
    public PageResultDTO<CompanyViewDTO> search(CompanySearchCriteriaDTO criteria) {
        return queryService.search(criteria);
    }
}
