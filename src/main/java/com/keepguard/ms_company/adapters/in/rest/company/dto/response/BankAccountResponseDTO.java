package com.keepguard.ms_company.adapters.in.rest.company.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record BankAccountResponseDTO(
    UUID id,
    String bankCode,
    String bankName,
    String agency,
    String account,
    String accountType,
    String accountHolder,
    String accountHolderDocument,
    Boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
