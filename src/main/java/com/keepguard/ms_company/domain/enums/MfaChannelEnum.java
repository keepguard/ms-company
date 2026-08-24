package com.keepguard.ms_company.domain.enums;

public enum MfaChannelEnum {
    EMAIL("E-mail"),
    SMS("SMS"),
    WHATSAPP("WhatsApp"),
    AUTHENTICATOR_APP("App Autenticador (TOTP)");

    private final String description;

    MfaChannelEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
