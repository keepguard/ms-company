package com.keepguard.ms_company.application.dto.company;

import com.keepguard.ms_company.domain.enums.CompanyStatusEnum;
import com.keepguard.ms_company.domain.enums.TaxRegimeEnum;
import com.keepguard.ms_company.application.dto.address.AddressViewDTO;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountViewDTO;
import com.keepguard.ms_company.application.dto.contact.ContactViewDTO;
import com.keepguard.ms_company.application.dto.representative.RepresentativeViewDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeViewDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CompanyViewDTO(
    UUID id,
    UUID codeCompany,
    UUID xApplication,
    String name,
    String legalName,
    String cnpj,
    String stateRegistration,
    String municipalRegistration,
    AddressViewDTO address,
    List<ContactViewDTO> contacts,
    List<RepresentativeViewDTO> representatives,
    BankAccountViewDTO bankAccount,
    TaxRegimeEnum taxRegime,
    List<CnaeViewDTO> cnaes,
    String ein,
    CompanyStatusEnum status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public CompanyViewDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID é obrigatório");
        }
        if (codeCompany == null) {
            throw new IllegalArgumentException("CodeCompany é obrigatório");
        }
        if (xApplication == null) {
            throw new IllegalArgumentException("XApplication é obrigatório");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome fantasia é obrigatório");
        }
        if (legalName == null || legalName.trim().isEmpty()) {
            throw new IllegalArgumentException("Razão social é obrigatória");
        }
        if (cnpj == null || cnpj.trim().isEmpty()) {
            throw new IllegalArgumentException("CNPJ é obrigatório");
        }
        // address, contact, representative e bankAccount podem ser nulos
        // eles serão adicionados posteriormente através de controllers específicos
        if (taxRegime == null) {
            throw new IllegalArgumentException("Regime tributário é obrigatório");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status é obrigatório");
        }
    }
}
