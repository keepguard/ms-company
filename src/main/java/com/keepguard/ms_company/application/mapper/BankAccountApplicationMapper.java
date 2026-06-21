package com.keepguard.ms_company.application.mapper;

import com.keepguard.ms_company.adapters.in.rest.bankaccount.dto.BankAccountCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.bankaccount.dto.BankAccountResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.bankaccount.dto.BankAccountUpdateDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountCreateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountViewDTO;
import com.keepguard.ms_company.domain.entity.BankAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class BankAccountApplicationMapper {

    public BankAccount toDomain(BankAccountCreateCommandDTO command) {
        if (command == null) {
            return null;
        }

        try {
            return BankAccount.create(
                command.code(),
                command.agency(),
                command.agencyDigit(),
                command.accountNumber(),
                command.accountDigit(),
                command.accountType()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear BankAccountCreateCommandDTO para BankAccount: {}", e.getMessage(), e);
            throw e;
        }
    }

    public BankAccount toDomain(BankAccountUpdateCommandDTO command, BankAccount existingBankAccount) {
        if (command == null || existingBankAccount == null) {
            return null;
        }

        try {
            // Cria um novo dados bancários com os dados atualizados
            return BankAccount.of(
                existingBankAccount.getId(),
                command.code() != null ? command.code() : existingBankAccount.getCode(),
                command.agency() != null ? command.agency() : existingBankAccount.getAgency(),
                command.agencyDigit() != null ? command.agencyDigit() : existingBankAccount.getAgencyDigit(),
                command.accountNumber() != null ? command.accountNumber() : existingBankAccount.getAccountNumber(),
                command.accountDigit() != null ? command.accountDigit() : existingBankAccount.getAccountDigit(),
                command.accountType() != null ? command.accountType() : existingBankAccount.getAccountType(),
                existingBankAccount.isActive()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear BankAccountUpdateCommandDTO para BankAccount: {}", e.getMessage(), e);
            throw e;
        }
    }

    public BankAccountViewDTO toViewDTO(BankAccount bankAccount) {
        if (bankAccount == null) {
            return null;
        }

        try {
            return new BankAccountViewDTO(
                bankAccount.getId(),
                null, // companyId - será definido pelo caller quando necessário
                bankAccount.getCode(),
                bankAccount.getAgency(),
                bankAccount.getAgencyDigit(),
                bankAccount.getAccountNumber(),
                bankAccount.getAccountDigit(),
                bankAccount.getAccountType(),
                bankAccount.isActive()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear BankAccount para BankAccountViewDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public BankAccountViewDTO toViewDTO(BankAccount bankAccount, UUID companyId) {
        if (bankAccount == null) {
            return null;
        }

        try {
            return new BankAccountViewDTO(
                bankAccount.getId(),
                companyId,
                bankAccount.getCode(),
                bankAccount.getAgency(),
                bankAccount.getAgencyDigit(),
                bankAccount.getAccountNumber(),
                bankAccount.getAccountDigit(),
                bankAccount.getAccountType(),
                bankAccount.isActive()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear BankAccount para BankAccountViewDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public BankAccountCreateCommandDTO toCreateCommand(BankAccountCreateDTO dto) {
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
            log.error("Erro ao mapear BankAccountCreateDTO para BankAccountCreateCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public BankAccountUpdateCommandDTO toUpdateCommand(BankAccountUpdateDTO dto) {
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
            log.error("Erro ao mapear BankAccountUpdateDTO para BankAccountUpdateCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public BankAccountResponseDTO toResponseDTO(BankAccountViewDTO viewDTO) {
        if (viewDTO == null) {
            return null;
        }

        try {
            return BankAccountResponseDTO.builder()
                .id(viewDTO.id())
                .companyId(viewDTO.companyId())
                .code(viewDTO.code())
                .agency(viewDTO.agency())
                .agencyDigit(viewDTO.agencyDigit())
                .accountNumber(viewDTO.accountNumber())
                .accountDigit(viewDTO.accountDigit())
                .accountType(viewDTO.accountType())
                .active(viewDTO.active())
                .build();
        } catch (Exception e) {
            log.error("Erro ao mapear BankAccountViewDTO para BankAccountResponseDTO: {}", e.getMessage(), e);
            throw e;
        }
    }
}
