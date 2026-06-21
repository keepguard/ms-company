package com.keepguard.ms_company.adapters.in.rest.company.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ContactResponseDTO(
    UUID id,
    String name,
    String email,
    String phone,
    String position,
    String department,
    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
