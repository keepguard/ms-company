package com.keepguard.ms_company.adapters.in.rest.representative.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para atualização de representante legal")
public class RepresentativeUpdateDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    @Schema(description = "Nome completo do representante", example = "João Silva Santos", required = true)
    private String name;

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "^\\d{11}$", message = "CPF deve conter 11 dígitos")
    @Schema(description = "CPF do representante (apenas números)", example = "12345678901", required = true)
    private String cpf;

    @Size(max = 15, message = "RG deve ter no máximo 15 caracteres")
    @Schema(description = "RG do representante", example = "123456789")
    private String rg;

    @NotNull(message = "Data de nascimento é obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de nascimento do representante", example = "1990-01-15", required = true)
    private LocalDate birthDate;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
    @Schema(description = "Email do representante", example = "joao.silva@email.com", required = true)
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "^\\d{10,15}$", message = "Telefone deve ter entre 10 e 15 dígitos")
    @Schema(description = "Telefone do representante (apenas números)", example = "11987654321", required = true)
    private String phone;

    @Size(max = 100, message = "Cargo deve ter no máximo 100 caracteres")
    @Schema(description = "Cargo do representante na empresa", example = "Diretor Presidente")
    private String role;
}
