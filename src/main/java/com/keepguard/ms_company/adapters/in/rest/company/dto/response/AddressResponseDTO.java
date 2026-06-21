package com.keepguard.ms_company.adapters.in.rest.company.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AddressResponseDTO(
    UUID id,
    String street,
    String number,
    String complement,
    String neighborhood,
    String city,
    String state,
    String zipCode,
    String country,
    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
