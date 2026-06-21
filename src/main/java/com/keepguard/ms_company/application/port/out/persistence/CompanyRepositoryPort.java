package com.keepguard.ms_company.application.port.out.persistence;

import com.keepguard.ms_company.application.dto.company.CompanySearchCriteriaDTO;
import com.keepguard.ms_company.application.dto.common.PageResultDTO;
import com.keepguard.ms_company.domain.entity.Company;
import com.keepguard.ms_company.domain.enums.CompanyStatusEnum;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyRepositoryPort {

    Company save(Company company);

    Optional<Company> findById(UUID id);

    List<Company> findAll();

    void deleteById(UUID id);

    void delete(Company company);

    Optional<Company> findByCnpj(String cnpj);

    Optional<Company> findByCodeCompany(UUID codeCompany);

    Optional<Company> findByXApplication(UUID xApplication);

    List<Company> findAllByStatus(CompanyStatusEnum status);

    boolean existsByCnpj(String cnpj);

    List<Company> findByLegalNameContainingIgnoreCase(String legalName);

    List<Company> findByNameContainingIgnoreCase(String name);

    PageResultDTO<Company> search(CompanySearchCriteriaDTO criteria);
}

