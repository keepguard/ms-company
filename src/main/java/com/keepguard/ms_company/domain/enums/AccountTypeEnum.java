package com.keepguard.ms_company.domain.enums;

public enum AccountTypeEnum {
    CORRENTE("Conta Corrente"),
    POUPANCA("Conta Poupança"),
    PJ("Conta Pessoa Jurídica");

    private final String description;

    AccountTypeEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
