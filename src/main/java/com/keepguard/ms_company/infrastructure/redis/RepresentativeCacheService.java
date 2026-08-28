package com.keepguard.ms_company.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepguard.ms_company.application.dto.representative.RepresentativeViewDTO;
import com.keepguard.ms_company.application.port.out.cache.RepresentativeCachePort;
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
public class RepresentativeCacheService implements RepresentativeCachePort {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${cache.redis.ttl.representative:2592000}")
    private long representativeTtlSeconds;

    @Value("${cache.redis.prefix.representative:representative_cache}")
    private String representativeCachePrefix;

    @CircuitBreaker(name = "redisCache")
    public void cacheRepresentativesByCompanyId(String companyId, List<RepresentativeViewDTO> representatives) {
        try {
            String key = companyKey(companyId);
            String value = objectMapper.writeValueAsString(representatives);
            redisTemplate.opsForValue().set(key, value, representativeTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear representantes | key={}", companyId);
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "getRepresentativesListFallback")
    @Retry(name = "redisCache")
    public List<RepresentativeViewDTO> getRepresentativesByCompanyIdFromCache(String companyId) {
        try {
            String key = companyKey(companyId);
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return objectMapper.readValue(value, objectMapper.getTypeFactory().constructCollectionType(List.class, RepresentativeViewDTO.class));
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void cacheActiveRepresentativeByCompanyId(String companyId, RepresentativeViewDTO representative) {
        try {
            String key = activeCompanyKey(companyId);
            String value = objectMapper.writeValueAsString(representative);
            redisTemplate.opsForValue().set(key, value, representativeTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Falha ao cachear representante ativo | key={}", companyId);
        }
    }

    @CircuitBreaker(name = "redisCache", fallbackMethod = "getRepresentativeFallback")
    @Retry(name = "redisCache")
    public RepresentativeViewDTO getActiveRepresentativeByCompanyIdFromCache(String companyId) {
        try {
            String key = activeCompanyKey(companyId);
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return objectMapper.readValue(value, RepresentativeViewDTO.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @CircuitBreaker(name = "redisCache")
    public void removeRepresentativesFromCacheByCompanyId(String companyId) {
        try {
            String key = companyKey(companyId);
            String activeKey = activeCompanyKey(companyId);
            redisTemplate.delete(key);
            redisTemplate.delete(activeKey);
        } catch (Exception e) {
            log.warn("Falha ao remover representantes do cache | key={}", companyId);
        }
    }

    private List<RepresentativeViewDTO> getRepresentativesListFallback(String companyId, Exception ex) {
        log.warn("FALLBACK: Redis indisponivel");
        return null;
    }

    private RepresentativeViewDTO getRepresentativeFallback(String companyId, Exception ex) {
        log.warn("FALLBACK: Redis indisponivel");
        return null;
    }

    private String basePrefix() {
        if (representativeCachePrefix == null || representativeCachePrefix.isBlank()) {
            return "representative_cache";
        }
        return representativeCachePrefix.replaceAll(":+$", "");
    }

    private String companyKey(String companyId) {
        return basePrefix() + ":company:" + normalize(companyId);
    }

    private String activeCompanyKey(String companyId) {
        return companyKey(companyId) + ":active";
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}

