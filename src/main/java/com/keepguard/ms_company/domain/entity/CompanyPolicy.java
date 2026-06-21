package com.keepguard.ms_company.domain.entity;

import com.keepguard.lib_common.exception.ValidationException;
import com.keepguard.ms_company.domain.enums.PolicyStatusEnum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class CompanyPolicy {

    private final UUID id;
    private final UUID companyId;
    private String code;
    private String description;
    private PolicyStatusEnum status;
    private Integer version;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Lista de eventos de domínio
    private final List<Object> domainEvents = new ArrayList<>();

    private CompanyPolicy(UUID id, UUID companyId, String code, String description,
                         PolicyStatusEnum status, Integer version, LocalDateTime effectiveFrom,
                         LocalDateTime effectiveTo, LocalDateTime createdAt, LocalDateTime updatedAt,
                         String createdBy, String updatedBy) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.companyId = Objects.requireNonNull(companyId, "Company ID não pode ser nulo");
        this.code = validateCode(code);
        this.description = validateDescription(description);
        this.status = Objects.requireNonNullElse(status, PolicyStatusEnum.INACTIVE);
        this.version = Objects.requireNonNullElse(version, 1);
        this.effectiveFrom = Objects.requireNonNullElse(effectiveFrom, LocalDateTime.now());
        this.effectiveTo = effectiveTo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    public static CompanyPolicy create(UUID companyId, String code, String description,
                                     PolicyStatusEnum status, LocalDateTime effectiveFrom,
                                     LocalDateTime effectiveTo, String createdBy) {
        CompanyPolicy policy = new CompanyPolicy(null, companyId, code, description, status, 1,
                               effectiveFrom, effectiveTo, LocalDateTime.now(), LocalDateTime.now(),
                               createdBy, createdBy);
        policy.addDomainEvent("CompanyPolicyCreated");
        return policy;
    }

    public static CompanyPolicy of(UUID id, UUID companyId, String code, String description,
                                 PolicyStatusEnum status, Integer version, LocalDateTime effectiveFrom,
                                 LocalDateTime effectiveTo, LocalDateTime createdAt, LocalDateTime updatedAt,
                                 String createdBy, String updatedBy) {
        return new CompanyPolicy(id, companyId, code, description, status, version,
                               effectiveFrom, effectiveTo, createdAt, updatedAt, createdBy, updatedBy);
    }

    public void updateDescription(String description, String updatedBy) {
        this.description = validateDescription(description);
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
        addDomainEvent("CompanyPolicyUpdated");
    }

    public void updateStatus(PolicyStatusEnum status, String updatedBy) {
        this.status = Objects.requireNonNull(status, "Status não pode ser nulo");
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
        addDomainEvent("CompanyPolicyUpdated");
    }

    public void updateEffectiveTo(LocalDateTime effectiveTo, String updatedBy) {
        this.effectiveTo = effectiveTo;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
        addDomainEvent("CompanyPolicyUpdated");
    }

    public void incrementVersion(String updatedBy) {
        this.version++;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
        addDomainEvent("CompanyPolicyUpdated");
    }

    public void deactivate(String updatedBy) {
        this.status = PolicyStatusEnum.INACTIVE;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
        addDomainEvent("CompanyPolicyDeactivated");
    }

    public boolean isActive() {
        return PolicyStatusEnum.ACTIVE.equals(this.status);
    }

    public boolean isEffective() {
        LocalDateTime now = LocalDateTime.now();
        return isActive() &&
               (effectiveFrom == null || !effectiveFrom.isAfter(now)) &&
               (effectiveTo == null || !effectiveTo.isBefore(now));
    }

    private String validateCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException("Código da política não pode ser nulo ou vazio");
        }
        if (code.length() > 64) {
            throw new ValidationException("Código da política não pode ter mais de 64 caracteres");
        }
        return code.trim();
    }

    private String validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Descrição da política não pode ser nula ou vazia");
        }
        if (description.length() > 255) {
            throw new ValidationException("Descrição da política não pode ter mais de 255 caracteres");
        }
        return description.trim();
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public PolicyStatusEnum getStatus() {
        return status;
    }

    public Integer getVersion() {
        return version;
    }

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDateTime getEffectiveTo() {
        return effectiveTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    private void addDomainEvent(String eventType) {
        this.domainEvents.add(new DomainEvent(eventType, this.id, this.companyId, this.code));
    }

    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyPolicy that = (CompanyPolicy) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CompanyPolicy{" +
                "id=" + id +
                ", companyId=" + companyId +
                ", code='" + code + '\'' +
                ", status=" + status +
                ", version=" + version +
                '}';
    }

    private static class DomainEvent {
        private final String eventType;
        private final UUID policyId;
        private final UUID companyId;
        private final String code;
        private final LocalDateTime occurredAt;

        public DomainEvent(String eventType, UUID policyId, UUID companyId, String code) {
            this.eventType = eventType;
            this.policyId = policyId;
            this.companyId = companyId;
            this.code = code;
            this.occurredAt = LocalDateTime.now();
        }

        public String getEventType() { return eventType; }
        public UUID getPolicyId() { return policyId; }
        public UUID getCompanyId() { return companyId; }
        public String getCode() { return code; }
        public LocalDateTime getOccurredAt() { return occurredAt; }
    }
}
