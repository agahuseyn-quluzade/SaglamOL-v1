package az.saglamol.policy.service;

import az.saglamol.policy.dto.response.CoverageRuleResponse;
import az.saglamol.policy.dto.response.InsuranceProductResponse;
import az.saglamol.policy.entity.InsuranceProductStatus;
import az.saglamol.policy.mapper.PolicyMapper;
import az.saglamol.policy.repository.CoverageRuleRepository;
import az.saglamol.policy.repository.InsuranceProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PolicyCatalogCacheService {

    private final InsuranceProductRepository productRepository;
    private final CoverageRuleRepository coverageRuleRepository;
    private final PolicyMapper policyMapper;

    public PolicyCatalogCacheService(
            InsuranceProductRepository productRepository,
            CoverageRuleRepository coverageRuleRepository,
            PolicyMapper policyMapper
    ) {
        this.productRepository = productRepository;
        this.coverageRuleRepository = coverageRuleRepository;
        this.policyMapper = policyMapper;
    }

    @Cacheable(cacheNames = "activeProductsByCompany", key = "#companyId")
    @Transactional(readOnly = true)
    public List<InsuranceProductResponse> activeProductsByCompany(UUID companyId) {
        return productRepository.findByInsuranceCompanyIdAndStatus(companyId, InsuranceProductStatus.ACTIVE).stream()
                .map(policyMapper::toResponse)
                .toList();
    }

    @Cacheable(cacheNames = "allActiveProducts")
    @Transactional(readOnly = true)
    public List<InsuranceProductResponse> allActiveProducts() {
        return productRepository.findByStatus(InsuranceProductStatus.ACTIVE).stream()
                .map(policyMapper::toResponse)
                .toList();
    }

    @Cacheable(cacheNames = "coverageRulesByProduct", key = "#productId")
    @Transactional(readOnly = true)
    public List<CoverageRuleResponse> coverageRulesByProduct(UUID productId) {
        return coverageRuleRepository.findByProductId(productId).stream()
                .map(policyMapper::toResponse)
                .toList();
    }

    @CacheEvict(cacheNames = {"activeProductsByCompany"}, key = "#companyId")
    public void evictActiveProductsByCompany(UUID companyId) {
    }

    @CacheEvict(cacheNames = "allActiveProducts", allEntries = true)
    public void evictAllActiveProducts() {
    }

    @CacheEvict(cacheNames = "coverageRulesByProduct", key = "#productId")
    public void evictCoverageRules(UUID productId) {
    }
}
