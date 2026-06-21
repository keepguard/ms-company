package com.keepguard.ms_company.domain.dto;

import java.util.UUID;

public record AddressCreateRequestCommandDTO(
    UUID companyId,
    String street,
    String number,
    String complement,
    String district,
    String city,
    String state,
    String country,
    String zipCode
) {

    public AddressCreateRequestCommandDTO {
        if (companyId == null) {
            throw new IllegalArgumentException("ID da empresa é obrigatório");
        }
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
