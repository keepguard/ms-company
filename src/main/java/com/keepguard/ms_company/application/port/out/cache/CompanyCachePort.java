package com.keepguard.ms_company.application.port.out.cache;

import com.keepguard.ms_company.application.dto.company.CompanySimpleViewDTO;
import com.keepguard.ms_company.application.dto.company.CompanyViewDTO;

public interface CompanyCachePort {

    void cacheCompanyById(String companyId, CompanyViewDTO company);
    CompanyViewDTO getCompanyByIdFromCache(String companyId);
    void removeCompanyFromCacheById(String companyId);

    void cacheCompanyByCnpj(String cnpj, CompanyViewDTO company);
    CompanyViewDTO getCompanyByCnpjFromCache(String cnpj);
    void removeCompanyFromCacheByCnpj(String cnpj);

    void cacheCompanyByCodeCompany(String codeCompany, CompanyViewDTO company);
    CompanyViewDTO getCompanyByCodeCompanyFromCache(String codeCompany);
    void removeCompanyFromCacheByCodeCompany(String codeCompany);

    void cacheCompanyByTenantId(String tenantId, CompanyViewDTO company);
    CompanyViewDTO getCompanyByTenantIdFromCache(String tenantId);
    void removeCompanyFromCacheByTenantId(String tenantId);

    void cacheSimpleCompanyById(String companyId, CompanySimpleViewDTO company);
    CompanySimpleViewDTO getSimpleCompanyByIdFromCache(String companyId);
    void removeSimpleCompanyFromCacheById(String companyId);

    void cacheSimpleCompanyByTenantId(String tenantId, CompanySimpleViewDTO company);
    CompanySimpleViewDTO getSimpleCompanyByTenantIdFromCache(String tenantId);
    void removeSimpleCompanyFromCacheByTenantId(String tenantId);

    void clearAllCompanyCache(String companyId, String cnpj, String codeCompany, String tenantId);
    void clearAllCompanyCacheById(String companyId);

}
