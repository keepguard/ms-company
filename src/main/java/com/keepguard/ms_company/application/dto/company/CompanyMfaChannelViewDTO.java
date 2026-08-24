package com.keepguard.ms_company.application.dto.company;

import com.keepguard.ms_company.domain.enums.MfaChannelEnum;
import java.util.UUID;

public record CompanyMfaChannelViewDTO(
    UUID id,
    MfaChannelEnum channel,
    boolean required,
    boolean enabled
) {}
