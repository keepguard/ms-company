package com.keepguard.ms_company.adapters.in.rest.companypolicy.dto.response;

import com.keepguard.ms_company.domain.enums.PolicyStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyPolicyResponseDTO {

    private UUID id;
    private UUID companyId;
    private String code;
    private String description;
    private PolicyStatusEnum status;
    private Integer version;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
