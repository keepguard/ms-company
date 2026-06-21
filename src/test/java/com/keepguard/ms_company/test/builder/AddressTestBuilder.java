package com.keepguard.ms_company.test.builder;

import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.address.dto.AddressUpdateDTO;
import com.keepguard.ms_company.application.dto.address.AddressCreateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.address.AddressViewDTO;
import com.keepguard.ms_company.domain.entity.Address;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyAddressJpaEntity;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyJpaEntity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Builder para criação de dados de teste para Address
 * Facilita a criação de objetos de teste com dados padrão
 */
public class AddressTestBuilder {
    
    private UUID id = UUID.randomUUID();
    private UUID companyId = UUID.randomUUID();
    private String street = "Rua das Flores";
    private String number = "123";
    private String complement = "Sala 1";
    private String district = "Centro";
    private String city = "São Paulo";
    private String state = "SP";
    private String country = "Brasil";
    private String zipCode = "01234567";
    private boolean active = true;
    
    public static AddressTestBuilder builder() {
        return new AddressTestBuilder();
    }
    
    public AddressTestBuilder withId(UUID id) {
        this.id = id;
        return this;
    }
    
    public AddressTestBuilder withCompanyId(UUID companyId) {
        this.companyId = companyId;
        return this;
    }
    
    public AddressTestBuilder withStreet(String street) {
        this.street = street;
        return this;
    }
    
    public AddressTestBuilder withNumber(String number) {
        this.number = number;
        return this;
    }
    
    public AddressTestBuilder withComplement(String complement) {
        this.complement = complement;
        return this;
    }
    
    public AddressTestBuilder withDistrict(String district) {
        this.district = district;
        return this;
    }
    
    public AddressTestBuilder withCity(String city) {
        this.city = city;
        return this;
    }
    
    public AddressTestBuilder withState(String state) {
        this.state = state;
        return this;
    }
    
    public AddressTestBuilder withCountry(String country) {
        this.country = country;
        return this;
    }
    
    public AddressTestBuilder withZipCode(String zipCode) {
        this.zipCode = zipCode;
        return this;
    }
    
    public AddressTestBuilder withActive(boolean active) {
        this.active = active;
        return this;
    }
    
    public AddressTestBuilder inactive() {
        this.active = false;
        return this;
    }
    
    public AddressTestBuilder withNullComplement() {
        this.complement = null;
        return this;
    }
    
    public AddressTestBuilder withRioDeJaneiro() {
        this.city = "Rio de Janeiro";
        this.state = "RJ";
        this.zipCode = "20000000";
        this.district = "Centro";
        this.street = "Avenida Rio Branco";
        return this;
    }
    
    public AddressTestBuilder withBeloHorizonte() {
        this.city = "Belo Horizonte";
        this.state = "MG";
        this.zipCode = "30000000";
        this.district = "Savassi";
        this.street = "Rua Pernambuco";
        return this;
    }
    
    public AddressTestBuilder withSalvador() {
        this.city = "Salvador";
        this.state = "BA";
        this.zipCode = "40000000";
        this.district = "Pelourinho";
        this.street = "Rua do Pelourinho";
        return this;
    }
    
    public AddressTestBuilder withBrasilia() {
        this.city = "Brasília";
        this.state = "DF";
        this.zipCode = "70000000";
        this.district = "Asa Sul";
        this.street = "SCS Quadra 1";
        return this;
    }
    
    public Address buildDomain() {
        Address address = Address.create(street, number, complement, district, city, state, country, zipCode);
        if (!active) {
            address.deactivate();
        }
        return address;
    }
    
    public AddressViewDTO buildView() {
        return new AddressViewDTO(
            id,
            companyId,
            street,
            number,
            complement,
            district,
            city,
            state,
            country,
            zipCode,
            active
        );
    }
    
    public AddressCreateCommandDTO buildCreateCommand() {
        return new AddressCreateCommandDTO(
            street,
            number,
            complement,
            district,
            city,
            state,
            country,
            zipCode
        );
    }
    
    public AddressUpdateCommandDTO buildUpdateCommand() {
        return new AddressUpdateCommandDTO(
            street,
            number,
            complement,
            district,
            city,
            state,
            country,
            zipCode
        );
    }
    
    public AddressCreateDTO buildCreateDTO() {
        AddressCreateDTO dto = new AddressCreateDTO();
        dto.setStreet(street);
        dto.setNumber(number);
        dto.setComplement(complement);
        dto.setDistrict(district);
        dto.setCity(city);
        dto.setState(state);
        dto.setCountry(country);
        dto.setZipCode(zipCode);
        return dto;
    }
    
    public AddressUpdateDTO buildUpdateDTO() {
        AddressUpdateDTO dto = new AddressUpdateDTO();
        dto.setStreet(street);
        dto.setNumber(number);
        dto.setComplement(complement);
        dto.setDistrict(district);
        dto.setCity(city);
        dto.setState(state);
        dto.setCountry(country);
        dto.setZipCode(zipCode);
        return dto;
    }
    
    public AddressResponseDTO buildResponseDTO() {
        return AddressResponseDTO.builder()
            .id(id)
            .companyId(companyId)
            .street(street)
            .number(number)
            .complement(complement)
            .district(district)
            .city(city)
            .state(state)
            .country(country)
            .zipCode(zipCode)
            .active(active)
            .build();
    }
    
    public CompanyAddressJpaEntity buildJpaEntity(CompanyJpaEntity company) {
        return CompanyAddressJpaEntity.builder()
            .id(id)
            .company(company)
            .street(street)
            .number(number)
            .complement(complement)
            .district(district)
            .city(city)
            .state(state)
            .country(country)
            .zipCode(zipCode)
            .active(active)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    // Métodos estáticos para casos comuns
    public static Address createDefaultAddress() {
        return builder().buildDomain();
    }
    
    public static AddressViewDTO createDefaultAddressViewDTO() {
        return builder().buildView();
    }
    
    public static AddressCreateCommandDTO createDefaultCreateCommand() {
        return builder().buildCreateCommand();
    }
    
    public static AddressUpdateCommandDTO createDefaultUpdateCommand() {
        return builder().buildUpdateCommand();
    }
    
    public static AddressCreateDTO createDefaultCreateDTO() {
        return builder().buildCreateDTO();
    }
    
    public static AddressUpdateDTO createDefaultUpdateDTO() {
        return builder().buildUpdateDTO();
    }
    
    public static AddressResponseDTO createDefaultResponseDTO() {
        return builder().buildResponseDTO();
    }
    
    public static Address createInactiveAddress() {
        return builder().inactive().buildDomain();
    }
    
    public static AddressViewDTO createInactiveAddressViewDTO() {
        return builder().inactive().buildView();
    }
    
    public static Address createAddressWithNullComplement() {
        return builder().withNullComplement().buildDomain();
    }
    
    public static AddressViewDTO createAddressViewDTOWithNullComplement() {
        return builder().withNullComplement().buildView();
    }
    
    public static Address createRioDeJaneiroAddress() {
        return builder().withRioDeJaneiro().buildDomain();
    }
    
    public static AddressViewDTO createRioDeJaneiroAddressViewDTO() {
        return builder().withRioDeJaneiro().buildView();
    }
    
    public static Address createBeloHorizonteAddress() {
        return builder().withBeloHorizonte().buildDomain();
    }
    
    public static AddressViewDTO createBeloHorizonteAddressViewDTO() {
        return builder().withBeloHorizonte().buildView();
    }
    
    public static Address createSalvadorAddress() {
        return builder().withSalvador().buildDomain();
    }
    
    public static Address createBrasiliaAddress() {
        return builder().withBrasilia().buildDomain();
    }
}
