package com.keepguard.ms_company.adapters.in.rest.companypolicy.dto;

import com.keepguard.ms_company.domain.enums.PolicyStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompanyPolicyRequest {

    @NotBlank(message = "Código da política é obrigatório")
    @Size(max = 64, message = "Código da política deve ter no máximo 64 caracteres")
    private String code;

    @NotBlank(message = "Descrição da política é obrigatória")
    @Size(max = 255, message = "Descrição da política deve ter no máximo 255 caracteres")
    private String description;

    @NotNull(message = "Status da política é obrigatório")
    private PolicyStatusEnum status;

    @NotNull(message = "Data de início de vigência é obrigatória")
    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;

    @NotBlank(message = "Usuário criador é obrigatório")
    @Size(max = 64, message = "Usuário criador deve ter no máximo 64 caracteres")
    private String createdBy;
}
