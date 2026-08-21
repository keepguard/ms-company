package com.keepguard.ms_company.infrastructure.persistence.entity;

import com.keepguard.ms_company.domain.enums.CompanyStatusEnum;
import com.keepguard.ms_company.domain.enums.TaxRegimeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyJpaEntity {

    @Id

    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "code_company", unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID codeCompany;

    @Column(name = "tenant_id", unique = true, nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "legal_name", nullable = false, length = 200)
    private String legalName;

    @Column(name = "cnpj", unique = true, nullable = false, length = 14)
    private String cnpj;

    @Column(name = "state_registration", length = 20)
    private String stateRegistration;

    @Column(name = "municipal_registration", length = 20)
    private String municipalRegistration;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    @Builder.Default
    private List<CompanyAddressJpaEntity> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    @Builder.Default
    private List<CompanyContactJpaEntity> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    @Builder.Default
    private List<CompanyRepresentativeJpaEntity> representatives = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    @Builder.Default
    private List<CompanyBankAccountJpaEntity> bankAccounts = new ArrayList<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    @Builder.Default
    private List<CompanyCnaeJpaEntity> cnaes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_regime", nullable = false)
    private TaxRegimeEnum taxRegime;

    @Column(name = "ein", length = 20)
    private String ein;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CompanyStatusEnum status = CompanyStatusEnum.PENDING_APPROVAL;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.codeCompany == null) {
            this.codeCompany = UUID.randomUUID();
        }
        if (this.tenantId == null) {
            this.tenantId = UUID.randomUUID();
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}