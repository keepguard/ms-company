package com.keepguard.ms_company.domain.enums;

public enum MfaPolicyEnum {
    NONE("Sem MFA"),
    EMAIL_ONLY("Apenas E-mail"),
    SMS_ONLY("Apenas SMS"),
    COMBINED_EMAIL_SMS("E-mail e SMS Combinados");

    private final String description;

    MfaPolicyEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
