package com.keepguard.ms_company.application.dto.address;

public record AddressUpdateCommandDTO(
    String street,
    String number,
    String complement,
    String district,
    String city,
    String state,
    String country,
    String zipCode
) {

    public AddressUpdateCommandDTO {
        if (street != null && street.trim().isEmpty()) {
            throw new IllegalArgumentException("Logradouro não pode ser vazio");
        }
        if (number != null && number.trim().isEmpty()) {
            throw new IllegalArgumentException("Número não pode ser vazio");
        }
        if (district != null && district.trim().isEmpty()) {
            throw new IllegalArgumentException("Bairro não pode ser vazio");
        }
        if (city != null && city.trim().isEmpty()) {
            throw new IllegalArgumentException("Cidade não pode ser vazia");
        }
        if (state != null && state.trim().isEmpty()) {
            throw new IllegalArgumentException("Estado não pode ser vazio");
        }
        if (country != null && country.trim().isEmpty()) {
            throw new IllegalArgumentException("País não pode ser vazio");
        }
        if (zipCode != null && zipCode.trim().isEmpty()) {
            throw new IllegalArgumentException("CEP não pode ser vazio");
        }
    }
}
