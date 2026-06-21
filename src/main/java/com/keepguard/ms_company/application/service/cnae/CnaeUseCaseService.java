package com.keepguard.ms_company.application.service.cnae;

import com.keepguard.ms_company.application.dto.cnae.CnaeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeViewDTO;
import com.keepguard.ms_company.application.port.in.CnaePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CnaeUseCaseService implements CnaePort {

    private final CnaeCommandService commandService;
    private final CnaeQueryService queryService;

    // === OPERAÇÕES DE COMANDO ===

    @Override
    public CnaeViewDTO create(UUID companyId, CnaeCreateCommandDTO command) {
        return commandService.create(companyId, command);
    }

    @Override
    public CnaeViewDTO update(UUID id, CnaeUpdateCommandDTO command) {
        return commandService.update(id, command);
    }

    @Override
    public CnaeViewDTO activate(UUID id) {
        return commandService.activate(id);
    }

    @Override
    public CnaeViewDTO deactivate(UUID id) {
        return commandService.deactivate(id);
    }

    @Override
    public CnaeViewDTO setAsPrincipal(UUID id) {
        return commandService.setAsPrincipal(id);
    }

    @Override
    public void delete(UUID id) {
        commandService.delete(id);
    }

    // === OPERAÇÕES DE CONSULTA ===

    @Override
    public CnaeViewDTO getById(UUID id) {
        return queryService.getById(id);
    }

    @Override
    public List<CnaeViewDTO> listByCompanyId(UUID companyId) {
        return queryService.listByCompanyId(companyId);
    }

    @Override
    public List<CnaeViewDTO> listActiveByCompanyId(UUID companyId) {
        return queryService.listActiveByCompanyId(companyId);
    }

    @Override
    public CnaeViewDTO getPrincipalByCompanyId(UUID companyId) {
        return queryService.getPrincipalByCompanyId(companyId);
    }

    @Override
    public List<CnaeViewDTO> listAll() {
        return queryService.listAll();
    }
}
