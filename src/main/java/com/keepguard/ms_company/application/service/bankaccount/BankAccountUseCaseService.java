package com.keepguard.ms_company.application.service.bankaccount;

import com.keepguard.ms_company.application.dto.bankaccount.BankAccountCreateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountViewDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountSearchCriteriaDTO;
import com.keepguard.ms_company.application.port.in.BankAccountPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankAccountUseCaseService implements BankAccountPort {

    private final BankAccountCommandService commandService;
    private final BankAccountQueryService queryService;

    // === OPERAÇÕES DE COMANDO ===

    @Override
    public BankAccountViewDTO create(UUID companyId, BankAccountCreateCommandDTO command) {
        return commandService.create(companyId, command);
    }

    @Override
    public BankAccountViewDTO update(UUID id, BankAccountUpdateCommandDTO command) {
        return commandService.update(id, command);
    }

    @Override
    public BankAccountViewDTO activate(UUID id) {
        return commandService.activate(id);
    }

    @Override
    public BankAccountViewDTO deactivate(UUID id) {
        return commandService.deactivate(id);
    }

    @Override
    public void delete(UUID id) {
        commandService.delete(id);
    }

    // === OPERAÇÕES DE CONSULTA ===

    @Override
    public BankAccountViewDTO getById(UUID id) {
        return queryService.getById(id);
    }

    @Override
    public List<BankAccountViewDTO> listByCompanyId(UUID companyId) {
        return queryService.listByCompanyId(companyId);
    }

    @Override
    public BankAccountViewDTO getActiveByCompanyId(UUID companyId) {
        return queryService.getActiveByCompanyId(companyId);
    }

    @Override
    public List<BankAccountViewDTO> listAll() {
        return queryService.listAll();
    }

    @Override
    public PageResultDTO<BankAccountViewDTO> search(BankAccountSearchCriteriaDTO criteria) {
        return queryService.search(criteria);
    }
}
