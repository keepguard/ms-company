package com.keepguard.ms_company.application.port.out.cache;

import com.keepguard.ms_company.application.dto.company.CompanyViewDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.response.CompanySimpleResponseDTO;

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

    void cacheCompanyByXApplication(String xApplication, CompanyViewDTO company);
    CompanyViewDTO getCompanyByXApplicationFromCache(String xApplication);
    void removeCompanyFromCacheByXApplication(String xApplication);

    void cacheSimpleCompanyById(String companyId, CompanySimpleResponseDTO company);
    CompanySimpleResponseDTO getSimpleCompanyByIdFromCache(String companyId);
    void removeSimpleCompanyFromCacheById(String companyId);

    void cacheSimpleCompanyByXApplication(String xApplication, CompanySimpleResponseDTO company);
    CompanySimpleResponseDTO getSimpleCompanyByXApplicationFromCache(String xApplication);
    void removeSimpleCompanyFromCacheByXApplication(String xApplication);

    void clearAllCompanyCache(String companyId, String cnpj, String codeCompany, String xApplication);
    void clearAllCompanyCacheById(String companyId);

}
