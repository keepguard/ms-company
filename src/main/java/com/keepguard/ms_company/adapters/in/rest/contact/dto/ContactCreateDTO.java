package com.keepguard.ms_company.adapters.in.rest.contact.dto;

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
public class ContactCreateDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String name;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "^[0-9\\s\\-\\(\\)\\+]+$", message = "Telefone deve conter apenas números, espaços, hífens, parênteses e sinal de mais")
    @Size(min = 10, max = 20, message = "Telefone deve ter entre 10 e 20 caracteres")
    private String phone;

    @Size(max = 150, message = "Website deve ter no máximo 150 caracteres")
    private String website;

    @Size(max = 100, message = "Cargo deve ter no máximo 100 caracteres")
    private String position;

    @Size(max = 100, message = "Departamento deve ter no máximo 100 caracteres")
    private String department;
}
