package com.keepguard.ms_company.adapters.in.rest.cnae.dto;

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
public class CnaeUpdateDTO {

    @NotBlank(message = "Código CNAE é obrigatório")
    @Pattern(regexp = "^\\d{7}$", message = "Código CNAE deve conter 7 dígitos")
    private String code;

    @NotBlank(message = "Descrição do CNAE é obrigatória")
    @Size(max = 500, message = "Descrição do CNAE deve ter no máximo 500 caracteres")
    private String description;

    @Size(max = 1, message = "Seção deve ter no máximo 1 caractere")
    private String section;

    @Size(max = 2, message = "Divisão deve ter no máximo 2 caracteres")
    private String division;

    @Size(max = 3, message = "Código do grupo deve ter no máximo 3 caracteres")
    private String groupCode;

    @Size(max = 4, message = "Código da classe deve ter no máximo 4 caracteres")
    private String classCode;

    @Size(max = 5, message = "Código da subclasse deve ter no máximo 5 caracteres")
    private String subclassCode;
}
