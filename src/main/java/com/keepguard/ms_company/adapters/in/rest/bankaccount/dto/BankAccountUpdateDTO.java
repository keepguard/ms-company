package com.keepguard.ms_company.adapters.in.rest.bankaccount.dto;

import com.keepguard.ms_company.domain.enums.AccountTypeEnum;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountUpdateDTO {

    @Pattern(regexp = "^\\d{3}$", message = "Código do banco deve conter 3 dígitos")
    private String code;

    @Size(max = 10, message = "Agência deve ter no máximo 10 caracteres")
    private String agency;

    @Size(max = 1, message = "Dígito da agência deve ter 1 caractere")
    private String agencyDigit;

    @Size(max = 20, message = "Número da conta deve ter no máximo 20 caracteres")
    private String accountNumber;

    @Size(max = 1, message = "Dígito da conta deve ter 1 caractere")
    private String accountDigit;

    private AccountTypeEnum accountType;
}
