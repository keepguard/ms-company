package com.keepguard.ms_company.adapters.in.rest.company.dto.response;

import com.keepguard.ms_company.domain.enums.MfaChannelEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyMfaChannelResponseDTO {

    private UUID id;
    private MfaChannelEnum channel;
    private boolean required;
    private boolean enabled;
}
