package com.keepguard.ms_company.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepguard.ms_company.application.dto.bankaccount.BankAccountViewDTO;
import com.keepguard.ms_company.application.port.out.cache.BankAccountCachePort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankAccountCacheService implements BankAccountCachePort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${cache.redis.ttl.bank-account:2592000}")
    private long bankAccountTtlSeconds;

    @Value("${cache.redis.prefix.bank-account:bank_account_cache:}")
    private String bankAccountCachePrefix;

    @CircuitBreaker(name = "redisCache")
    public void cacheBankAccountsByCompanyId(String companyId, List<BankAccountViewDTO> bankAccounts) {
        try {
            String key = bankAccountCachePrefix + "company:" + companyId;
            String value = objectMapper.writeValueAsString(bankAccounts);
            redisTemplate.opsForValue().set(key, value, bankAccountTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear dados bancários | key={}", companyId);
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "getBankAccountsListFallback")
    @Retry(name = "redisCache")
    public List<BankAccountViewDTO> getBankAccountsByCompanyIdFromCache(String companyId) {
        try {
            String key = bankAccountCachePrefix + "company:" + companyId;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return objectMapper.readValue(value, objectMapper.getTypeFactory().constructCollectionType(List.class, BankAccountViewDTO.class));
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void cacheActiveBankAccountByCompanyId(String companyId, BankAccountViewDTO bankAccount) {
        try {
            String key = bankAccountCachePrefix + "company:" + companyId + ":active";
            String value = objectMapper.writeValueAsString(bankAccount);
            redisTemplate.opsForValue().set(key, value, bankAccountTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear dado bancário ativo | key={}", companyId);
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "getBankAccountFallback")
    @Retry(name = "redisCache")
    public BankAccountViewDTO getActiveBankAccountByCompanyIdFromCache(String companyId) {
        try {
            String key = bankAccountCachePrefix + "company:" + companyId + ":active";
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return objectMapper.readValue(value, BankAccountViewDTO.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void removeBankAccountsFromCacheByCompanyId(String companyId) {
        try {
            String key = bankAccountCachePrefix + "company:" + companyId;
            String activeKey = bankAccountCachePrefix + "company:" + companyId + ":active";
            redisTemplate.delete(key);
            redisTemplate.delete(activeKey);
        } catch (Exception e) {
            log.warn("Falha ao remover dados bancários do cache | key={}", companyId);
        }
    }

    private List<BankAccountViewDTO> getBankAccountsListFallback(String companyId, Exception ex) {
        log.warn("FALLBACK: Redis indisponivel");
        return null;
    }

    private BankAccountViewDTO getBankAccountFallback(String companyId, Exception ex) {
        log.warn("FALLBACK: Redis indisponivel");
        return null;
    }
}

