package com.keepguard.ms_company.adapters.in.rest.company.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record RepresentativeResponseDTO(
    UUID id,
    String name,
    String email,
    String phone,
    String position,
    String document,
    String documentType,
    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
