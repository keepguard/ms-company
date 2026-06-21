package com.keepguard.ms_company.application.port.out.cache;

import com.keepguard.ms_company.application.dto.address.AddressViewDTO;

import java.util.List;

public interface AddressCachePort {

    void cacheAddressesByCompanyId(String companyId, List<AddressViewDTO> addresses);
    List<AddressViewDTO> getAddressesByCompanyIdFromCache(String companyId);
    
    void cacheActiveAddressByCompanyId(String companyId, AddressViewDTO address);
    AddressViewDTO getActiveAddressByCompanyIdFromCache(String companyId);
    
    void removeAddressesFromCacheByCompanyId(String companyId);

}

