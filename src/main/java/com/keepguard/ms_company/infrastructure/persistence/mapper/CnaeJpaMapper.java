package com.keepguard.ms_company.infrastructure.persistence.mapper;

import com.keepguard.ms_company.domain.entity.Cnae;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyCnaeJpaEntity;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyJpaEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CnaeJpaMapper {

    public Cnae toDomain(CompanyCnaeJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Cnae.of(
            entity.getId(),
            entity.getCode(),
            entity.getDescription(),
            entity.getSection(),
            entity.getDivision(),
            entity.getGroupCode(),
            entity.getClassCode(),
            entity.getSubclassCode(),
            entity.getActive(),
            entity.getPrincipal(),
            entity.getCompany() != null ? entity.getCompany().getId() : UUID.randomUUID(), // Usar UUID temporário se não houver empresa
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public CompanyCnaeJpaEntity toEntity(Cnae cnae) {
        if (cnae == null) {
            return null;
        }

        return CompanyCnaeJpaEntity.builder()
            .id(cnae.getId())
            .code(cnae.getCode())
            .description(cnae.getDescription())
            .section(cnae.getSection())
            .division(cnae.getDivision())
            .groupCode(cnae.getGroupCode())
            .classCode(cnae.getClassCode())
            .subclassCode(cnae.getSubclassCode())
            .active(cnae.isActive())
            .principal(cnae.isPrincipal())
            .createdAt(cnae.getCreatedAt())
            .updatedAt(cnae.getUpdatedAt())
            .build();
    }

    public CompanyCnaeJpaEntity toEntity(Cnae cnae, UUID companyId) {
        if (cnae == null) {
            return null;
        }

        CompanyJpaEntity company = new CompanyJpaEntity();
        company.setId(companyId);

        return CompanyCnaeJpaEntity.builder()
            .id(cnae.getId())
            .company(company)
            .code(cnae.getCode())
            .description(cnae.getDescription())
            .section(cnae.getSection())
            .division(cnae.getDivision())
            .groupCode(cnae.getGroupCode())
            .classCode(cnae.getClassCode())
            .subclassCode(cnae.getSubclassCode())
            .active(cnae.isActive())
            .principal(cnae.isPrincipal())
            .createdAt(cnae.getCreatedAt())
            .updatedAt(cnae.getUpdatedAt())
            .build();
    }

    public CompanyCnaeJpaEntity toJpaEntity(Cnae cnae) {
        return toEntity(cnae);
    }
}
