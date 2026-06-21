package com.keepguard.ms_company.domain.enums;

public enum TaxRegimeEnum {
    SIMPLES_NACIONAL("Simples Nacional"),
    LUCRO_PRESUMIDO("Lucro Presumido"),
    LUCRO_REAL("Lucro Real");

    private final String description;

    TaxRegimeEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
