package com.keepguard.ms_company.application.dto.address;

import java.util.UUID;

public record AddressViewDTO(
    UUID id,
    UUID companyId,
    String street,
    String number,
    String complement,
    String district,
    String city,
    String state,
    String country,
    String zipCode,
    boolean active
) {

    public AddressViewDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID é obrigatório");
        }
        // companyId pode ser null quando o endereço é retornado como parte de uma Company
        // Validação removida para permitir uso interno no CompanyMapper
        if (street == null || street.trim().isEmpty()) {
            throw new IllegalArgumentException("Logradouro é obrigatório");
        }
        if (number == null || number.trim().isEmpty()) {
            throw new IllegalArgumentException("Número é obrigatório");
        }
        if (district == null || district.trim().isEmpty()) {
            throw new IllegalArgumentException("Bairro é obrigatório");
        }
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("Cidade é obrigatória");
        }
        if (state == null || state.trim().isEmpty()) {
            throw new IllegalArgumentException("Estado é obrigatório");
        }
        if (country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("País é obrigatório");
        }
        if (zipCode == null || zipCode.trim().isEmpty()) {
            throw new IllegalArgumentException("CEP é obrigatório");
        }
    }
}
