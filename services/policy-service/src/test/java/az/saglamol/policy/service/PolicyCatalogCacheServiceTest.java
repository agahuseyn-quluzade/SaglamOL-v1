package az.saglamol.policy.service;

import az.saglamol.policy.entity.CoverageType;
import az.saglamol.policy.entity.InsuranceProduct;
import az.saglamol.policy.entity.InsuranceProductStatus;
import az.saglamol.policy.mapper.PolicyMapper;
import az.saglamol.policy.repository.CoverageRuleRepository;
import az.saglamol.policy.repository.InsuranceProductRepository;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(classes = PolicyCatalogCacheServiceTest.TestConfig.class)
class PolicyCatalogCacheServiceTest {

    @Autowired
    private PolicyCatalogCacheService cacheService;

    @Autowired
    private InsuranceProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void activeProductsCacheHitUsesRepositoryOnce() {
        clearCaches();
        UUID companyId = UUID.randomUUID();
        when(productRepository.findByInsuranceCompanyIdAndStatus(companyId, InsuranceProductStatus.ACTIVE))
                .thenReturn(List.of(product(UUID.randomUUID(), companyId)));

        assertEquals(1, cacheService.activeProductsByCompany(companyId).size());
        assertEquals(1, cacheService.activeProductsByCompany(companyId).size());

        verify(productRepository, times(1)).findByInsuranceCompanyIdAndStatus(companyId, InsuranceProductStatus.ACTIVE);
    }

    @Test
    void activeProductsCacheMissForDifferentCompanyQueriesRepositoryAgain() {
        clearCaches();
        UUID firstCompanyId = UUID.randomUUID();
        UUID secondCompanyId = UUID.randomUUID();
        when(productRepository.findByInsuranceCompanyIdAndStatus(firstCompanyId, InsuranceProductStatus.ACTIVE))
                .thenReturn(List.of(product(UUID.randomUUID(), firstCompanyId)));
        when(productRepository.findByInsuranceCompanyIdAndStatus(secondCompanyId, InsuranceProductStatus.ACTIVE))
                .thenReturn(List.of(product(UUID.randomUUID(), secondCompanyId)));

        cacheService.activeProductsByCompany(firstCompanyId);
        cacheService.activeProductsByCompany(secondCompanyId);

        verify(productRepository, times(1)).findByInsuranceCompanyIdAndStatus(firstCompanyId, InsuranceProductStatus.ACTIVE);
        verify(productRepository, times(1)).findByInsuranceCompanyIdAndStatus(secondCompanyId, InsuranceProductStatus.ACTIVE);
    }

    private void clearCaches() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }

    private InsuranceProduct product(UUID productId, UUID companyId) {
        Instant now = Instant.now();
        return new InsuranceProduct(
                productId,
                companyId,
                "P-" + productId.toString().substring(0, 8),
                "Standard Health",
                "Standard product",
                CoverageType.STANDARD,
                new BigDecimal("100.00"),
                new BigDecimal("10000.00"),
                "AZN",
                InsuranceProductStatus.ACTIVE,
                now,
                now
        );
    }

    @EnableCaching
    static class TestConfig {
        @Bean
        PolicyCatalogCacheService policyCatalogCacheService(
                InsuranceProductRepository productRepository,
                CoverageRuleRepository coverageRuleRepository,
                PolicyMapper policyMapper
        ) {
            return new PolicyCatalogCacheService(productRepository, coverageRuleRepository, policyMapper);
        }

        @Bean
        InsuranceProductRepository productRepository() {
            return mock(InsuranceProductRepository.class);
        }

        @Bean
        CoverageRuleRepository coverageRuleRepository() {
            return mock(CoverageRuleRepository.class);
        }

        @Bean
        PolicyMapper policyMapper() {
            return Mappers.getMapper(PolicyMapper.class);
        }

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("activeProductsByCompany", "allActiveProducts", "coverageRulesByProduct");
        }
    }
}
