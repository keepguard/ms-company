package com.keepguard.ms_company.application.service.address;

import com.keepguard.ms_company.application.dto.address.AddressCreateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.address.AddressSearchCriteriaDTO;
import com.keepguard.ms_company.application.port.in.AddressPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressUseCaseService implements AddressPort {

    private final AddressCommandService commandService;
    private final AddressQueryService queryService;

    // === OPERAÇÕES DE COMANDO ===

    @Override
    public AddressViewDTO create(UUID companyId, AddressCreateCommandDTO command) {
        return commandService.create(companyId, command);
    }

    @Override
    public AddressViewDTO update(UUID id, AddressUpdateCommandDTO command) {
        return commandService.update(id, command);
    }

    @Override
    public AddressViewDTO activate(UUID id) {
        return commandService.activate(id);
    }

    @Override
    public AddressViewDTO deactivate(UUID id) {
        return commandService.deactivate(id);
    }

    @Override
    public void delete(UUID id) {
        commandService.delete(id);
    }

    // === OPERAÇÕES DE CONSULTA ===

    @Override
    public AddressViewDTO getById(UUID id) {
        return queryService.getById(id);
    }

    @Override
    public List<AddressViewDTO> listByCompanyId(UUID companyId) {
        return queryService.listByCompanyId(companyId);
    }

    @Override
    public AddressViewDTO getActiveByCompanyId(UUID companyId) {
        return queryService.getActiveByCompanyId(companyId);
    }

    @Override
    public List<AddressViewDTO> listAll() {
        return queryService.listAll();
    }

    @Override
    public PageResultDTO<AddressViewDTO> search(AddressSearchCriteriaDTO criteria) {
        return queryService.search(criteria);
    }
}
