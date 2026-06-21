package com.keepguard.ms_company.infrastructure.persistence;

import com.keepguard.ms_company.domain.entity.Representative;
import com.keepguard.ms_company.application.port.out.persistence.RepresentativeRepositoryPort;
import com.keepguard.ms_company.infrastructure.persistence.entity.CompanyRepresentativeJpaEntity;
import com.keepguard.ms_company.infrastructure.persistence.mapper.RepresentativeJpaMapper;
import com.keepguard.ms_company.infrastructure.persistence.spring.RepresentativeSpringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;

@Repository
@RequiredArgsConstructor
@Retry(name = "databaseOperation")
@Bulkhead(name = "databaseOperation")
public class RepresentativeRepositoryAdapter implements RepresentativeRepositoryPort {

    private final RepresentativeSpringRepository representativeSpringRepository;
    private final RepresentativeJpaMapper representativeJpaMapper;

    @Override
    public Representative save(Representative representative) {
        // Para updates, precisamos carregar a entidade existente para manter a relação com a empresa
        Optional<CompanyRepresentativeJpaEntity> existingEntity = representativeSpringRepository.findById(representative.getId());

        if (existingEntity.isPresent()) {
            // Update: mantém a relação com a empresa
            CompanyRepresentativeJpaEntity existing = existingEntity.get();
            existing.setName(representative.getName());
            existing.setCpf(representative.getCpf());
            existing.setRg(representative.getRg());
            existing.setBirthDate(representative.getBirthDate());
            existing.setEmail(representative.getEmail());
            existing.setPhone(representative.getPhone());
            existing.setRole(representative.getRole());
            existing.setActive(representative.isActive());

            CompanyRepresentativeJpaEntity savedEntity = representativeSpringRepository.save(existing);
            return representativeJpaMapper.toDomain(savedEntity);
        } else {
            // Create: nova entidade
            CompanyRepresentativeJpaEntity jpaEntity = representativeJpaMapper.toJpaEntity(representative);
            CompanyRepresentativeJpaEntity savedEntity = representativeSpringRepository.save(jpaEntity);
            return representativeJpaMapper.toDomain(savedEntity);
        }
    }

    @Override
    public Representative save(Representative representative, UUID companyId) {
        CompanyRepresentativeJpaEntity jpaEntity = representativeJpaMapper.toJpaEntity(representative, companyId);
        CompanyRepresentativeJpaEntity savedEntity = representativeSpringRepository.save(jpaEntity);
        return representativeJpaMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Representative> findById(UUID id) {
        return representativeSpringRepository.findById(id)
            .map(representativeJpaMapper::toDomain);
    }

    @Override
    public List<Representative> findAll() {
        return representativeSpringRepository.findAll().stream()
            .map(representativeJpaMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        representativeSpringRepository.deleteById(id);
    }

    @Override
    public void delete(Representative representative) {
        CompanyRepresentativeJpaEntity jpaEntity = representativeJpaMapper.toJpaEntity(representative);
        representativeSpringRepository.delete(jpaEntity);
    }

    @Override
    public List<Representative> findByCompanyId(UUID companyId) {
        return representativeSpringRepository.findByCompanyId(companyId).stream()
            .map(representativeJpaMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Representative> findActiveByCompanyId(UUID companyId) {
        return representativeSpringRepository.findFirstByCompanyIdAndActiveTrue(companyId)
            .map(representativeJpaMapper::toDomain);
    }

    @Override
    public List<Representative> findAllActive() {
        return representativeSpringRepository.findByActiveTrue().stream()
            .map(representativeJpaMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(UUID id) {
        return representativeSpringRepository.existsById(id);
    }

    @Override
    public boolean existsByCompanyIdAndCpf(UUID companyId, String cpf) {
        return representativeSpringRepository.existsByCompanyIdAndCpf(companyId, cpf);
    }

    @Override
    public Optional<Representative> findByCompanyIdAndCpf(UUID companyId, String cpf) {
        return representativeSpringRepository.findByCompanyIdAndCpf(companyId, cpf)
            .map(representativeJpaMapper::toDomain);
    }

    @Override
    public Optional<Representative> findByCpf(String cpf) {
        return representativeSpringRepository.findByCpf(cpf)
            .map(representativeJpaMapper::toDomain);
    }

    @Override
    public Optional<Representative> findByEmail(String email) {
        return representativeSpringRepository.findByEmail(email)
            .map(representativeJpaMapper::toDomain);
    }

    @Override
    public List<Representative> findByNameContainingIgnoreCase(String name) {
        return representativeSpringRepository.findByNameContainingIgnoreCase(name).stream()
            .map(representativeJpaMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Representative> findByRoleContainingIgnoreCase(String role) {
        return representativeSpringRepository.findByRoleContainingIgnoreCase(role).stream()
            .map(representativeJpaMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public long countActiveByCompanyId(UUID companyId) {
        return representativeSpringRepository.countByCompanyIdAndActiveTrue(companyId);
    }

    @Override
    public Optional<UUID> findCompanyIdByRepresentativeId(UUID representativeId) {
        return representativeSpringRepository.findCompanyIdByRepresentativeId(representativeId);
    }
}
