package com.keepguard.ms_company.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "company_cnaes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "code"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyCnaeJpaEntity {

    @Id

    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyJpaEntity company;

    @Column(name = "code", nullable = false, length = 7)
    private String code;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "section", length = 1)
    private String section;

    @Column(name = "division", length = 2)
    private String division;

    @Column(name = "group_code", length = 3)
    private String groupCode;

    @Column(name = "class_code", length = 4)
    private String classCode;

    @Column(name = "subclass_code", length = 5)
    private String subclassCode;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "principal", nullable = false)
    @Builder.Default
    private Boolean principal = false;

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
