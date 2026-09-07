package com.keepguard.ms_company.application.port.in;

import com.keepguard.ms_company.application.dto.company.CompanyCreateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyMfaChannelCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanySimpleViewDTO;
import com.keepguard.ms_company.application.dto.company.CompanyUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.company.CompanySearchCriteriaDTO;

import java.util.List;
import java.util.UUID;

public interface CompanyPort {

    // === OPERAÇÕES DE COMANDO ===

    CompanyViewDTO create(CompanyCreateCommandDTO command);

    CompanyViewDTO update(UUID id, CompanyUpdateCommandDTO command);

    CompanyViewDTO approve(UUID id);

    CompanyViewDTO reject(UUID id);

    CompanyViewDTO activate(UUID id);

    CompanyViewDTO deactivate(UUID id);

    CompanyViewDTO suspend(UUID id);

    CompanyViewDTO block(UUID id);

    CompanyViewDTO updateMfaChannels(UUID id, List<CompanyMfaChannelCommandDTO> channels);

    void delete(UUID id);

    // === OPERAÇÕES DE CONSULTA ===

    CompanyViewDTO getById(UUID id);

    CompanyViewDTO getByCodeCompany(UUID codeCompany);

    CompanyViewDTO getByCnpj(String cnpj);

    CompanyViewDTO getByTenantId(UUID tenantId);

    CompanySimpleViewDTO getSimpleByTenantId(UUID tenantId);

    PageResultDTO<CompanyViewDTO> search(CompanySearchCriteriaDTO criteria);
}
