package com.keepguard.ms_company.adapters.in.rest.company.dto;

import com.keepguard.ms_company.domain.enums.AccountTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class BankAccountDTO {

    @NotBlank(message = "Código do banco é obrigatório")
    @Pattern(regexp = "^\\d{3}$", message = "Código do banco deve conter 3 dígitos")
    private String code;

    @NotBlank(message = "Agência é obrigatória")
    @Size(max = 10, message = "Agência deve ter no máximo 10 caracteres")
    private String agency;

    @Size(max = 1, message = "Dígito da agência deve ter 1 caractere")
    private String agencyDigit;

    @NotBlank(message = "Número da conta é obrigatório")
    @Size(max = 20, message = "Número da conta deve ter no máximo 20 caracteres")
    private String accountNumber;

    @NotBlank(message = "Dígito da conta é obrigatório")
    @Size(max = 1, message = "Dígito da conta deve ter 1 caractere")
    private String accountDigit;

    @NotNull(message = "Tipo da conta é obrigatório")
    private AccountTypeEnum accountType;
}
