package com.keepguard.ms_company.adapters.in.rest.company.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepresentativeDTO {

    @NotBlank(message = "Nome do representante é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    private String name;

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "^\\d{11}$", message = "CPF deve conter 11 dígitos")
    private String cpf;

    @Size(max = 15, message = "RG deve ter no máximo 15 caracteres")
    private String rg;

    @NotNull(message = "Data de nascimento é obrigatória")
    private LocalDate birthDate;

    @NotBlank(message = "Email do representante é obrigatório")
    @Email(message = "Email deve ter formato válido")
    @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
    private String email;

    @NotBlank(message = "Telefone do representante é obrigatório")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Telefone deve ter formato válido")
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String phone;

    @Size(max = 100, message = "Cargo deve ter no máximo 100 caracteres")
    private String role;
}
