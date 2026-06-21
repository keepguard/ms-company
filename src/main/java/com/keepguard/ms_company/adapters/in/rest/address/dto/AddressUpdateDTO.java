package com.keepguard.ms_company.adapters.in.rest.address.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressUpdateDTO {

    @Size(max = 150, message = "Logradouro deve ter no máximo 150 caracteres")
    private String street;

    @Size(max = 20, message = "Número deve ter no máximo 20 caracteres")
    private String number;

    @Size(max = 100, message = "Complemento deve ter no máximo 100 caracteres")
    private String complement;

    @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
    private String district;

    @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
    private String city;

    @Size(min = 2, max = 2, message = "Estado deve ter exatamente 2 caracteres")
    private String state;

    @Size(max = 100, message = "País deve ter no máximo 100 caracteres")
    private String country;

    @Size(min = 8, max = 8, message = "CEP deve ter exatamente 8 dígitos")
    private String zipCode;
}
