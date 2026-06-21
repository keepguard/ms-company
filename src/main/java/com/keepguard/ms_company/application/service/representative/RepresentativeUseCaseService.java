package com.keepguard.ms_company.application.service.representative;

import com.keepguard.ms_company.application.dto.representative.RepresentativeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeViewDTO;
import com.keepguard.ms_company.application.port.in.RepresentativePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepresentativeUseCaseService implements RepresentativePort {

    private final RepresentativeCommandService commandService;
    private final RepresentativeQueryService queryService;

    // === OPERAÇÕES DE COMANDO ===

    @Override
    public RepresentativeViewDTO create(UUID companyId, RepresentativeCreateCommandDTO command) {
        return commandService.create(companyId, command);
    }

    @Override
    public RepresentativeViewDTO update(UUID id, RepresentativeUpdateCommandDTO command) {
        return commandService.update(id, command);
    }

    @Override
    public RepresentativeViewDTO activate(UUID id) {
        return commandService.activate(id);
    }

    @Override
    public RepresentativeViewDTO deactivate(UUID id) {
        return commandService.deactivate(id);
    }

    @Override
    public void delete(UUID id) {
        commandService.delete(id);
    }

    // === OPERAÇÕES DE CONSULTA ===

    @Override
    public RepresentativeViewDTO findById(UUID id) {
        return queryService.findById(id);
    }

    @Override
    public List<RepresentativeViewDTO> findAll() {
        return queryService.findAll();
    }

    @Override
    public List<RepresentativeViewDTO> findByCompanyId(UUID companyId) {
        return queryService.findByCompanyId(companyId);
    }

    @Override
    public RepresentativeViewDTO findActiveByCompanyId(UUID companyId) {
        return queryService.findActiveByCompanyId(companyId);
    }

    @Override
    public List<RepresentativeViewDTO> findAllActive() {
        return queryService.findAllActive();
    }

    @Override
    public RepresentativeViewDTO findByCpf(String cpf) {
        return queryService.findByCpf(cpf);
    }

    @Override
    public RepresentativeViewDTO findByEmail(String email) {
        return queryService.findByEmail(email);
    }
}
