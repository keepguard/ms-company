package com.keepguard.ms_company.adapters.in.rest.company.dto.response;

import com.keepguard.ms_company.domain.enums.CompanyStatusEnum;
import com.keepguard.ms_company.domain.enums.TaxRegimeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO de resposta simples para Company")
public class CompanySimpleResponseDTO {

    @Schema(description = "ID único da empresa", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Código único da empresa", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID codeCompany;

    @Schema(description = "Código de aplicação da empresa", example = "123e4567-e89b-12d3-a456-426614174002")
    private UUID tenantId;

    @Schema(description = "Nome da empresa", example = "Empresa Exemplo Ltda")
    private String name;

    @Schema(description = "Razão social da empresa", example = "Empresa Exemplo Ltda")
    private String legalName;

    @Schema(description = "CNPJ da empresa", example = "12345678000195")
    private String cnpj;

    @Schema(description = "Inscrição estadual", example = "123456789")
    private String stateRegistration;

    @Schema(description = "Inscrição municipal", example = "987654321")
    private String municipalRegistration;

    @Schema(description = "Regime tributário", example = "LUCRO_REAL")
    private TaxRegimeEnum taxRegime;

    @Schema(description = "EIN (Employer Identification Number)", example = "123456789")
    private String ein;

    @Schema(description = "Status da empresa", example = "ACTIVE")
    private CompanyStatusEnum status;

    @Schema(description = "Data de criação", example = "2023-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Data de atualização", example = "2023-01-01T10:00:00")
    private LocalDateTime updatedAt;
}
