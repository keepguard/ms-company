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
    @DisplayName("Deve gravar empresa na chave tenantId e não na chave xapp")
    void deveGravarEmpresaNaChaveTenantId() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(companyView)).thenReturn("{\"id\":\"1\"}");

        companyCacheService.cacheCompanyByTenantId(tenantId, companyView);

        verify(valueOperations).set(
                eq(PREFIX + "tenantId:" + tenantId),
                eq("{\"id\":\"1\"}"),
                eq(TTL),
                eq(TimeUnit.SECONDS));
        verify(valueOperations, never()).set(eq(PREFIX + "xapp:" + tenantId), anyString(), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Deve ler empresa pela chave tenantId")
    void deveLerEmpresaPelaChaveTenantId() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(PREFIX + "tenantId:" + tenantId)).thenReturn("{\"id\":\"1\"}");
        when(objectMapper.readValue("{\"id\":\"1\"}", CompanyViewDTO.class)).thenReturn(companyView);

        CompanyViewDTO result = companyCacheService.getCompanyByTenantIdFromCache(tenantId);

        assertThat(result).isEqualTo(companyView);
        verify(valueOperations).get(PREFIX + "tenantId:" + tenantId);
        verify(valueOperations, never()).get(PREFIX + "xapp:" + tenantId);
    }

    @Test
    @DisplayName("Deve fazer fallback para a chave xapp quando tenantId não existir")
    void deveFazerFallbackParaChaveLegadaXapp() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(PREFIX + "tenantId:" + tenantId)).thenReturn(null);
        when(valueOperations.get(PREFIX + "xapp:" + tenantId)).thenReturn("{\"id\":\"legacy\"}");
        when(objectMapper.readValue("{\"id\":\"legacy\"}", CompanyViewDTO.class)).thenReturn(companyView);

        CompanyViewDTO result = companyCacheService.getCompanyByTenantIdFromCache(tenantId);

        assertThat(result).isEqualTo(companyView);
        verify(valueOperations).get(PREFIX + "tenantId:" + tenantId);
        verify(valueOperations).get(PREFIX + "xapp:" + tenantId);
    }

    @Test
    @DisplayName("Deve apagar chaves tenantId e xapp ao remover por tenant")
    void deveApagarChavesAtualELegadaAoRemover() {
        companyCacheService.removeCompanyFromCacheByTenantId(tenantId);

        verify(redisTemplate).delete(PREFIX + "tenantId:" + tenantId);
        verify(redisTemplate).delete(PREFIX + "xapp:" + tenantId);
    }

    @Test
    @DisplayName("Deve gravar empresa simples na chave simple:tenantId")
    void deveGravarEmpresaSimplesNaChaveTenantId() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(simpleView)).thenReturn("{\"name\":\"Empresa\"}");

        companyCacheService.cacheSimpleCompanyByTenantId(tenantId, simpleView);

        verify(valueOperations).set(
                eq(PREFIX + "simple:tenantId:" + tenantId),
                eq("{\"name\":\"Empresa\"}"),
                eq(TTL),
                eq(TimeUnit.SECONDS));
        verify(valueOperations, never()).set(
                eq(PREFIX + "simple:xapp:" + tenantId),
                anyString(),
                anyLong(),
                eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Deve fazer fallback para simple:xapp quando simple:tenantId não existir")
    void deveFazerFallbackParaChaveSimplesLegada() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(PREFIX + "simple:tenantId:" + tenantId)).thenReturn(null);
        when(valueOperations.get(PREFIX + "simple:xapp:" + tenantId)).thenReturn("{\"name\":\"legacy\"}");
        when(objectMapper.readValue("{\"name\":\"legacy\"}", CompanySimpleResponseDTO.class)).thenReturn(simpleView);

        CompanySimpleResponseDTO result = companyCacheService.getSimpleCompanyByTenantIdFromCache(tenantId);

        assertThat(result).isEqualTo(simpleView);
        verify(valueOperations).get(PREFIX + "simple:tenantId:" + tenantId);
        verify(valueOperations).get(PREFIX + "simple:xapp:" + tenantId);
    }

    @Test
    @DisplayName("Deve apagar chaves simple:tenantId e simple:xapp ao remover")
    void deveApagarChavesSimplesAtualELegadaAoRemover() {
        companyCacheService.removeSimpleCompanyFromCacheByTenantId(tenantId);

        verify(redisTemplate).delete(PREFIX + "simple:tenantId:" + tenantId);
        verify(redisTemplate).delete(PREFIX + "simple:xapp:" + tenantId);
    }
}
