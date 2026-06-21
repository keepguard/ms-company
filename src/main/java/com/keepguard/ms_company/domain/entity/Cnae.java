package com.keepguard.ms_company.domain.entity;

import com.keepguard.lib_common.exception.ValidationException;
import com.keepguard.lib_common.utils.BrazilianValidationUtils;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class Cnae {

    private final UUID id;
    private String code;
    private String description;
    private final String section;
    private final String division;
    private final String groupCode;
    private final String classCode;
    private final String subclassCode;
    private boolean active;
    private boolean principal;
    private final UUID companyId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Cnae(UUID id, String code, String description, String section, String division,
                String groupCode, String classCode, String subclassCode, boolean active,
                boolean principal, UUID companyId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.code = validateCode(code);
        this.description = validateDescription(description);
        this.section = section;
        this.division = division;
        this.groupCode = groupCode;
        this.classCode = classCode;
        this.subclassCode = subclassCode;
        this.active = active;
        this.principal = principal;
        this.companyId = Objects.requireNonNull(companyId, "ID da empresa é obrigatório");
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public static Cnae create(String code, String description, String section, String division,
                              String groupCode, String classCode, String subclassCode,
                              boolean principal, UUID companyId) {
        return new Cnae(null, code, description, section, division, groupCode, classCode,
                       subclassCode, true, principal, companyId, LocalDateTime.now(), LocalDateTime.now());
    }

    public static Cnae of(UUID id, String code, String description, String section, String division,
                          String groupCode, String classCode, String subclassCode, boolean active,
                          boolean principal, UUID companyId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Cnae(id, code, description, section, division, groupCode, classCode,
                       subclassCode, active, principal, companyId, createdAt, updatedAt);
    }

    private String validateCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException("Código CNAE é obrigatório");
        }
        String cleanCode = code.replaceAll("[^0-9]", "").trim();
        BrazilianValidationUtils.validateCnae(cleanCode);
        return cleanCode;
    }

    private String validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição do CNAE é obrigatória");
        }
        if (description.length() > 500) {
            throw new IllegalArgumentException("Descrição do CNAE deve ter no máximo 500 caracteres");
        }
        return description.trim();
    }

    // Getters
    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public String getSection() { return section; }
    public String getDivision() { return division; }
    public String getGroupCode() { return groupCode; }
    public String getClassCode() { return classCode; }
    public String getSubclassCode() { return subclassCode; }
    public boolean isActive() { return active; }
    public boolean isPrincipal() { return principal; }
    public UUID getCompanyId() { return companyId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Business methods
    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        if (this.principal) {
            throw new IllegalStateException("Não é possível desativar o CNAE principal. Defina outro como principal primeiro.");
        }
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void setAsPrincipal() {
        this.principal = true;
        this.active = true; // CNAE principal deve estar ativo
        this.updatedAt = LocalDateTime.now();
    }

    public void unsetAsPrincipal() {
        this.principal = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateDescription(String description) {
        this.description = validateDescription(description);
        this.updatedAt = LocalDateTime.now();
    }

    public void updateCode(String code) {
        this.code = validateCode(code);
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cnae cnae = (Cnae) o;
        return Objects.equals(id, cnae.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Cnae{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", principal=" + principal +
                ", active=" + active +
                ", companyId=" + companyId +
                '}';
    }
}
