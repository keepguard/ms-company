package com.keepguard.ms_company.infrastructure.persistence.entity;

import com.keepguard.ms_company.domain.enums.AccountTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "company_bank_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyBankAccountJpaEntity {

    @Id

    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyJpaEntity company;

    @Column(name = "bank_code", nullable = false, length = 3)
    private String code;

    @Column(name = "bank_agency", nullable = false, length = 10)
    private String agency;

    @Column(name = "bank_agency_digit", length = 1)
    private String agencyDigit;

    @Column(name = "bank_account_number", nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "bank_account_digit", nullable = false, length = 1)
    private String accountDigit;

    @Enumerated(EnumType.STRING)
    @Column(name = "bank_account_type", nullable = false)
    private AccountTypeEnum accountType;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
