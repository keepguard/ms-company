package com.keepguard.ms_company.adapters.in.rest.representative.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados do representante legal")
public class RepresentativeResponseDTO {

    @Schema(description = "ID único do representante", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Nome completo do representante", example = "João Silva Santos")
    private String name;

    @Schema(description = "CPF do representante", example = "12345678901")
    private String cpf;

    @Schema(description = "RG do representante", example = "123456789")
    private String rg;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Data de nascimento do representante", example = "1990-01-15")
    private LocalDate birthDate;

    @Schema(description = "Email do representante", example = "joao.silva@email.com")
    private String email;

    @Schema(description = "Telefone do representante", example = "11987654321")
    private String phone;

    @Schema(description = "Cargo do representante na empresa", example = "Diretor Presidente")
    private String role;

    @Schema(description = "Status ativo do representante", example = "true")
    private Boolean active;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data de criação", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Data de última atualização", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;
}
