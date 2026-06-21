package com.keepguard.ms_company.adapters.in.rest.companypolicy.dto;

import com.keepguard.ms_company.application.dto.companypolicy.CompanyPolicyViewDTO;
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
public class CompanyPolicyResponse {

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

    public static CompanyPolicyResponse from(CompanyPolicyViewDTO view) {
        return CompanyPolicyResponse.builder()
                .id(view.id())
                .companyId(view.companyId())
                .code(view.code())
                .description(view.description())
                .status(view.status())
                .version(view.version())
                .effectiveFrom(view.effectiveFrom())
                .effectiveTo(view.effectiveTo())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .createdBy(view.createdBy())
                .updatedBy(view.updatedBy())
                .build();
    }
}
