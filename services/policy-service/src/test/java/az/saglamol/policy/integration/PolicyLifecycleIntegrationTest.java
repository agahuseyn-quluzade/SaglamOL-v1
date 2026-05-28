package az.saglamol.policy.integration;

import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.policy.client.ProfileScopeClient;
import az.saglamol.policy.dto.request.EligibilityCheckRequest;
import az.saglamol.policy.dto.request.IssuePolicyRequest;
import az.saglamol.policy.entity.CoverageRule;
import az.saglamol.policy.entity.CoverageType;
import az.saglamol.policy.entity.InsuranceProduct;
import az.saglamol.policy.entity.InsuranceProductStatus;
import az.saglamol.policy.entity.OutboxEvent;
import az.saglamol.policy.entity.PayoutModel;
import az.saglamol.policy.entity.PolicyStatus;
import az.saglamol.policy.entity.ProviderContract;
import az.saglamol.policy.entity.ProviderContractStatus;
import az.saglamol.policy.entity.ReservationStatus;
import az.saglamol.policy.entity.RuleStatus;
import az.saglamol.policy.entity.ServiceType;
import az.saglamol.policy.repository.CoverageRuleRepository;
import az.saglamol.policy.repository.InsuranceProductRepository;
import az.saglamol.policy.repository.ProviderContractRepository;
import az.saglamol.policy.service.EligibilityService;
import az.saglamol.policy.service.PolicyLimitService;
import az.saglamol.policy.service.PolicyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "outbox.scheduler.enabled=false"
})
@Testcontainers(disabledWithoutDocker = true)
class PolicyLifecycleIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("policy_it")
            .withUsername("saglamol")
            .withPassword("saglamol");

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    PolicyService policyService;
    @Autowired
    EligibilityService eligibilityService;
    @Autowired
    PolicyLimitService limitService;
    @Autowired
    InsuranceProductRepository productRepository;
    @Autowired
    CoverageRuleRepository ruleRepository;
    @Autowired
    ProviderContractRepository contractRepository;

    @MockBean
    ProfileScopeClient profileScopeClient;
    @MockBean
    OutboxEventService<OutboxEvent> outboxEventService;

    @Test
    void seedProductIssuePolicyEligibilityReserveAndCommit() {
        UUID companyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID patientProfileId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        Instant now = Instant.now();
        productRepository.save(new InsuranceProduct(productId, companyId, "IT-GOLD", "Gold", null,
                CoverageType.STANDARD, new BigDecimal("100.00"), new BigDecimal("1000.00"),
                "AZN", InsuranceProductStatus.ACTIVE, now, now));
        ruleRepository.save(new CoverageRule(UUID.randomUUID(), productId, ServiceType.HOSPITAL, 80,
                new BigDecimal("500.00"), 0, false, RuleStatus.ACTIVE, now, now));
        contractRepository.save(new ProviderContract(UUID.randomUUID(), companyId, hospitalId, productId,
                "IT-CON", LocalDate.now().minusDays(1), LocalDate.now().plusYears(1),
                ProviderContractStatus.ACTIVE, PayoutModel.DIRECT_TO_HOSPITAL, now, now));
        when(profileScopeClient.patientExists(patientProfileId)).thenReturn(true);

        var policy = policyService.issuePolicy(admin(), new IssuePolicyRequest(
                productId, patientProfileId, null, LocalDate.now(), LocalDate.now().plusYears(1)));

        assertEquals(PolicyStatus.PAYMENT_PENDING, policy.status());
        assertEquals(companyId, policy.insuranceCompanyId());

        policyService.activatePolicy(policy.id());
        var eligibility = eligibilityService.checkEligibility(new EligibilityCheckRequest(
                policy.id(), companyId, patientProfileId, hospitalId, ServiceType.HOSPITAL,
                new BigDecimal("100.00"), LocalDate.now()));

        assertTrue(eligibility.eligible());
        assertTrue(eligibility.inNetwork());

        UUID claimId = UUID.randomUUID();
        var reserved = limitService.reserveLimit(policy.id(), claimId, companyId, new BigDecimal("80.00"));
        var committed = limitService.commitReservation(reserved.id());

        assertEquals(ReservationStatus.RESERVED, reserved.status());
        assertEquals(ReservationStatus.COMMITTED, committed.status());
    }

    private AuthContext admin() {
        return new AuthContext(UUID.randomUUID(), List.of(RoleConstants.ADMIN), "corr", Map.of());
    }
}
