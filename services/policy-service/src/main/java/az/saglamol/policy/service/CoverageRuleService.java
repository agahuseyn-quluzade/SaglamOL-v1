package az.saglamol.policy.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.policy.dto.request.CreateCoverageRuleRequest;
import az.saglamol.policy.dto.request.UpdateCoverageRuleRequest;
import az.saglamol.policy.dto.response.CoverageRuleResponse;
import az.saglamol.policy.entity.CoverageRule;
import az.saglamol.policy.entity.InsuranceProduct;
import az.saglamol.policy.entity.RuleStatus;
import az.saglamol.policy.exception.PolicyException;
import az.saglamol.policy.mapper.PolicyMapper;
import az.saglamol.policy.repository.CoverageRuleRepository;
import az.saglamol.policy.repository.InsuranceProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CoverageRuleService {

    private final CoverageRuleRepository coverageRuleRepository;
    private final InsuranceProductRepository productRepository;
    private final PolicyMapper policyMapper;
    private final PolicyAccessService accessService;
    private final PolicyCatalogCacheService cacheService;

    public CoverageRuleService(
            CoverageRuleRepository coverageRuleRepository,
            InsuranceProductRepository productRepository,
            PolicyMapper policyMapper,
            PolicyAccessService accessService,
            PolicyCatalogCacheService cacheService
    ) {
        this.coverageRuleRepository = coverageRuleRepository;
        this.productRepository = productRepository;
        this.policyMapper = policyMapper;
        this.accessService = accessService;
        this.cacheService = cacheService;
    }

    @Transactional
    public CoverageRuleResponse addCoverageRule(AuthContext authContext, UUID productId, CreateCoverageRuleRequest request) {
        InsuranceProduct product = productById(productId);
        accessService.requireCanManageProduct(authContext, product);
        validateCoverage(request.coveragePercent(), request.maxAmount());
        if (coverageRuleRepository.findByProductIdAndServiceType(productId, request.serviceType()).isPresent()) {
            throw new PolicyException("COVERAGE_RULE_ALREADY_EXISTS", "Coverage rule already exists for service type");
        }
        Instant now = Instant.now();
        CoverageRule rule = new CoverageRule(
                UUID.randomUUID(),
                productId,
                request.serviceType(),
                request.coveragePercent(),
                request.maxAmount(),
                request.waitingPeriodDays(),
                request.requiresPreApproval(),
                RuleStatus.ACTIVE,
                now,
                now
        );
        CoverageRule saved = coverageRuleRepository.save(rule);
        cacheService.evictCoverageRules(productId);
        return policyMapper.toResponse(saved);
    }

    @Transactional
    public CoverageRuleResponse updateCoverageRule(AuthContext authContext, UUID ruleId, UpdateCoverageRuleRequest request) {
        CoverageRule rule = ruleById(ruleId);
        InsuranceProduct product = productById(rule.getProductId());
        accessService.requireCanManageProduct(authContext, product);
        validateCoverage(request.coveragePercent(), request.maxAmount());
        rule.update(
                request.coveragePercent(),
                request.maxAmount(),
                request.waitingPeriodDays(),
                request.requiresPreApproval(),
                Instant.now()
        );
        cacheService.evictCoverageRules(rule.getProductId());
        return policyMapper.toResponse(rule);
    }

    @Transactional
    public CoverageRuleResponse changeStatus(AuthContext authContext, UUID ruleId, RuleStatus status) {
        CoverageRule rule = ruleById(ruleId);
        InsuranceProduct product = productById(rule.getProductId());
        accessService.requireCanManageProduct(authContext, product);
        rule.changeStatus(status, Instant.now());
        cacheService.evictCoverageRules(rule.getProductId());
        return policyMapper.toResponse(rule);
    }

    @Transactional(readOnly = true)
    public List<CoverageRuleResponse> getRulesByProduct(UUID productId, AuthContext authContext) {
        InsuranceProduct product = productById(productId);
        accessService.requireCanViewProduct(authContext, product);
        return cacheService.coverageRulesByProduct(productId);
    }

    private void validateCoverage(Integer coveragePercent, BigDecimal maxAmount) {
        if (coveragePercent == null || coveragePercent < 0 || coveragePercent > 100) {
            throw new PolicyException("INVALID_COVERAGE_PERCENT", "Coverage percent must be between 0 and 100");
        }
        if (maxAmount == null || maxAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new PolicyException("INVALID_COVERAGE_AMOUNT", "Coverage max amount must be greater than or equal to zero");
        }
    }

    private CoverageRule ruleById(UUID ruleId) {
        return coverageRuleRepository.findById(ruleId)
                .orElseThrow(() -> new PolicyException("COVERAGE_RULE_NOT_FOUND", "Coverage rule was not found"));
    }

    private InsuranceProduct productById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new PolicyException("PRODUCT_NOT_FOUND", "Insurance product was not found"));
    }
}
