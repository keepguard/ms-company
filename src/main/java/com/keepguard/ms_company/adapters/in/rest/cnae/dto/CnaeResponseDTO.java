package com.keepguard.ms_company.adapters.in.rest.cnae.dto;

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
public class CnaeResponseDTO {

    private UUID id;
    private UUID companyId;
    private String code;
    private String description;
    private String section;
    private String division;
    private String groupCode;
    private String classCode;
    private String subclassCode;
    private boolean active;
    private boolean principal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
