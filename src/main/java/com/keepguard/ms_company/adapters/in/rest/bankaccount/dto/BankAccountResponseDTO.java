package com.keepguard.ms_company.adapters.in.rest.bankaccount.dto;

import com.keepguard.ms_company.domain.enums.AccountTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountResponseDTO {

    private UUID id;
    private UUID companyId;
    private String code;
    private String agency;
    private String agencyDigit;
    private String accountNumber;
    private String accountDigit;
    private AccountTypeEnum accountType;
    private boolean active;
}
