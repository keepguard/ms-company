package com.keepguard.ms_company.adapters.in.rest.company.dto.response;

import com.keepguard.ms_company.domain.enums.CompanyStatusEnum;
import com.keepguard.ms_company.domain.enums.TaxRegimeEnum;
import com.keepguard.ms_company.adapters.in.rest.cnae.dto.CnaeResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.AddressDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.ContactDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.RepresentativeDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.BankAccountDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponseDTO {

    private UUID id;
    private UUID codeCompany;
    private UUID tenantId;
    private String name;
    private String legalName;
    private String cnpj;
    private String stateRegistration;
    private String municipalRegistration;
    private AddressDTO address;
    private List<ContactDTO> contacts;
    private List<RepresentativeDTO> representatives;
    private BankAccountDTO bankAccount;
    private TaxRegimeEnum taxRegime;
    private List<CnaeResponseDTO> cnaes;
    private String ein;
    private CompanyStatusEnum status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
