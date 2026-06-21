package com.keepguard.ms_company.adapters.in.rest.company.dto;

import jakarta.validation.constraints.Email;
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
public class ContactDTO {

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Telefone deve ter formato válido")
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String phone;

    @Size(max = 150, message = "Website deve ter no máximo 150 caracteres")
    private String website;
}
