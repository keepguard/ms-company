package com.keepguard.ms_company.application.port.in;

import com.keepguard.ms_company.application.dto.bankaccount.BankAccountCreateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountSearchCriteriaDTO;

import java.util.List;
import java.util.UUID;

public interface BankAccountPort {

    // === OPERAÇÕES DE COMANDO ===

    BankAccountViewDTO create(UUID companyId, BankAccountCreateCommandDTO command);

    BankAccountViewDTO update(UUID id, BankAccountUpdateCommandDTO command);

    BankAccountViewDTO activate(UUID id);

    BankAccountViewDTO deactivate(UUID id);

    void delete(UUID id);

    // === OPERAÇÕES DE CONSULTA ===

    BankAccountViewDTO getById(UUID id);

    List<BankAccountViewDTO> listByCompanyId(UUID companyId);

    BankAccountViewDTO getActiveByCompanyId(UUID companyId);

    List<BankAccountViewDTO> listAll();

    PageResultDTO<BankAccountViewDTO> search(BankAccountSearchCriteriaDTO criteria);
}
