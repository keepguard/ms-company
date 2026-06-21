package com.keepguard.ms_company.test.builder;

import com.keepguard.ms_company.adapters.in.rest.company.dto.request.CompanyCreateDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.request.CompanyUpdateDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.response.CompanyResponseDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.response.CompanySimpleResponseDTO;
import com.keepguard.ms_company.application.dto.company.CompanyCreateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.company.CompanyViewDTO;
import com.keepguard.ms_company.domain.entity.Company;
import com.keepguard.ms_company.domain.enums.TaxRegimeEnum;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyJpaEntity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Builder para criação de dados de teste para Company
 * Facilita a criação de objetos de teste com dados padrão
 */
public class CompanyTestBuilder {
    
    private UUID id = UUID.randomUUID();
    private UUID codeCompany = UUID.randomUUID();
    private UUID xApplication = UUID.randomUUID();
    private String name = "Empresa Teste";
    private String legalName = "Empresa Teste Ltda";
    private String cnpj = "11222333000181";
    private String stateRegistration = "123456789";
    private String municipalRegistration = "987654321";
    private TaxRegimeEnum taxRegime = TaxRegimeEnum.SIMPLES_NACIONAL;
    private com.keepguard.ms_company.domain.enums.CompanyStatusEnum status = com.keepguard.ms_company.domain.enums.CompanyStatusEnum.PENDING_APPROVAL;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public static CompanyTestBuilder builder() {
        return new CompanyTestBuilder();
    }
    
    public CompanyTestBuilder withId(UUID id) {
        this.id = id;
        return this;
    }
    
    public CompanyTestBuilder withXApplication(UUID xApplication) {
        this.xApplication = xApplication;
        return this;
    }
    
    public CompanyTestBuilder withName(String name) {
        this.name = name;
        return this;
    }
    
    public CompanyTestBuilder withLegalName(String legalName) {
        this.legalName = legalName;
        return this;
    }
    
    public CompanyTestBuilder withCnpj(String cnpj) {
        this.cnpj = cnpj;
        return this;
    }
    
    public CompanyTestBuilder withStatus(com.keepguard.ms_company.domain.enums.CompanyStatusEnum status) {
        this.status = status;
        return this;
    }
    
    public CompanyTestBuilder withStateRegistration(String stateRegistration) {
        this.stateRegistration = stateRegistration;
        return this;
    }
    
    public CompanyTestBuilder withMunicipalRegistration(String municipalRegistration) {
        this.municipalRegistration = municipalRegistration;
        return this;
    }
    
    public CompanyTestBuilder withTaxRegime(TaxRegimeEnum taxRegime) {
        this.taxRegime = taxRegime;
        return this;
    }
    
    public CompanyTestBuilder withActiveStatus() {
        this.status = com.keepguard.ms_company.domain.enums.CompanyStatusEnum.ACTIVE;
        return this;
    }
    
    public CompanyTestBuilder withInactiveStatus() {
        this.status = com.keepguard.ms_company.domain.enums.CompanyStatusEnum.INACTIVE;
        return this;
    }
    
    public CompanyTestBuilder withPendingApprovalStatus() {
        this.status = com.keepguard.ms_company.domain.enums.CompanyStatusEnum.PENDING_APPROVAL;
        return this;
    }
    
    public CompanyTestBuilder withBlockedStatus() {
        this.status = com.keepguard.ms_company.domain.enums.CompanyStatusEnum.BLOCKED;
        return this;
    }
    
    public CompanyTestBuilder withSuspendedStatus() {
        this.status = com.keepguard.ms_company.domain.enums.CompanyStatusEnum.SUSPENDED;
        return this;
    }
    
    public CompanyTestBuilder withSimplesNacional() {
        this.taxRegime = TaxRegimeEnum.SIMPLES_NACIONAL;
        return this;
    }
    
    public CompanyTestBuilder withLucroPresumido() {
        this.taxRegime = TaxRegimeEnum.LUCRO_PRESUMIDO;
        return this;
    }
    
    public CompanyTestBuilder withLucroReal() {
        this.taxRegime = TaxRegimeEnum.LUCRO_REAL;
        return this;
    }
    
    
    public CompanyTestBuilder withTechCompany() {
        this.name = "Tech Solutions Ltda";
        this.legalName = "Tech Solutions Tecnologia Ltda";
        this.cnpj = "11222333000181";
        this.taxRegime = TaxRegimeEnum.SIMPLES_NACIONAL;
        return this;
    }
    
    public CompanyTestBuilder withRetailCompany() {
        this.name = "Comércio Varejista Ltda";
        this.legalName = "Comércio Varejista de Produtos Ltda";
        this.cnpj = "11222333000181";
        this.taxRegime = TaxRegimeEnum.LUCRO_PRESUMIDO;
        return this;
    }
    
    public CompanyTestBuilder withIndustrialCompany() {
        this.name = "Indústria Metalúrgica Ltda";
        this.legalName = "Indústria Metalúrgica de Produtos Ltda";
        this.cnpj = "11222333000181";
        this.taxRegime = TaxRegimeEnum.LUCRO_REAL;
        return this;
    }
    
    public CompanyTestBuilder withServiceCompany() {
        this.name = "Prestação de Serviços Ltda";
        this.legalName = "Prestação de Serviços Especializados Ltda";
        this.cnpj = "11222333000181";
        this.taxRegime = TaxRegimeEnum.SIMPLES_NACIONAL;
        return this;
    }
    
    public Company buildDomain() {
        return Company.create(
            name,
            legalName,
            cnpj,
            stateRegistration,
            municipalRegistration,
            taxRegime,
            stateRegistration
        );
    }
    
    public CompanyViewDTO buildView() {
        return new CompanyViewDTO(
            id,
            codeCompany,
            xApplication,
            name,
            legalName,
            cnpj,
            stateRegistration,
            municipalRegistration,
            null, // address
            new java.util.ArrayList<>(), // contacts
            new java.util.ArrayList<>(), // representatives
            null, // bankAccount
            taxRegime,
            new java.util.ArrayList<>(), // cnaes
            stateRegistration, // ein
            status,
            createdAt,
            updatedAt
        );
    }
    
    public CompanyCreateCommandDTO buildCreateCommand() {
        return new CompanyCreateCommandDTO(
            name,
            legalName,
            cnpj,
            stateRegistration,
            municipalRegistration,
            taxRegime,
            stateRegistration
        );
    }
    
    public CompanyUpdateCommandDTO buildUpdateCommand() {
        return new CompanyUpdateCommandDTO(
            name,
            legalName,
            stateRegistration,
            municipalRegistration,
            taxRegime,
            stateRegistration
        );
    }
    
    public CompanyCreateDTO buildCreateDTO() {
        return new CompanyCreateDTO(
            name,
            legalName,
            cnpj,
            stateRegistration,
            municipalRegistration,
            taxRegime,
            stateRegistration
        );
    }
    
    public CompanyUpdateDTO buildUpdateDTO() {
        return new CompanyUpdateDTO(
            name,
            legalName,
            stateRegistration,
            municipalRegistration,
            taxRegime,
            stateRegistration
        );
    }
    
    public CompanyResponseDTO buildResponseDTO() {
        return new CompanyResponseDTO(
            id,
            codeCompany,
            xApplication,
            name,
            legalName,
            cnpj,
            stateRegistration,
            municipalRegistration,
            null, // address
            new java.util.ArrayList<>(), // contacts
            new java.util.ArrayList<>(), // representatives
            null, // bankAccount
            taxRegime,
            new java.util.ArrayList<>(), // cnaes
            stateRegistration, // ein
            status,
            createdAt,
            updatedAt
        );
    }
    
    public CompanySimpleResponseDTO buildSimpleResponseDTO() {
        return new CompanySimpleResponseDTO(
            id,
            codeCompany,
            xApplication,
            name,
            legalName,
            cnpj,
            stateRegistration,
            municipalRegistration,
            taxRegime,
            stateRegistration, // ein
            status,
            createdAt,
            updatedAt
        );
    }
    
    public CompanyJpaEntity buildJpaEntity() {
        return CompanyJpaEntity.builder()
            .id(id)
            .codeCompany(codeCompany)
            .xApplication(xApplication)
            .name(name)
            .legalName(legalName)
            .cnpj(cnpj)
            .stateRegistration(stateRegistration)
            .municipalRegistration(municipalRegistration)
            .taxRegime(taxRegime)
            .status(status)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }
    
    // Métodos estáticos para casos comuns
    public static Company createDefaultCompany() {
        return builder().buildDomain();
    }
    
    public static CompanyViewDTO createDefaultCompanyViewDTO() {
        return builder().buildView();
    }
    
    public static CompanyCreateCommandDTO createDefaultCreateCommand() {
        return builder().buildCreateCommand();
    }
    
    public static CompanyUpdateCommandDTO createDefaultUpdateCommand() {
        return builder().buildUpdateCommand();
    }
    
    public static CompanyUpdateDTO createDefaultUpdateDTO() {
        return builder().buildUpdateDTO();
    }

    public static Company createInactiveCompany() {
        return builder().withInactiveStatus().buildDomain();
    }
    
    public static CompanyViewDTO createInactiveCompanyViewDTO() {
        return builder().withInactiveStatus().buildView();
    }
    
    public static Company createTechCompany() {
        return builder().withTechCompany().buildDomain();
    }
    
    public static CompanyViewDTO createTechCompanyViewDTO() {
        return builder().withTechCompany().buildView();
    }
    
    public static Company createRetailCompany() {
        return builder().withRetailCompany().buildDomain();
    }
    
    public static Company createIndustrialCompany() {
        return builder().withIndustrialCompany().buildDomain();
    }
    
    public static Company createServiceCompany() {
        return builder().withServiceCompany().buildDomain();
    }
    
    public static Company createCompanyWithSimplesNacional() {
        return builder().withSimplesNacional().buildDomain();
    }
    
    public static Company createCompanyWithLucroPresumido() {
        return builder().withLucroPresumido().buildDomain();
    }
    
    public static Company createCompanyWithLucroReal() {
        return builder().withLucroReal().buildDomain();
    }
}
