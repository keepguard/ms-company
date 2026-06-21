package com.keepguard.ms_company.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepguard.ms_company.application.dto.address.AddressViewDTO;
import com.keepguard.ms_company.application.port.out.cache.AddressCachePort;
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
public class AddressCacheService implements AddressCachePort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${cache.redis.ttl.address:2592000}")
    private long addressTtlSeconds;

    @Value("${cache.redis.prefix.address:address_cache:}")
    private String addressCachePrefix;

    @CircuitBreaker(name = "redisCache")
    public void cacheAddressesByCompanyId(String companyId, List<AddressViewDTO> addresses) {
        try {
            String key = addressCachePrefix + "company:" + companyId;
            String value = objectMapper.writeValueAsString(addresses);
            redisTemplate.opsForValue().set(key, value, addressTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear endereços | key={}", companyId);
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "getAddressesListFallback")
    @Retry(name = "redisCache")
    public List<AddressViewDTO> getAddressesByCompanyIdFromCache(String companyId) {
        try {
            String key = addressCachePrefix + "company:" + companyId;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return objectMapper.readValue(value, objectMapper.getTypeFactory().constructCollectionType(List.class, AddressViewDTO.class));
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void cacheActiveAddressByCompanyId(String companyId, AddressViewDTO address) {
        try {
            String key = addressCachePrefix + "company:" + companyId + ":active";
            String value = objectMapper.writeValueAsString(address);
            redisTemplate.opsForValue().set(key, value, addressTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear endereço ativo | key={}", companyId);
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "getAddressFallback")
    @Retry(name = "redisCache")
    public AddressViewDTO getActiveAddressByCompanyIdFromCache(String companyId) {
        try {
            String key = addressCachePrefix + "company:" + companyId + ":active";
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return objectMapper.readValue(value, AddressViewDTO.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void removeAddressesFromCacheByCompanyId(String companyId) {
        try {
            String key = addressCachePrefix + "company:" + companyId;
            String activeKey = addressCachePrefix + "company:" + companyId + ":active";
            redisTemplate.delete(key);
            redisTemplate.delete(activeKey);
        } catch (Exception e) {
            log.warn("Falha ao remover endereços do cache | key={}", companyId);
        }
    }

    private List<AddressViewDTO> getAddressesListFallback(String companyId, Exception ex) {
        log.warn("FALLBACK: Redis indisponivel");
        return null;
    }

    private AddressViewDTO getAddressFallback(String companyId, Exception ex) {
        log.warn("FALLBACK: Redis indisponivel");
        return null;
    }
}

