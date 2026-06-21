package com.keepguard.ms_company.adapters.in.rest.address.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponseDTO {

    private UUID id;
    private UUID companyId;
    private String street;
    private String number;
    private String complement;
    private String district;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private boolean active;
}
