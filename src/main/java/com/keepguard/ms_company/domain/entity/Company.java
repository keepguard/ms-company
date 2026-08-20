package com.keepguard.ms_company.domain.entity;

import com.keepguard.lib_common.exception.InvalidStatusException;
import com.keepguard.lib_common.exception.ValidationException;
import com.keepguard.lib_common.utils.BrazilianValidationUtils;
import com.keepguard.ms_company.domain.enums.CompanyStatusEnum;
import com.keepguard.ms_company.domain.enums.TaxRegimeEnum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Company {

    private final UUID id;
    private final UUID codeCompany;
    private final UUID tenantId;
    private String name;
    private String legalName;
    private String cnpj;
    private String stateRegistration;
    private String municipalRegistration;
    private final List<Address> addresses = new ArrayList<>();
    private final List<Contact> contacts = new ArrayList<>();
    private final List<Representative> representatives = new ArrayList<>();
    private final List<BankAccount> bankAccounts = new ArrayList<>();
    private final List<Cnae> cnaes = new ArrayList<>();
    private TaxRegimeEnum taxRegime;
    private String ein;
    private CompanyStatusEnum status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Company(UUID id, UUID codeCompany, UUID tenantId, String name, String legalName, String cnpj, String stateRegistration,
                   String municipalRegistration, TaxRegimeEnum taxRegime, String ein,
                   CompanyStatusEnum status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.codeCompany = codeCompany == null ? UUID.randomUUID() : codeCompany;
        this.tenantId = tenantId == null ? UUID.randomUUID() : tenantId;
        this.name = validateName(name);
        this.legalName = validateLegalName(legalName);
        this.cnpj = validateCnpj(cnpj);
        this.stateRegistration = stateRegistration;
        this.municipalRegistration = municipalRegistration;
        this.taxRegime = Objects.requireNonNullElse(taxRegime, TaxRegimeEnum.SIMPLES_NACIONAL);
        this.ein = ein;
        this.status = Objects.requireNonNullElse(status, CompanyStatusEnum.PENDING_APPROVAL);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Company create(String name, String legalName, String cnpj, String stateRegistration,
                                String municipalRegistration, TaxRegimeEnum taxRegime, String ein) {
        return new Company(null, null, null, name, legalName, cnpj, stateRegistration, municipalRegistration,
                          taxRegime, ein, CompanyStatusEnum.PENDING_APPROVAL,
                          LocalDateTime.now(), LocalDateTime.now());
    }

    public static Company of(UUID id, UUID codeCompany, UUID tenantId, String name, String legalName, String cnpj, String stateRegistration,
                            String municipalRegistration, TaxRegimeEnum taxRegime, String ein,
                            CompanyStatusEnum status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Company(id, codeCompany, tenantId, name, legalName, cnpj, stateRegistration, municipalRegistration,
                          taxRegime, ein, status, createdAt, updatedAt);
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome fantasia é obrigatório");
        }
        if (name.length() > 150) {
            throw new IllegalArgumentException("Nome fantasia deve ter no máximo 150 caracteres");
        }
        return name.trim();
    }

    private String validateLegalName(String legalName) {
        if (legalName == null || legalName.trim().isEmpty()) {
            throw new IllegalArgumentException("Razão social é obrigatória");
        }
        if (legalName.length() > 200) {
            throw new IllegalArgumentException("Razão social deve ter no máximo 200 caracteres");
        }
        return legalName.trim();
    }

    private String validateCnpj(String cnpj) {
        if (cnpj == null || cnpj.trim().isEmpty()) {
            throw new ValidationException("CNPJ é obrigatório");
        }
        String cleanCnpj = cnpj.replaceAll("\\D", "");
        BrazilianValidationUtils.validateCnpj(cleanCnpj);
        return cleanCnpj;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getCodeCompany() { return codeCompany; }
    public UUID getTenantId() { return tenantId; }
    public String getName() { return name; }
    public String getLegalName() { return legalName; }
    public String getCnpj() { return cnpj; }
    public String getStateRegistration() { return stateRegistration; }
    public String getMunicipalRegistration() { return municipalRegistration; }
    public List<Address> getAddresses() { return new ArrayList<>(addresses); }
    public List<Contact> getContacts() { return new ArrayList<>(contacts); }
    public List<Representative> getRepresentatives() { return new ArrayList<>(representatives); }
    public List<BankAccount> getBankAccounts() { return new ArrayList<>(bankAccounts); }
    public List<Cnae> getCnaes() { return new ArrayList<>(cnaes); }
    public TaxRegimeEnum getTaxRegime() { return taxRegime; }
    public String getEin() { return ein; }
    public CompanyStatusEnum getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Business methods
    public void approve() {
        if (status != CompanyStatusEnum.PENDING_APPROVAL) {
            throw InvalidStatusException.invalidTransition("Empresa", status.toString(), CompanyStatusEnum.PENDING_APPROVAL.toString());
        }

        // Validações obrigatórias para aprovação
        validateRequiredDataForApproval();

        this.status = CompanyStatusEnum.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    private void validateRequiredDataForApproval() {
        // 1. Deve ter pelo menos um endereço ativo
        if (getActiveAddress().isEmpty()) {
            throw new ValidationException("Empresa deve ter pelo menos um endereço ativo para ser aprovada");
        }

        // 2. Deve ter pelo menos uma conta bancária ativa
        if (getActiveBankAccount().isEmpty()) {
            throw new ValidationException("Empresa deve ter pelo menos uma conta bancária ativa para ser aprovada");
        }

        // 3. Deve ter pelo menos um CNAE ativo e principal
        if (getPrincipalCnae().isEmpty()) {
            throw new ValidationException("Empresa deve ter pelo menos um CNAE ativo e principal para ser aprovada");
        }

        // 4. Deve ter pelo menos um contato ativo
        if (getActiveContact().isEmpty()) {
            throw new ValidationException("Empresa deve ter pelo menos um contato ativo para ser aprovada");
        }

        // 5. Deve ter pelo menos um representante ativo
        if (getActiveRepresentative().isEmpty()) {
            throw new ValidationException("Empresa deve ter pelo menos um representante ativo para ser aprovada");
        }

    }

    public void reject() {
        if (status != CompanyStatusEnum.PENDING_APPROVAL) {
            throw InvalidStatusException.invalidTransition("Empresa", status.toString(), CompanyStatusEnum.PENDING_APPROVAL.toString());
        }
        this.status = CompanyStatusEnum.BLOCKED;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        if (status == CompanyStatusEnum.PENDING_APPROVAL) {
            throw InvalidStatusException.invalidOperation("Empresa", status.toString(), "ativada - deve ser aprovada primeiro");
        }
        if (status != CompanyStatusEnum.INACTIVE) {
            throw InvalidStatusException.invalidTransition("Empresa", status.toString(), CompanyStatusEnum.INACTIVE.toString());
        }
        this.status = CompanyStatusEnum.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        if (status == CompanyStatusEnum.PENDING_APPROVAL) {
            throw InvalidStatusException.invalidOperation("Empresa", status.toString(), "desativada - deve ser aprovada primeiro");
        }
        if (status != CompanyStatusEnum.ACTIVE) {
            throw InvalidStatusException.invalidTransition("Empresa", status.toString(), CompanyStatusEnum.ACTIVE.toString());
        }
        this.status = CompanyStatusEnum.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void suspend() {
        if (status == CompanyStatusEnum.PENDING_APPROVAL) {
            throw InvalidStatusException.invalidOperation("Empresa", status.toString(), "suspensa - deve ser aprovada primeiro");
        }
        if (status != CompanyStatusEnum.ACTIVE) {
            throw InvalidStatusException.invalidTransition("Empresa", status.toString(), CompanyStatusEnum.ACTIVE.toString());
        }
        this.status = CompanyStatusEnum.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    public void block() {
        this.status = CompanyStatusEnum.BLOCKED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return CompanyStatusEnum.ACTIVE.equals(this.status);
    }

    public boolean isBlockedOrSuspended() {
        return CompanyStatusEnum.BLOCKED.equals(this.status) ||
               CompanyStatusEnum.SUSPENDED.equals(this.status);
    }

    public void validateStatusForOperations() {
        if (isBlockedOrSuspended()) {
            throw new com.keepguard.ms_company.application.service.exception.InvalidStatusForOperationException(
                "Não é possível realizar operações na empresa com status '" + this.status.getDescription() + "'. " +
                "Operações são permitidas apenas para empresas com status Ativa, Inativa ou Aguardando Aprovação."
            );
        }
    }

    // --- Address management ---
    public void addAddress(Address address) {
        Objects.requireNonNull(address, "Endereço é obrigatório");
        if (address.isActive()) {
            addresses.forEach(Address::deactivate);
        }
        addresses.add(address);
        this.updatedAt = LocalDateTime.now();
    }

    public Optional<Address> getActiveAddress() {
        return addresses.stream().filter(Address::isActive).findFirst();
    }

    // --- Contact management ---
    public void addContact(Contact contact) {
        Objects.requireNonNull(contact, "Contato é obrigatório");
        // Permite múltiplos contatos ativos
        contacts.add(contact);
        this.updatedAt = LocalDateTime.now();
    }

    public Optional<Contact> getActiveContact() {
        return contacts.stream().filter(Contact::isActive).findFirst();
    }

    public List<Contact> getActiveContacts() {
        return contacts.stream().filter(Contact::isActive).toList();
    }

    // --- Representative management ---
    public void addRepresentative(Representative representative) {
        Objects.requireNonNull(representative, "Representante é obrigatório");
        // Permite múltiplos representantes ativos
        representatives.add(representative);
        this.updatedAt = LocalDateTime.now();
    }

    public Optional<Representative> getActiveRepresentative() {
        return representatives.stream().filter(Representative::isActive).findFirst();
    }

    public List<Representative> getActiveRepresentatives() {
        return representatives.stream()
            .filter(Representative::isActive)
            .sorted((r1, r2) -> {
                // Principal primeiro (se houver campo principal)
                // Por enquanto, ordena por data de criação (mais recente primeiro)
                return r1.getId().compareTo(r2.getId());
            })
            .toList();
    }

    // --- BankAccount management ---
    public void addBankAccount(BankAccount bankAccount) {
        Objects.requireNonNull(bankAccount, "Dados bancários são obrigatórios");
        if (bankAccount.isActive()) {
            bankAccounts.forEach(BankAccount::deactivate);
        }
        bankAccounts.add(bankAccount);
        this.updatedAt = LocalDateTime.now();
    }

    public Optional<BankAccount> getActiveBankAccount() {
        return bankAccounts.stream().filter(BankAccount::isActive).findFirst();
    }

    // --- CNAE management ---
    public void addCnae(Cnae cnae) {
        Objects.requireNonNull(cnae, "CNAE é obrigatório");
        if (cnae.isPrincipal()) {
            // Desativar outros CNAEs principais
            cnaes.stream()
                .filter(Cnae::isPrincipal)
                .forEach(Cnae::unsetAsPrincipal);
        }
        cnaes.add(cnae);
        this.updatedAt = LocalDateTime.now();
    }

    public void removeCnae(UUID cnaeId) {
        cnaes.removeIf(cnae -> cnae.getId().equals(cnaeId));
        this.updatedAt = LocalDateTime.now();
    }

    public Optional<Cnae> getPrincipalCnae() {
        return cnaes.stream()
            .filter(Cnae::isPrincipal)
            .filter(Cnae::isActive)
            .findFirst();
    }

    public List<Cnae> getActiveCnaes() {
        return cnaes.stream()
            .filter(Cnae::isActive)
            .toList();
    }

    public List<Cnae> getSecondaryCnaes() {
        return cnaes.stream()
            .filter(cnae -> !cnae.isPrincipal())
            .filter(Cnae::isActive)
            .toList();
    }

    public void updateBasicInfo(String name, String legalName, String stateRegistration,
                               String municipalRegistration, String ein) {
        this.name = validateName(name);
        this.legalName = validateLegalName(legalName);
        this.stateRegistration = stateRegistration;
        this.municipalRegistration = municipalRegistration;
        this.ein = ein;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateTaxRegime(TaxRegimeEnum taxRegime) {
        if (taxRegime == null) {
            throw new IllegalArgumentException("Regime tributário é obrigatório");
        }
        this.taxRegime = taxRegime;
        this.updatedAt = LocalDateTime.now();
    }

    public void setPrincipalCnae(UUID cnaeId) {
        Optional<Cnae> targetCnae = cnaes.stream()
            .filter(cnae -> cnae.getId().equals(cnaeId))
            .findFirst();

        if (targetCnae.isEmpty()) {
            throw new IllegalArgumentException("CNAE não encontrado");
        }

        // Desativar outros CNAEs principais
        cnaes.stream()
            .filter(Cnae::isPrincipal)
            .forEach(Cnae::unsetAsPrincipal);

        // Definir novo CNAE principal
        targetCnae.get().setAsPrincipal();
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        return Objects.equals(id, company.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", legalName='" + legalName + '\'' +
                ", cnpj='" + cnpj + '\'' +
                ", status=" + status +
                '}';
    }
}