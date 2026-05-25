package az.saglamol.policy.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.policy.dto.request.CreateInsuranceProductRequest;
import az.saglamol.policy.dto.request.UpdateInsuranceProductRequest;
import az.saglamol.policy.dto.response.InsuranceProductResponse;
import az.saglamol.policy.entity.InsuranceProduct;
import az.saglamol.policy.entity.InsuranceProductStatus;
import az.saglamol.policy.exception.PolicyException;
import az.saglamol.policy.mapper.PolicyMapper;
import az.saglamol.policy.repository.InsuranceProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class InsuranceProductService {

    private final InsuranceProductRepository productRepository;
    private final PolicyMapper policyMapper;
    private final PolicyAccessService accessService;
    private final PolicyCatalogCacheService cacheService;

    public InsuranceProductService(
            InsuranceProductRepository productRepository,
            PolicyMapper policyMapper,
            PolicyAccessService accessService,
            PolicyCatalogCacheService cacheService
    ) {
        this.productRepository = productRepository;
        this.policyMapper = policyMapper;
        this.accessService = accessService;
        this.cacheService = cacheService;
    }

    @Transactional
    public InsuranceProductResponse createProduct(AuthContext authContext, CreateInsuranceProductRequest request) {
        accessService.requireCanCreateProduct(authContext, request.insuranceCompanyId());
        if (productRepository.existsByInsuranceCompanyIdAndProductCode(request.insuranceCompanyId(), request.productCode())) {
            throw new PolicyException("PRODUCT_ALREADY_EXISTS", "Product code already exists for insurance company");
        }
        Instant now = Instant.now();
        InsuranceProduct product = new InsuranceProduct(
                UUID.randomUUID(),
                request.insuranceCompanyId(),
                request.productCode(),
                request.name(),
                request.description(),
                request.coverageType(),
                request.premiumAmount(),
                request.annualLimit(),
                request.currency(),
                InsuranceProductStatus.DRAFT,
                now,
                now
        );
        InsuranceProduct saved = productRepository.save(product);
        cacheService.evictActiveProductsByCompany(saved.getInsuranceCompanyId());
        cacheService.evictAllActiveProducts();
        return policyMapper.toResponse(saved);
    }

    @Transactional
    public InsuranceProductResponse updateProduct(AuthContext authContext, UUID productId, UpdateInsuranceProductRequest request) {
        InsuranceProduct product = productById(productId);
        accessService.requireCanManageProduct(authContext, product);
        product.update(
                request.name(),
                request.description(),
                request.coverageType(),
                request.premiumAmount(),
                request.annualLimit(),
                request.currency(),
                Instant.now()
        );
        cacheService.evictActiveProductsByCompany(product.getInsuranceCompanyId());
        cacheService.evictAllActiveProducts();
        return policyMapper.toResponse(product);
    }

    @Transactional
    public InsuranceProductResponse changeStatus(AuthContext authContext, UUID productId, InsuranceProductStatus status) {
        InsuranceProduct product = productById(productId);
        accessService.requireCanManageProduct(authContext, product);
        product.changeStatus(status, Instant.now());
        cacheService.evictActiveProductsByCompany(product.getInsuranceCompanyId());
        cacheService.evictAllActiveProducts();
        return policyMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public InsuranceProductResponse getProduct(UUID productId, AuthContext authContext) {
        InsuranceProduct product = productById(productId);
        accessService.requireCanViewProduct(authContext, product);
        return policyMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<InsuranceProductResponse> searchProducts(UUID companyId, InsuranceProductStatus status,
                                                         Pageable pageable, AuthContext authContext) {
        if (authContext.hasRole(RoleConstants.PATIENT)) {
            if (status != null && status != InsuranceProductStatus.ACTIVE) {
                throw new PolicyException("FORBIDDEN", "Patients can only view active products");
            }
            List<InsuranceProductResponse> products = companyId == null
                    ? cacheService.allActiveProducts()
                    : cacheService.activeProductsByCompany(companyId);
            return page(products, pageable);
        }

        UUID scopedCompanyId = accessService.resolveSearchCompany(authContext, companyId);
        if (authContext.hasRole(RoleConstants.AGENT)) {
            if (status != null && status != InsuranceProductStatus.ACTIVE) {
                throw new PolicyException("FORBIDDEN", "Agents can only view active products");
            }
            return page(cacheService.activeProductsByCompany(scopedCompanyId), pageable);
        }

        if (scopedCompanyId == null && status == InsuranceProductStatus.ACTIVE) {
            return page(cacheService.allActiveProducts(), pageable);
        }

        Page<InsuranceProduct> result;
        if (scopedCompanyId != null && status != null) {
            result = productRepository.findByInsuranceCompanyIdAndStatus(scopedCompanyId, status, pageable);
        } else if (scopedCompanyId != null) {
            result = productRepository.findByInsuranceCompanyId(scopedCompanyId, pageable);
        } else if (status != null) {
            result = productRepository.findByStatus(status, pageable);
        } else {
            result = productRepository.findAll(pageable);
        }
        return result.map(policyMapper::toResponse);
    }

    private Page<InsuranceProductResponse> page(List<InsuranceProductResponse> items, Pageable pageable) {
        int start = Math.min((int) pageable.getOffset(), items.size());
        int end = Math.min(start + pageable.getPageSize(), items.size());
        return new PageImpl<>(items.subList(start, end), pageable, items.size());
    }

    private InsuranceProduct productById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new PolicyException("PRODUCT_NOT_FOUND", "Insurance product was not found"));
    }
}
