package com.keepguard.ms_company.domain.enums;

public enum PolicyStatusEnum {
    ACTIVE("Ativa"),
    INACTIVE("Inativa");

    private final String description;

    PolicyStatusEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
