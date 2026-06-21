package com.keepguard.ms_company.application.port.out.cache;

import com.keepguard.ms_company.application.dto.bankaccount.BankAccountViewDTO;

import java.util.List;

public interface BankAccountCachePort {

    void cacheBankAccountsByCompanyId(String companyId, List<BankAccountViewDTO> bankAccounts);
    List<BankAccountViewDTO> getBankAccountsByCompanyIdFromCache(String companyId);
    
    void cacheActiveBankAccountByCompanyId(String companyId, BankAccountViewDTO bankAccount);
    BankAccountViewDTO getActiveBankAccountByCompanyIdFromCache(String companyId);
    
    void removeBankAccountsFromCacheByCompanyId(String companyId);

}

