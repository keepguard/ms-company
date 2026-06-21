package com.keepguard.ms_company.adapters.in.rest.company.dto;

import jakarta.validation.constraints.NotBlank;
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
public class AddressDTO {

    @NotBlank(message = "Logradouro é obrigatório")
    @Size(max = 150, message = "Logradouro deve ter no máximo 150 caracteres")
    private String street;

    @NotBlank(message = "Número é obrigatório")
    @Size(max = 20, message = "Número deve ter no máximo 20 caracteres")
    private String number;

    @Size(max = 100, message = "Complemento deve ter no máximo 100 caracteres")
    private String complement;

    @NotBlank(message = "Bairro é obrigatório")
    @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
    private String district;

    @NotBlank(message = "Cidade é obrigatória")
    @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
    private String city;

    @NotBlank(message = "Estado é obrigatório")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Estado deve ser uma sigla de 2 letras maiúsculas")
    private String state;

    @NotBlank(message = "País é obrigatório")
    @Size(max = 100, message = "País deve ter no máximo 100 caracteres")
    private String country;

    @NotBlank(message = "CEP é obrigatório")
    @Pattern(regexp = "^\\d{8}$", message = "CEP deve conter 8 dígitos")
    private String zipCode;
}
