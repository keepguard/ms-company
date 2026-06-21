package com.keepguard.ms_company.domain.entity;

import com.keepguard.lib_common.exception.ValidationException;
import com.keepguard.lib_common.utils.BrazilianValidationUtils;
import com.keepguard.ms_company.domain.enums.AccountTypeEnum;

import java.util.Objects;
import java.util.UUID;

public final class BankAccount {

    private final UUID id;
    private final String code;
    private final String agency;
    private final String agencyDigit;
    private final String accountNumber;
    private final String accountDigit;
    private final AccountTypeEnum accountType;
    private boolean active;

    public BankAccount(UUID id, String code, String agency, String agencyDigit, String accountNumber,
                      String accountDigit, AccountTypeEnum accountType, boolean active) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.code = validateCode(code);
        this.agency = validateAgency(agency);
        this.agencyDigit = validateAgencyDigit(agencyDigit);
        this.accountNumber = validateAccountNumber(accountNumber);
        this.accountDigit = validateAccountDigit(accountDigit);
        this.accountType = Objects.requireNonNull(accountType, "Tipo da conta é obrigatório");
        this.active = active;
    }

    public static BankAccount create(String code, String agency, String agencyDigit, String accountNumber,
                                   String accountDigit, AccountTypeEnum accountType) {
        return new BankAccount(null, code, agency, agencyDigit, accountNumber, accountDigit, accountType, true);
    }

    public static BankAccount of(UUID id, String code, String agency, String agencyDigit, String accountNumber,
                                String accountDigit, AccountTypeEnum accountType, boolean active) {
        return new BankAccount(id, code, agency, agencyDigit, accountNumber, accountDigit, accountType, active);
    }

    private String validateCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException("Código do banco é obrigatório");
        }
        String cleanCode = code.replaceAll("\\D", "");
        BrazilianValidationUtils.validateBankCode(cleanCode);
        return cleanCode;
    }

    private String validateAgency(String agency) {
        if (agency == null || agency.trim().isEmpty()) {
            throw new IllegalArgumentException("Agência é obrigatória");
        }
        if (agency.length() > 10) {
            throw new IllegalArgumentException("Agência deve ter no máximo 10 caracteres");
        }
        return agency.trim();
    }

    private String validateAgencyDigit(String agencyDigit) {
        if (agencyDigit == null || agencyDigit.trim().isEmpty()) {
            return null;
        }
        if (agencyDigit.length() > 1) {
            throw new IllegalArgumentException("Dígito da agência deve ter 1 caractere");
        }
        return agencyDigit.trim();
    }

    private String validateAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Número da conta é obrigatório");
        }
        if (accountNumber.length() > 20) {
            throw new IllegalArgumentException("Número da conta deve ter no máximo 20 caracteres");
        }
        return accountNumber.trim();
    }

    private String validateAccountDigit(String accountDigit) {
        if (accountDigit == null || accountDigit.trim().isEmpty()) {
            throw new IllegalArgumentException("Dígito da conta é obrigatório");
        }
        if (accountDigit.length() > 1) {
            throw new IllegalArgumentException("Dígito da conta deve ter 1 caractere");
        }
        return accountDigit.trim();
    }

    // Getters
    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getAgency() { return agency; }
    public String getAgencyDigit() { return agencyDigit; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountDigit() { return accountDigit; }
    public AccountTypeEnum getAccountType() { return accountType; }
    public boolean isActive() { return active; }

    // Business methods
    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankAccount that = (BankAccount) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BankAccount{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", agency='" + agency + '\'' +
                ", agencyDigit='" + agencyDigit + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", accountDigit='" + accountDigit + '\'' +
                ", accountType=" + accountType +
                ", active=" + active +
                '}';
    }
}
