package com.keepguard.ms_company.application.port.out.cache;

import com.keepguard.ms_company.application.dto.representative.RepresentativeViewDTO;

import java.util.List;

public interface RepresentativeCachePort {

    void cacheRepresentativesByCompanyId(String companyId, List<RepresentativeViewDTO> representatives);
    List<RepresentativeViewDTO> getRepresentativesByCompanyIdFromCache(String companyId);
    
    void cacheActiveRepresentativeByCompanyId(String companyId, RepresentativeViewDTO representative);
    RepresentativeViewDTO getActiveRepresentativeByCompanyIdFromCache(String companyId);
    
    void removeRepresentativesFromCacheByCompanyId(String companyId);

}

