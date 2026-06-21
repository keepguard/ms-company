package com.keepguard.ms_company.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepguard.ms_company.application.dto.contact.ContactViewDTO;
import com.keepguard.ms_company.application.port.out.cache.ContactCachePort;
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
public class ContactCacheService implements ContactCachePort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${cache.redis.ttl.contact:2592000}")
    private long contactTtlSeconds;

    @Value("${cache.redis.prefix.contact:contact_cache:}")
    private String contactCachePrefix;

    @CircuitBreaker(name = "redisCache")
    public void cacheContactsByCompanyId(String companyId, List<ContactViewDTO> contacts) {
        try {
            String key = contactCachePrefix + "company:" + companyId;
            String value = objectMapper.writeValueAsString(contacts);
            redisTemplate.opsForValue().set(key, value, contactTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear contatos | key={}", companyId);
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "getContactsListFallback")
    @Retry(name = "redisCache")
    public List<ContactViewDTO> getContactsByCompanyIdFromCache(String companyId) {
        try {
            String key = contactCachePrefix + "company:" + companyId;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return objectMapper.readValue(value, objectMapper.getTypeFactory().constructCollectionType(List.class, ContactViewDTO.class));
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void cacheActiveContactsByCompanyId(String companyId, List<ContactViewDTO> contacts) {
        try {
            String key = contactCachePrefix + "company:" + companyId + ":active";
            String value = objectMapper.writeValueAsString(contacts);
            redisTemplate.opsForValue().set(key, value, contactTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear contatos ativos | key={}", companyId);
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "getContactsListFallback")
    @Retry(name = "redisCache")
    public List<ContactViewDTO> getActiveContactsByCompanyIdFromCache(String companyId) {
        try {
            String key = contactCachePrefix + "company:" + companyId + ":active";
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return objectMapper.readValue(value, objectMapper.getTypeFactory().constructCollectionType(List.class, ContactViewDTO.class));
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void removeContactsFromCacheByCompanyId(String companyId) {
        try {
            String key = contactCachePrefix + "company:" + companyId;
            String activeKey = contactCachePrefix + "company:" + companyId + ":active";
            redisTemplate.delete(key);
            redisTemplate.delete(activeKey);
        } catch (Exception e) {
            log.warn("Falha ao remover contatos do cache | key={}", companyId);
        }
    }

    private List<ContactViewDTO> getContactsListFallback(String companyId, Exception ex) {
        log.warn("FALLBACK: Redis indisponivel");
        return null;
    }
}

