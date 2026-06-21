package com.keepguard.ms_company.adapters.in.rest.company.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CnaeResponseDTO(
    UUID id,
    String code,
    String description,
    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
