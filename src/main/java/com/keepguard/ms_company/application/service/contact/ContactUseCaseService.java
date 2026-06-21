package com.keepguard.ms_company.application.service.contact;

import com.keepguard.ms_company.application.dto.contact.ContactCreateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.contact.ContactSearchCriteriaDTO;
import com.keepguard.ms_company.application.port.in.ContactPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactUseCaseService implements ContactPort {

    private final ContactCommandService commandService;
    private final ContactQueryService queryService;

    // === OPERAÇÕES DE COMANDO ===

    @Override
    public ContactViewDTO create(UUID companyId, ContactCreateCommandDTO command) {
        return commandService.create(companyId, command);
    }

    @Override
    public ContactViewDTO update(UUID id, ContactUpdateCommandDTO command) {
        return commandService.update(id, command);
    }

    @Override
    public ContactViewDTO activate(UUID id) {
        return commandService.activate(id);
    }

    @Override
    public ContactViewDTO deactivate(UUID id) {
        return commandService.deactivate(id);
    }

    @Override
    public void delete(UUID id) {
        commandService.delete(id);
    }

    // === OPERAÇÕES DE CONSULTA ===

    @Override
    public ContactViewDTO getById(UUID id) {
        return queryService.getById(id);
    }

    @Override
    public List<ContactViewDTO> listByCompanyId(UUID companyId) {
        return queryService.listByCompanyId(companyId);
    }

    @Override
    public List<ContactViewDTO> listActiveByCompanyId(UUID companyId) {
        return queryService.listActiveByCompanyId(companyId);
    }

    @Override
    public List<ContactViewDTO> listAll() {
        return queryService.listAll();
    }

    @Override
    public PageResultDTO<ContactViewDTO> search(ContactSearchCriteriaDTO criteria) {
        return queryService.search(criteria);
    }
}
