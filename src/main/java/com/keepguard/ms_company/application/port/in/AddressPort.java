package com.keepguard.ms_company.application.port.in;

import com.keepguard.ms_company.application.dto.address.AddressCreateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.address.AddressSearchCriteriaDTO;

import java.util.List;
import java.util.UUID;

public interface AddressPort {

    // === OPERAÇÕES DE COMANDO ===

    AddressViewDTO create(UUID companyId, AddressCreateCommandDTO command);

    AddressViewDTO update(UUID id, AddressUpdateCommandDTO command);

    AddressViewDTO activate(UUID id);

    AddressViewDTO deactivate(UUID id);

    void delete(UUID id);

    // === OPERAÇÕES DE CONSULTA ===

    AddressViewDTO getById(UUID id);

    List<AddressViewDTO> listByCompanyId(UUID companyId);

    AddressViewDTO getActiveByCompanyId(UUID companyId);

    List<AddressViewDTO> listAll();

    PageResultDTO<AddressViewDTO> search(AddressSearchCriteriaDTO criteria);
}
