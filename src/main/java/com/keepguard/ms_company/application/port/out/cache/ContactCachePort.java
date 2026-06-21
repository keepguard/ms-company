package com.keepguard.ms_company.application.port.out.cache;

import com.keepguard.ms_company.application.dto.contact.ContactViewDTO;

import java.util.List;

public interface ContactCachePort {

    void cacheContactsByCompanyId(String companyId, List<ContactViewDTO> contacts);
    List<ContactViewDTO> getContactsByCompanyIdFromCache(String companyId);
    
    void cacheActiveContactsByCompanyId(String companyId, List<ContactViewDTO> contacts);
    List<ContactViewDTO> getActiveContactsByCompanyIdFromCache(String companyId);
    
    void removeContactsFromCacheByCompanyId(String companyId);

}

