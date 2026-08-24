package com.keepguard.ms_company.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepguard.ms_company.application.dto.company.CompanyViewDTO;
import com.keepguard.ms_company.adapters.in.rest.company.dto.response.CompanySimpleResponseDTO;
import com.keepguard.ms_company.application.port.out.cache.CompanyCachePort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyCacheService implements CompanyCachePort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    private final AddressCacheService addressCacheService;
    private final BankAccountCacheService bankAccountCacheService;
    private final ContactCacheService contactCacheService;
    private final RepresentativeCacheService representativeCacheService;

    @Value("${cache.redis.ttl.company:2592000}")
    private long companyTtlSeconds;

    @Value("${cache.redis.prefix.company:company_cache:}")
    private String companyCachePrefix;

    @CircuitBreaker(name = "redisCache")
    public void cacheCompanyById(String companyId, CompanyViewDTO company) {
        try {
            String key = companyCachePrefix + "id:" + companyId;
            String value = objectMapper.writeValueAsString(company);
            redisTemplate.opsForValue().set(key, value, companyTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear empresa por ID | key={}", companyId);
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "getCompanyFallback")
    @Retry(name = "redisCache")
    public CompanyViewDTO getCompanyByIdFromCache(String companyId) {
        try {
            String key = companyCachePrefix + "id:" + companyId;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return objectMapper.readValue(value, CompanyViewDTO.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void cacheCompanyByCnpj(String cnpj, CompanyViewDTO company) {
        try {
            String key = companyCachePrefix + "cnpj:" + cnpj;
            String value = objectMapper.writeValueAsString(company);
            redisTemplate.opsForValue().set(key, value, companyTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear empresa por CNPJ | key={}", cnpj);
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "getCompanyFallback")
    @Retry(name = "redisCache")
    public CompanyViewDTO getCompanyByCnpjFromCache(String cnpj) {
        try {
            String key = companyCachePrefix + "cnpj:" + cnpj;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return objectMapper.readValue(value, CompanyViewDTO.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void cacheCompanyByCodeCompany(String codeCompany, CompanyViewDTO company) {
        try {
            String key = companyCachePrefix + "code:" + codeCompany;
            String value = objectMapper.writeValueAsString(company);
            redisTemplate.opsForValue().set(key, value, companyTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear empresa por CodeCompany | key={}", codeCompany);
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "getCompanyFallback")
    @Retry(name = "redisCache")
    public CompanyViewDTO getCompanyByCodeCompanyFromCache(String codeCompany) {
        try {
            String key = companyCachePrefix + "code:" + codeCompany;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return objectMapper.readValue(value, CompanyViewDTO.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void removeCompanyFromCacheById(String companyId) {
        try {
            String key = companyCachePrefix + "id:" + companyId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Falha ao remover empresa do cache por ID | key={}", companyId);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void removeCompanyFromCacheByCnpj(String cnpj) {
        try {
            String key = companyCachePrefix + "cnpj:" + cnpj;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Falha ao remover empresa do cache por CNPJ | key={}", cnpj);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void removeCompanyFromCacheByCodeCompany(String codeCompany) {
        try {
            String key = companyCachePrefix + "code:" + codeCompany;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Falha ao remover empresa do cache por CodeCompany | key={}", codeCompany);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void cacheCompanyByTenantId(String tenantId, CompanyViewDTO company) {
        try {
            String key = companyCachePrefix + "xapp:" + tenantId;
            String value = objectMapper.writeValueAsString(company);
            redisTemplate.opsForValue().set(key, value, companyTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear empresa por TenantId | key={}", tenantId);
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "getCompanyFallback")
    @Retry(name = "redisCache")
    public CompanyViewDTO getCompanyByTenantIdFromCache(String tenantId) {
        try {
            String key = companyCachePrefix + "xapp:" + tenantId;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return objectMapper.readValue(value, CompanyViewDTO.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void removeCompanyFromCacheByTenantId(String tenantId) {
        try {
            String key = companyCachePrefix + "xapp:" + tenantId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Falha ao remover empresa do cache por TenantId | key={}", tenantId);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void cacheSimpleCompanyById(String companyId, CompanySimpleResponseDTO company) {
        try {
            String key = companyCachePrefix + "simple:id:" + companyId;
            String value = objectMapper.writeValueAsString(company);
            redisTemplate.opsForValue().set(key, value, companyTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear empresa simples por ID | key={}", companyId);
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "getSimpleCompanyFallback")
    @Retry(name = "redisCache")
    public CompanySimpleResponseDTO getSimpleCompanyByIdFromCache(String companyId) {
        try {
            String key = companyCachePrefix + "simple:id:" + companyId;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return objectMapper.readValue(value, CompanySimpleResponseDTO.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void removeSimpleCompanyFromCacheById(String companyId) {
        try {
            String key = companyCachePrefix + "simple:id:" + companyId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Falha ao remover empresa simples do cache por ID | key={}", companyId);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void cacheSimpleCompanyByTenantId(String tenantId, CompanySimpleResponseDTO company) {
        try {
            String key = companyCachePrefix + "simple:xapp:" + tenantId;
            String value = objectMapper.writeValueAsString(company);
            redisTemplate.opsForValue().set(key, value, companyTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear empresa simples por TenantId | key={}", tenantId);
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "getSimpleCompanyFallback")
    @Retry(name = "redisCache")
    public CompanySimpleResponseDTO getSimpleCompanyByTenantIdFromCache(String tenantId) {
        try {
            String key = companyCachePrefix + "simple:xapp:" + tenantId;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return objectMapper.readValue(value, CompanySimpleResponseDTO.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void removeSimpleCompanyFromCacheByTenantId(String tenantId) {
        try {
            String key = companyCachePrefix + "simple:xapp:" + tenantId;
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Falha ao remover empresa simples do cache por TenantId | key={}", tenantId);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void clearAllCompanyCache(String companyId, String cnpj, String codeCompany, String tenantId) {
        try {
            removeCompanyFromCacheById(companyId);
            removeCompanyFromCacheByCnpj(cnpj);
            removeCompanyFromCacheByCodeCompany(codeCompany);
            removeCompanyFromCacheByTenantId(tenantId);
            removeSimpleCompanyFromCacheById(companyId);
            removeSimpleCompanyFromCacheByTenantId(tenantId);
            
            addressCacheService.removeAddressesFromCacheByCompanyId(companyId);
            bankAccountCacheService.removeBankAccountsFromCacheByCompanyId(companyId);
            contactCacheService.removeContactsFromCacheByCompanyId(companyId);
            representativeCacheService.removeRepresentativesFromCacheByCompanyId(companyId);
            
            log.info("Cache completo limpo para empresa | companyId={}", companyId);
        } catch (Exception e) {
            log.warn("Falha ao limpar cache completo da empresa | key={}", companyId);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void clearAllCompanyCacheById(String companyId) {
        try {
            removeCompanyFromCacheById(companyId);
            removeSimpleCompanyFromCacheById(companyId);
            
            addressCacheService.removeAddressesFromCacheByCompanyId(companyId);
            bankAccountCacheService.removeBankAccountsFromCacheByCompanyId(companyId);
            contactCacheService.removeContactsFromCacheByCompanyId(companyId);
            representativeCacheService.removeRepresentativesFromCacheByCompanyId(companyId);
            
            log.info("Cache limpo para empresa | companyId={}", companyId);
        } catch (Exception e) {
            log.warn("Falha ao limpar cache da empresa | key={}", companyId);
        }
    }

    private CompanyViewDTO getCompanyFallback(String param, Exception ex) {
        log.warn("FALLBACK: Redis indisponivel");
        return null;
    }

    private CompanySimpleResponseDTO getSimpleCompanyFallback(String param, Exception ex) {
        log.warn("FALLBACK: Redis indisponivel");
        return null;
    }
}
