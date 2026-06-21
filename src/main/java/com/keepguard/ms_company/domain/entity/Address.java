package com.keepguard.ms_company.domain.entity;

import com.keepguard.lib_common.exception.ValidationException;
import com.keepguard.lib_common.utils.BrazilianValidationUtils;

import java.util.Objects;
import java.util.UUID;

public final class Address {

    private final UUID id;
    private final String street;
    private final String number;
    private final String complement;
    private final String district;
    private final String city;
    private final String state;
    private final String country;
    private final String zipCode;
    private boolean active;

    public Address(UUID id, String street, String number, String complement, String district,
                   String city, String state, String country, String zipCode, boolean active) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.street = validateStreet(street);
        this.number = validateNumber(number);
        this.complement = validateComplement(complement);
        this.district = validateDistrict(district);
        this.city = validateCity(city);
        this.state = validateState(state);
        this.country = validateCountry(country);
        this.zipCode = validateZipCode(zipCode);
        this.active = active;
    }

    public static Address create(String street, String number, String complement, String district,
                                String city, String state, String country, String zipCode) {
        return new Address(null, street, number, complement, district, city, state, country, zipCode, true);
    }

    public static Address of(UUID id, String street, String number, String complement, String district,
                            String city, String state, String country, String zipCode, boolean active) {
        return new Address(id, street, number, complement, district, city, state, country, zipCode, active);
    }

    private String validateStreet(String street) {
        if (street == null || street.trim().isEmpty()) {
            throw new IllegalArgumentException("Logradouro é obrigatório");
        }
        if (street.length() > 150) {
            throw new IllegalArgumentException("Logradouro deve ter no máximo 150 caracteres");
        }
        return street.trim();
    }

    private String validateNumber(String number) {
        if (number == null || number.trim().isEmpty()) {
            throw new IllegalArgumentException("Número é obrigatório");
        }
        if (number.length() > 20) {
            throw new IllegalArgumentException("Número deve ter no máximo 20 caracteres");
        }
        return number.trim();
    }

    private String validateComplement(String complement) {
        if (complement == null) {
            return null;
        }
        String trimmed = complement.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > 100) {
            throw new IllegalArgumentException("Complemento deve ter no máximo 100 caracteres");
        }
        return trimmed;
    }

    private String validateDistrict(String district) {
        if (district == null || district.trim().isEmpty()) {
            throw new IllegalArgumentException("Bairro é obrigatório");
        }
        if (district.length() > 100) {
            throw new IllegalArgumentException("Bairro deve ter no máximo 100 caracteres");
        }
        return district.trim();
    }

    private String validateCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("Cidade é obrigatória");
        }
        if (city.length() > 100) {
            throw new IllegalArgumentException("Cidade deve ter no máximo 100 caracteres");
        }
        return city.trim();
    }

    private String validateState(String state) {
        if (state == null || state.trim().isEmpty()) {
            throw new ValidationException("Estado é obrigatório");
        }
        String cleanState = state.trim().toUpperCase();
        BrazilianValidationUtils.validateState(cleanState);
        return cleanState;
    }

    private String validateCountry(String country) {
        if (country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("País é obrigatório");
        }
        if (country.length() > 100) {
            throw new IllegalArgumentException("País deve ter no máximo 100 caracteres");
        }
        return country.trim();
    }

    private String validateZipCode(String zipCode) {
        if (zipCode == null || zipCode.trim().isEmpty()) {
            throw new ValidationException("CEP é obrigatório");
        }
        String cleanZipCode = zipCode.replaceAll("\\D", "");
        BrazilianValidationUtils.validateCep(cleanZipCode);
        return cleanZipCode;
    }

    // Getters
    public UUID getId() { return id; }
    public String getStreet() { return street; }
    public String getNumber() { return number; }
    public String getComplement() { return complement; }
    public String getDistrict() { return district; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getCountry() { return country; }
    public String getZipCode() { return zipCode; }
    public boolean isActive() { return active; }

    // Business methods
    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(id, address.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", street='" + street + '\'' +
                ", number='" + number + '\'' +
                ", complement='" + complement + '\'' +
                ", district='" + district + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", country='" + country + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", active=" + active +
                '}';
    }
}
