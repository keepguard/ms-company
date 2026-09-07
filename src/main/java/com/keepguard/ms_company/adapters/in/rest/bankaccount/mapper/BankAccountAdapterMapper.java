package com.keepguard.ms_company.adapters.in.rest.bankaccount.mapper;

import com.keepguard.ms_company.adapters.in.rest.bankaccount.dto.request.BankAccountCreateRequestDTO;
import com.keepguard.ms_company.adapters.in.rest.bankaccount.dto.response.BankAccountResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.bankaccount.dto.request.BankAccountUpdateRequestDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.BankAccountDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountCreateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountViewDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BankAccountAdapterMapper {

    public BankAccountCreateCommandDTO toCreateCommand(BankAccountCreateRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        try {
            return new BankAccountCreateCommandDTO(
                dto.getCode(),
                dto.getAgency(),
                dto.getAgencyDigit(),
                dto.getAccountNumber(),
                dto.getAccountDigit(),
                dto.getAccountType()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear BankAccountCreateRequestDTO para BankAccountCreateCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public BankAccountUpdateCommandDTO toUpdateCommand(BankAccountUpdateRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        try {
            return new BankAccountUpdateCommandDTO(
                dto.getCode(),
                dto.getAgency(),
                dto.getAgencyDigit(),
                dto.getAccountNumber(),
                dto.getAccountDigit(),
                dto.getAccountType()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear BankAccountUpdateRequestDTO para BankAccountUpdateCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public BankAccountResponseDTO toResponseDTO(BankAccountViewDTO view) {
        if (view == null) {
            return null;
        }

        try {
            return BankAccountResponseDTO.builder()
                .id(view.id())
                .companyId(view.companyId())
                .code(view.code())
                .agency(view.agency())
                .agencyDigit(view.agencyDigit())
                .accountNumber(view.accountNumber())
                .accountDigit(view.accountDigit())
                .accountType(view.accountType())
                .active(view.active())
                .build();
        } catch (Exception e) {
            log.error("Erro ao mapear BankAccountViewDTO para BankAccountResponseDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public BankAccountDTO toCompanyBankAccountDTO(BankAccountViewDTO view) {
        if (view == null) {
            return null;
        }

        try {
            return BankAccountDTO.builder()
                .code(view.code())
                .agency(view.agency())
                .agencyDigit(view.agencyDigit())
                .accountNumber(view.accountNumber())
                .accountDigit(view.accountDigit())
                .accountType(view.accountType())
                .build();
        } catch (Exception e) {
            log.error("Erro ao mapear BankAccountViewDTO para BankAccountDTO: {}", e.getMessage(), e);
            throw e;
        }
    }
}
