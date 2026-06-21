package com.keepguard.ms_company.adapters.in.rest.company.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CompanyDetailsResponseDTO(
    UUID id,
    String name,
    String cnpj,
    String tradeName,
    String taxRegime,
    String status,
    String description,
    List<AddressResponseDTO> addresses,
    List<ContactResponseDTO> contacts,
    List<RepresentativeResponseDTO> representatives,
    List<BankAccountResponseDTO> bankAccounts,
    List<CnaeResponseDTO> cnaes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
