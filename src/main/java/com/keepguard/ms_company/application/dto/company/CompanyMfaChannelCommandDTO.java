package com.keepguard.ms_company.application.dto.company;

import com.keepguard.ms_company.domain.enums.MfaChannelEnum;

public record CompanyMfaChannelCommandDTO(
    MfaChannelEnum channel,
    boolean required,
    boolean enabled
) {}
