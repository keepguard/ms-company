package com.keepguard.ms_company.application.port.in;

import com.keepguard.ms_company.application.dto.contact.ContactCreateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.contact.ContactViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.contact.ContactSearchCriteriaDTO;

import java.util.List;
import java.util.UUID;

public interface ContactPort {

    // === OPERAÇÕES DE COMANDO ===

    ContactViewDTO create(UUID companyId, ContactCreateCommandDTO command);

    ContactViewDTO update(UUID id, ContactUpdateCommandDTO command);

    ContactViewDTO activate(UUID id);

    ContactViewDTO deactivate(UUID id);

    void delete(UUID id);

    // === OPERAÇÕES DE CONSULTA ===

    ContactViewDTO getById(UUID id);

    List<ContactViewDTO> listByCompanyId(UUID companyId);

    List<ContactViewDTO> listActiveByCompanyId(UUID companyId);

    List<ContactViewDTO> listAll();

    PageResultDTO<ContactViewDTO> search(ContactSearchCriteriaDTO criteria);
}
