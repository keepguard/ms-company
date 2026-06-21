package com.keepguard.ms_company.adapters.in.rest.contact.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponseDTO {

    private UUID id;
    private UUID companyId;
    private String name;
    private String email;
    private String phone;
    private String website;
    private String position;
    private String department;
    private boolean active;
}
