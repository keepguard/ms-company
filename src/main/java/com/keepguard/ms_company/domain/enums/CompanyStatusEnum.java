package com.keepguard.ms_company.domain.enums;

public enum CompanyStatusEnum {
    ACTIVE("Ativa"),
    INACTIVE("Inativa"),
    PENDING_APPROVAL("Aguardando Aprovação"),
    SUSPENDED("Suspensa"),
    BLOCKED("Bloqueada");

    private final String description;

    CompanyStatusEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
