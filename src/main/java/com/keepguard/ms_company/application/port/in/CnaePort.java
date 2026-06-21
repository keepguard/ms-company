package com.keepguard.ms_company.application.port.in;

import com.keepguard.ms_company.application.dto.cnae.CnaeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeViewDTO;

import java.util.List;
import java.util.UUID;

public interface CnaePort {

    // === OPERAÇÕES DE COMANDO ===

    CnaeViewDTO create(UUID companyId, CnaeCreateCommandDTO command);

    CnaeViewDTO update(UUID id, CnaeUpdateCommandDTO command);

    CnaeViewDTO activate(UUID id);

    CnaeViewDTO deactivate(UUID id);

    CnaeViewDTO setAsPrincipal(UUID id);

    void delete(UUID id);

    // === OPERAÇÕES DE CONSULTA ===

    CnaeViewDTO getById(UUID id);

    List<CnaeViewDTO> listByCompanyId(UUID companyId);

    List<CnaeViewDTO> listActiveByCompanyId(UUID companyId);

    CnaeViewDTO getPrincipalByCompanyId(UUID companyId);

    List<CnaeViewDTO> listAll();
}
