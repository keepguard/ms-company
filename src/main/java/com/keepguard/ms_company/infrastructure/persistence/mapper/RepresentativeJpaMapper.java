package com.keepguard.ms_company.infrastructure.persistence.mapper;

import com.keepguard.ms_company.domain.entity.Representative;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyRepresentativeJpaEntity;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyJpaEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RepresentativeJpaMapper {

    public CompanyRepresentativeJpaEntity toJpaEntity(Representative representative) {
        if (representative == null) {
            return null;
        }

        return CompanyRepresentativeJpaEntity.builder()
            .id(representative.getId())
            .name(representative.getName())
            .cpf(representative.getCpf())
            .rg(representative.getRg())
            .birthDate(representative.getBirthDate())
            .email(representative.getEmail())
            .phone(representative.getPhone())
            .role(representative.getRole())
            .active(representative.isActive())
            .build();
    }

    public CompanyRepresentativeJpaEntity toJpaEntity(Representative representative, UUID companyId) {
        if (representative == null) {
            return null;
        }

        CompanyJpaEntity company = CompanyJpaEntity.builder()
            .id(companyId)
            .build();

        return CompanyRepresentativeJpaEntity.builder()
            .id(representative.getId())
            .company(company)
            .name(representative.getName())
            .cpf(representative.getCpf())
            .rg(representative.getRg())
            .birthDate(representative.getBirthDate())
            .email(representative.getEmail())
            .phone(representative.getPhone())
            .role(representative.getRole())
            .active(representative.isActive())
            .build();
    }

    public Representative toDomain(CompanyRepresentativeJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        return Representative.of(
            jpaEntity.getId(),
            jpaEntity.getName(),
            jpaEntity.getCpf(),
            jpaEntity.getRg(),
            jpaEntity.getBirthDate(),
            jpaEntity.getEmail(),
            jpaEntity.getPhone(),
            jpaEntity.getRole(),
            jpaEntity.getActive()
        );
    }
}
