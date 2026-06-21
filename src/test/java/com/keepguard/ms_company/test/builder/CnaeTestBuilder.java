package com.keepguard.ms_company.test.builder;

import com.keepguard.ms_company.application.dto.cnae.CnaeCreateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeUpdateCommandDTO;
import com.keepguard.ms_company.application.dto.cnae.CnaeViewDTO;
import com.keepguard.ms_company.domain.entity.Cnae;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Builder para criação de dados de teste para CNAE
 * Facilita a criação de objetos de teste com dados padrão
 */
public class CnaeTestBuilder {
    
    private UUID id = UUID.randomUUID();
    private UUID companyId = UUID.randomUUID();
    private String code = "1234567";
    private String description = "Atividade de desenvolvimento de software";
    private String section = "J";
    private String division = "62";
    private String groupCode = "620";
    private String classCode = "6201";
    private String subclassCode = "62015";
    private boolean active = true;
    private boolean principal = false;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public static CnaeTestBuilder builder() {
        return new CnaeTestBuilder();
    }
    
    public CnaeTestBuilder withId(UUID id) {
        this.id = id;
        return this;
    }
    
    public CnaeTestBuilder withCompanyId(UUID companyId) {
        this.companyId = companyId;
        return this;
    }
    
    public CnaeTestBuilder withCode(String code) {
        this.code = code;
        return this;
    }
    
    public CnaeTestBuilder withDescription(String description) {
        this.description = description;
        return this;
    }
    
    public CnaeTestBuilder withSection(String section) {
        this.section = section;
        return this;
    }
    
    public CnaeTestBuilder withDivision(String division) {
        this.division = division;
        return this;
    }
    
    public CnaeTestBuilder withGroupCode(String groupCode) {
        this.groupCode = groupCode;
        return this;
    }
    
    public CnaeTestBuilder withClassCode(String classCode) {
        this.classCode = classCode;
        return this;
    }
    
    public CnaeTestBuilder withSubclassCode(String subclassCode) {
        this.subclassCode = subclassCode;
        return this;
    }
    
    public CnaeTestBuilder withActive(boolean active) {
        this.active = active;
        return this;
    }
    
    public CnaeTestBuilder withPrincipal(boolean principal) {
        this.principal = principal;
        return this;
    }
    
    public CnaeTestBuilder inactive() {
        this.active = false;
        return this;
    }
    
    public CnaeTestBuilder asPrincipal() {
        this.principal = true;
        return this;
    }
    
    public CnaeTestBuilder withComercialActivity() {
        this.code = "4711301";
        this.description = "Comércio varejista de mercadorias em geral";
        this.section = "G";
        this.division = "47";
        this.groupCode = "471";
        this.classCode = "4711";
        this.subclassCode = "47113";
        return this;
    }
    
    public CnaeTestBuilder withIndustrialActivity() {
        this.code = "1011201";
        this.description = "Frigorífico - abate de bovinos";
        this.section = "A";
        this.division = "10";
        this.groupCode = "101";
        this.classCode = "1011";
        this.subclassCode = "10112";
        return this;
    }
    
    public CnaeTestBuilder withServiceActivity() {
        this.code = "6201500";
        this.description = "Desenvolvimento de programas de computador sob encomenda";
        this.section = "J";
        this.division = "62";
        this.groupCode = "620";
        this.classCode = "6201";
        this.subclassCode = "62015";
        return this;
    }
    
    public Cnae buildDomain() {
        return Cnae.of(
            id,
            code,
            description,
            section,
            division,
            groupCode,
            classCode,
            subclassCode,
            active,
            principal,
            companyId,
            createdAt,
            updatedAt
        );
    }
    
    public CnaeViewDTO buildView() {
        return new CnaeViewDTO(
            id,
            companyId,
            code,
            description,
            section,
            division,
            groupCode,
            classCode,
            subclassCode,
            active,
            principal,
            createdAt,
            updatedAt
        );
    }
    
    public CnaeCreateCommandDTO buildCreateCommand() {
        return new CnaeCreateCommandDTO(
            code,
            description,
            section,
            division,
            groupCode,
            classCode,
            subclassCode,
            principal,
            companyId
        );
    }
    
    public CnaeUpdateCommandDTO buildUpdateCommand() {
        return new CnaeUpdateCommandDTO(
            code,
            description,
            section,
            division,
            groupCode,
            classCode,
            subclassCode
        );
    }
    
    // Métodos estáticos para casos comuns
    public static Cnae createDefaultCnae() {
        return builder().buildDomain();
    }
    
    public static CnaeViewDTO createDefaultCnaeViewDTO() {
        return builder().buildView();
    }
    
    public static CnaeCreateCommandDTO createDefaultCreateCommand() {
        return builder().buildCreateCommand();
    }
    
    public static CnaeUpdateCommandDTO createDefaultUpdateCommand() {
        return builder().buildUpdateCommand();
    }
    
    public static Cnae createPrincipalCnae() {
        return builder().asPrincipal().buildDomain();
    }
    
    public static CnaeViewDTO createPrincipalCnaeViewDTO() {
        return builder().asPrincipal().buildView();
    }
    
    public static Cnae createInactiveCnae() {
        return builder().inactive().buildDomain();
    }
    
    public static CnaeViewDTO createInactiveCnaeViewDTO() {
        return builder().inactive().buildView();
    }
    
    public static Cnae createComercialCnae() {
        return builder().withComercialActivity().buildDomain();
    }
    
    public static Cnae createIndustrialCnae() {
        return builder().withIndustrialActivity().buildDomain();
    }
    
    public static Cnae createServiceCnae() {
        return builder().withServiceActivity().buildDomain();
    }
}
