package com.keepguard.ms_company.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepguard.ms_company.adapters.in.rest.company.dto.response.CompanySimpleResponseDTO;
import com.keepguard.ms_company.application.dto.company.CompanyViewDTO;
import com.keepguard.ms_company.test.builder.CompanyTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyCacheService - chaves por tenantId")
class CompanyCacheServiceTest {

    private static final String PREFIX = "company_cache:";
    private static final long TTL = 2592000L;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private AddressCacheService addressCacheService;

    @Mock
    private BankAccountCacheService bankAccountCacheService;

    @Mock
    private ContactCacheService contactCacheService;

    @Mock
    private RepresentativeCacheService representativeCacheService;

    @InjectMocks
    private CompanyCacheService companyCacheService;

    private String tenantId;
    private CompanyViewDTO companyView;
    private CompanySimpleResponseDTO simpleView;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID().toString();
        companyView = CompanyTestBuilder.createDefaultCompanyViewDTO();
        simpleView = CompanySimpleResponseDTO.builder()
                .id(companyView.id())
                .tenantId(UUID.fromString(tenantId))
                .name(companyView.name())
                .build();

        ReflectionTestUtils.setField(companyCacheService, "companyTtlSeconds", TTL);
        ReflectionTestUtils.setField(companyCacheService, "companyCachePrefix", PREFIX);
    }

    @Test
    @DisplayName("Deve gravar empresa na chave tenantId")
    void deveGravarEmpresaNaChaveTenantId() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(companyView)).thenReturn("{\"id\":\"1\"}");

        companyCacheService.cacheCompanyByTenantId(tenantId, companyView);

        verify(valueOperations).set(
                eq("company_cache:tenantId:" + tenantId),
                eq("{\"id\":\"1\"}"),
                eq(TTL),
                eq(TimeUnit.SECONDS));
        verify(valueOperations, never()).set(eq("company_cache:xapp:" + tenantId), anyString(), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Deve ler empresa pela chave tenantId")
    void deveLerEmpresaPelaChaveTenantId() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("company_cache:tenantId:" + tenantId)).thenReturn("{\"id\":\"1\"}");
        when(objectMapper.readValue("{\"id\":\"1\"}", CompanyViewDTO.class)).thenReturn(companyView);

        CompanyViewDTO result = companyCacheService.getCompanyByTenantIdFromCache(tenantId);

        assertThat(result).isEqualTo(companyView);
        verify(valueOperations).get("company_cache:tenantId:" + tenantId);
        verify(valueOperations, never()).get("company_cache:xapp:" + tenantId);
    }

    @Test
    @DisplayName("Deve normalizar tenantId para lowercase na chave")
    void deveNormalizarTenantIdParaLowercase() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(companyView)).thenReturn("{\"id\":\"1\"}");

        companyCacheService.cacheCompanyByTenantId("TENANT-ABC", companyView);

        verify(valueOperations).set(
                eq("company_cache:tenantId:tenant-abc"),
                eq("{\"id\":\"1\"}"),
                eq(TTL),
                eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Deve gravar CNPJ somente com dígitos na chave")
    void deveGravarCnpjSomenteComDigitos() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(companyView)).thenReturn("{\"id\":\"1\"}");

        companyCacheService.cacheCompanyByCnpj("12.345.678/0001-90", companyView);

        verify(valueOperations).set(
                eq("company_cache:cnpj:12345678000190"),
                eq("{\"id\":\"1\"}"),
                eq(TTL),
                eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Deve apagar apenas a chave tenantId ao remover por tenant")
    void deveApagarChaveTenantIdAoRemover() {
        companyCacheService.removeCompanyFromCacheByTenantId(tenantId);

        verify(redisTemplate).delete("company_cache:tenantId:" + tenantId);
        verify(redisTemplate, never()).delete("company_cache:xapp:" + tenantId);
    }

    @Test
    @DisplayName("Deve gravar empresa simples na chave simple:tenantId")
    void deveGravarEmpresaSimplesNaChaveTenantId() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(simpleView)).thenReturn("{\"name\":\"Empresa\"}");

        companyCacheService.cacheSimpleCompanyByTenantId(tenantId, simpleView);

        verify(valueOperations).set(
                eq("company_cache:simple:tenantId:" + tenantId),
                eq("{\"name\":\"Empresa\"}"),
                eq(TTL),
                eq(TimeUnit.SECONDS));
        verify(valueOperations, never()).set(
                eq("company_cache:simple:xapp:" + tenantId),
                anyString(),
                anyLong(),
                eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Deve apagar apenas a chave simple:tenantId ao remover")
    void deveApagarChaveSimplesAoRemover() {
        companyCacheService.removeSimpleCompanyFromCacheByTenantId(tenantId);

        verify(redisTemplate).delete("company_cache:simple:tenantId:" + tenantId);
        verify(redisTemplate, never()).delete("company_cache:simple:xapp:" + tenantId);
    }
}
