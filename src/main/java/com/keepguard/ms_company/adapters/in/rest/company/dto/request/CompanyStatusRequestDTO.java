package com.keepguard.ms_company.adapters.in.rest.company.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CompanyStatusRequestDTO(
    @NotNull(message = "ID da empresa é obrigatório")
    UUID id,
    
    @NotBlank(message = "Status é obrigatório")
    String status
) {
}
