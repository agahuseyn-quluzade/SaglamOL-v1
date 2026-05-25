package az.saglamol.policy.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.policy.client.ProfileScopeClient;
import az.saglamol.policy.client.dto.InsuranceScopeResponse;
import az.saglamol.policy.client.dto.UserProfileSummaryResponse;
import az.saglamol.policy.dto.request.CreateCoverageRuleRequest;
import az.saglamol.policy.dto.request.CreateInsuranceProductRequest;
import az.saglamol.policy.dto.request.CreateProviderContractRequest;
import az.saglamol.policy.entity.CoverageType;
import az.saglamol.policy.entity.InsuranceProduct;
import az.saglamol.policy.entity.InsuranceProductStatus;
import az.saglamol.policy.entity.PayoutModel;
import az.saglamol.policy.entity.ProviderContract;
import az.saglamol.policy.entity.ProviderContractStatus;
import az.saglamol.policy.entity.ServiceType;
import az.saglamol.policy.exception.PolicyException;
import az.saglamol.policy.mapper.PolicyMapper;
import az.saglamol.policy.repository.CoverageRuleRepository;
import az.saglamol.policy.repository.InsuranceProductRepository;
import az.saglamol.policy.repository.ProviderContractRepository;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PolicyBusinessServiceTest {

    private final InsuranceProductRepository productRepository = mock(InsuranceProductRepository.class);
    private final CoverageRuleRepository coverageRuleRepository = mock(CoverageRuleRepository.class);
    private final ProviderContractRepository contractRepository = mock(ProviderContractRepository.class);
    private final ProfileScopeClient profileScopeClient = mock(ProfileScopeClient.class);
    private final PolicyMapper mapper = Mappers.getMapper(PolicyMapper.class);
    private final PolicyAccessService accessService = new PolicyAccessService(profileScopeClient);
    private final PolicyCatalogCacheService cacheService = mock(PolicyCatalogCacheService.class);
    private final InsuranceProductService productService = new InsuranceProductService(
            productRepository,
            mapper,
            accessService,
            cacheService
    );
    private final CoverageRuleService coverageRuleService = new CoverageRuleService(
            coverageRuleRepository,
            productRepository,
            mapper,
            accessService,
            cacheService
    );
    private final ProviderContractService contractService = new ProviderContractService(
            contractRepository,
            productRepository,
            mapper,
            accessService
    );

    @Test
    void insuranceAdminCreatesProductForOwnCompany() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        when(profileScopeClient.insuranceScope(userId)).thenReturn(scope(userId, companyId, true, true, false));
        when(productRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = productService.createProduct(
                auth(userId, RoleConstants.INSURANCE_ADMIN),
                createProductRequest(companyId, "P-001")
        );

        assertEquals(companyId, response.insuranceCompanyId());
        assertEquals("P-001", response.productCode());
        assertEquals(InsuranceProductStatus.DRAFT, response.status());
    }

    @Test
    void insuranceAdminCannotCreateAnotherCompanyProduct() {
        UUID userId = UUID.randomUUID();
        UUID ownCompanyId = UUID.randomUUID();
        UUID anotherCompanyId = UUID.randomUUID();
        when(profileScopeClient.insuranceScope(userId)).thenReturn(scope(userId, ownCompanyId, true, true, false));

        assertThrows(PolicyException.class, () -> productService.createProduct(
                auth(userId, RoleConstants.INSURANCE_ADMIN),
                createProductRequest(anotherCompanyId, "P-001")
        ));
    }

    @Test
    void agentCannotCreateProduct() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        when(profileScopeClient.insuranceScope(userId)).thenReturn(scope(userId, companyId, true, false, true));

        assertThrows(PolicyException.class, () -> productService.createProduct(
                auth(userId, RoleConstants.AGENT),
                createProductRequest(companyId, "P-001")
        ));
    }

    @Test
    void coverageRuleValidationRejectsInvalidPercent() {
        UUID productId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product(productId, companyId, InsuranceProductStatus.ACTIVE)));

        assertThrows(PolicyException.class, () -> coverageRuleService.addCoverageRule(
                auth(UUID.randomUUID(), RoleConstants.ADMIN),
                productId,
                new CreateCoverageRuleRequest(
                        ServiceType.HOSPITAL,
                        101,
                        new BigDecimal("1000.00"),
                        0,
                        false
                )
        ));
    }

    @Test
    void providerContractCreateAndTerminate() {
        UUID companyId = UUID.randomUUID();
        UUID contractId = UUID.randomUUID();
        when(contractRepository.save(any())).thenAnswer(invocation -> {
            ProviderContract contract = invocation.getArgument(0);
            return new ProviderContract(
                    contractId,
                    contract.getInsuranceCompanyId(),
                    contract.getHospitalId(),
                    contract.getProductId(),
                    contract.getContractNumber(),
                    contract.getStartDate(),
                    contract.getEndDate(),
                    contract.getStatus(),
                    contract.getPayoutModel(),
                    contract.getCreatedAt(),
                    contract.getUpdatedAt()
            );
        });

        var created = contractService.createContract(
                auth(UUID.randomUUID(), RoleConstants.ADMIN),
                createContractRequest(companyId, UUID.randomUUID(), null, "CON-1")
        );
        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract(
                contractId,
                companyId,
                created.hospitalId(),
                null,
                ProviderContractStatus.ACTIVE
        )));

        var terminated = contractService.terminateContract(contractId, auth(UUID.randomUUID(), RoleConstants.ADMIN));

        assertEquals(ProviderContractStatus.ACTIVE, created.status());
        assertEquals(ProviderContractStatus.TERMINATED, terminated.status());
    }

    @Test
    void hospitalAdminSeesOwnHospitalContracts() {
        UUID userId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        UUID otherHospitalId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        when(profileScopeClient.userSummary(userId)).thenReturn(new UserProfileSummaryResponse(
                userId,
                null,
                null,
                null,
                null,
                UUID.randomUUID(),
                hospitalId,
                null,
                false,
                false,
                false,
                true,
                false
        ));
        when(contractRepository.findByHospitalId(hospitalId)).thenReturn(List.of(
                contract(UUID.randomUUID(), companyId, hospitalId, null, ProviderContractStatus.ACTIVE),
                contract(UUID.randomUUID(), companyId, otherHospitalId, null, ProviderContractStatus.ACTIVE)
        ));

        var contracts = contractService.getContractsByHospital(hospitalId, auth(userId, RoleConstants.HOSPITAL_ADMIN));

        assertEquals(1, contracts.size());
        assertEquals(hospitalId, contracts.getFirst().hospitalId());
    }

    private AuthContext auth(UUID userId, String... roles) {
        return new AuthContext(userId, List.of(roles), UUID.randomUUID().toString(), Map.of());
    }

    private InsuranceScopeResponse scope(UUID userId, UUID companyId, boolean canView, boolean canManage, boolean agent) {
        return new InsuranceScopeResponse(
                userId,
                companyId,
                List.of(),
                canView,
                canManage,
                agent,
                canManage,
                canView && !canManage && !agent
        );
    }

    private CreateInsuranceProductRequest createProductRequest(UUID companyId, String productCode) {
        return new CreateInsuranceProductRequest(
                companyId,
                productCode,
                "Standard Health",
                "Standard product",
                CoverageType.STANDARD,
                new BigDecimal("100.00"),
                new BigDecimal("10000.00"),
                "AZN"
        );
    }

    private CreateProviderContractRequest createContractRequest(UUID companyId, UUID hospitalId, UUID productId, String contractNumber) {
        return new CreateProviderContractRequest(
                companyId,
                hospitalId,
                productId,
                contractNumber,
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                PayoutModel.DIRECT_TO_HOSPITAL
        );
    }

    private InsuranceProduct product(UUID productId, UUID companyId, InsuranceProductStatus status) {
        Instant now = Instant.now();
        return new InsuranceProduct(
                productId,
                companyId,
                "P-001",
                "Standard Health",
                "Standard product",
                CoverageType.STANDARD,
                new BigDecimal("100.00"),
                new BigDecimal("10000.00"),
                "AZN",
                status,
                now,
                now
        );
    }

    private ProviderContract contract(UUID contractId, UUID companyId, UUID hospitalId, UUID productId,
                                      ProviderContractStatus status) {
        Instant now = Instant.now();
        return new ProviderContract(
                contractId,
                companyId,
                hospitalId,
                productId,
                "CON-" + contractId.toString().substring(0, 8),
                LocalDate.now(),
                LocalDate.now().plusYears(1),
                status,
                PayoutModel.DIRECT_TO_HOSPITAL,
                now,
                now
        );
    }
}
