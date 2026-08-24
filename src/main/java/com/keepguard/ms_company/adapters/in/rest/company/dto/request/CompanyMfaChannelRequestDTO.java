package com.keepguard.ms_company.adapters.in.rest.company.dto.request;

import com.keepguard.ms_company.domain.enums.MfaChannelEnum;
import jakarta.validation.constraints.NotNull;

public record CompanyMfaChannelRequestDTO(
    @NotNull(message = "Canal de MFA é obrigatório")
    MfaChannelEnum channel,

    boolean required,

    boolean enabled
) {
    public CompanyMfaChannelRequestDTO(MfaChannelEnum channel) {
        this(channel, true, true);
    }
}
