package com.keepguard.ms_company.adapters.in.rest.company.dto.request;

import com.keepguard.ms_company.domain.enums.TaxRegimeEnum;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUpdateDTO {

    @Size(max = 150, message = "Nome fantasia deve ter no máximo 150 caracteres")
    private String name;

    @Size(max = 200, message = "Razão social deve ter no máximo 200 caracteres")
    private String legalName;

    @Size(max = 20, message = "Inscrição estadual deve ter no máximo 20 caracteres")
    private String stateRegistration;

    @Size(max = 20, message = "Inscrição municipal deve ter no máximo 20 caracteres")
    private String municipalRegistration;

    private TaxRegimeEnum taxRegime;

    @Size(max = 20, message = "EIN deve ter no máximo 20 caracteres")
    private String ein;
}
