package com.keepguard.ms_company.adapters.in.rest.company.dto.request;

import com.keepguard.ms_company.domain.enums.TaxRegimeEnum;
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
public class CompanyCreateDTO {

    @NotBlank(message = "Nome fantasia é obrigatório")
    @Size(max = 150, message = "Nome fantasia deve ter no máximo 150 caracteres")
    private String name;

    @NotBlank(message = "Razão social é obrigatória")
    @Size(max = 200, message = "Razão social deve ter no máximo 200 caracteres")
    private String legalName;

    @NotBlank(message = "CNPJ é obrigatório")
    @Pattern(regexp = "^\\d{14}$", message = "CNPJ deve conter 14 dígitos")
    private String cnpj;

    @Size(max = 20, message = "Inscrição estadual deve ter no máximo 20 caracteres")
    private String stateRegistration;

    @Size(max = 20, message = "Inscrição municipal deve ter no máximo 20 caracteres")
    private String municipalRegistration;

    @NotNull(message = "Regime tributário é obrigatório")
    private TaxRegimeEnum taxRegime;

    @Size(max = 20, message = "EIN deve ter no máximo 20 caracteres")
    private String ein;
}
