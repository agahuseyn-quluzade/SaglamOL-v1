package az.saglamol.policy.service;

import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.policy.client.ProfileScopeClient;
import az.saglamol.policy.client.dto.InsuranceScopeResponse;
import az.saglamol.policy.client.dto.UserProfileSummaryResponse;
import az.saglamol.policy.dto.request.IssuePolicyRequest;
import az.saglamol.policy.entity.CoverageType;
import az.saglamol.policy.entity.InsuranceProduct;
import az.saglamol.policy.entity.InsuranceProductStatus;
import az.saglamol.policy.entity.OutboxEvent;
import az.saglamol.policy.entity.Policy;
import az.saglamol.policy.entity.PolicyStatus;
import az.saglamol.policy.exception.PolicyException;
import az.saglamol.policy.mapper.PolicyMapper;
import az.saglamol.policy.repository.InsuranceProductRepository;
import az.saglamol.policy.repository.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PolicyLifecycleServiceTest {

    private final PolicyRepository policyRepository = mock(PolicyRepository.class);
    private final InsuranceProductRepository productRepository = mock(InsuranceProductRepository.class);
    private final ProfileScopeClient profileScopeClient = mock(ProfileScopeClient.class);
    private final PolicyMapper mapper = Mappers.getMapper(PolicyMapper.class);
    private final PolicyAccessService accessService = new PolicyAccessService(profileScopeClient);
    @SuppressWarnings("unchecked")
    private final OutboxEventService<OutboxEvent> outboxEventService = mock(OutboxEventService.class);
    private final PolicyService service = new PolicyService(
            policyRepository,
            productRepository,
            profileScopeClient,
            mapper,
            accessService,
            outboxEventService
    );

    @Test
    void issuePolicyCreatesPaymentPendingPolicyAndOutboxEvent() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID patientProfileId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product(productId, companyId, InsuranceProductStatus.ACTIVE)));
        when(profileScopeClient.insuranceScope(userId)).thenReturn(scope(userId, companyId, true, true, false));
        when(profileScopeClient.patientExists(patientProfileId)).thenReturn(true);
        when(policyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.issuePolicy(
                auth(userId, RoleConstants.INSURANCE_ADMIN),
                new IssuePolicyRequest(productId, patientProfileId, null, LocalDate.now(), LocalDate.now().plusYears(1))
        );

        assertEquals(PolicyStatus.PAYMENT_PENDING, response.status());
        assertEquals(companyId, response.insuranceCompanyId());
        verify(outboxEventService).saveEvent(eq("Policy"), eq(response.id()), eq("PolicyCreatedEvent"), any(Object.class));
    }

    @Test
    void issuePolicyRequiresActiveProduct() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product(productId, UUID.randomUUID(), InsuranceProductStatus.DRAFT)));

        assertThrows(PolicyException.class, () -> service.issuePolicy(
                auth(UUID.randomUUID(), RoleConstants.ADMIN),
                new IssuePolicyRequest(productId, UUID.randomUUID(), null, LocalDate.now(), LocalDate.now().plusYears(1))
        ));
    }

    @Test
    void patientSeesOwnPolicies() {
        UUID userId = UUID.randomUUID();
        UUID patientProfileId = UUID.randomUUID();
        when(profileScopeClient.userSummary(userId)).thenReturn(summary(userId, patientProfileId, null, null));
        when(policyRepository.findByPatientProfileId(patientProfileId)).thenReturn(List.of(
                policy(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), patientProfileId, PolicyStatus.ACTIVE)
        ));

        var policies = service.getMyPolicies(auth(userId, RoleConstants.PATIENT));

        assertEquals(1, policies.size());
        assertEquals(patientProfileId, policies.getFirst().patientProfileId());
    }

    @Test
    void agentSearchIsScopedToOwnCompany() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 20);
        when(profileScopeClient.insuranceScope(userId)).thenReturn(scope(userId, companyId, true, false, true));
        when(policyRepository.search(eq(companyId), eq(null), eq(PolicyStatus.ACTIVE), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(policy(UUID.randomUUID(), companyId, UUID.randomUUID(), UUID.randomUUID(), PolicyStatus.ACTIVE))));

        var page = service.searchPolicies(null, null, PolicyStatus.ACTIVE, pageable, auth(userId, RoleConstants.AGENT));

        assertEquals(1, page.getTotalElements());
    }

    @Test
    void activatePaymentPendingPolicyPublishesEvent() {
        UUID policyId = UUID.randomUUID();
        Policy policy = policy(policyId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), PolicyStatus.PAYMENT_PENDING);
        when(policyRepository.findById(policyId)).thenReturn(Optional.of(policy));

        var response = service.activatePolicy(policyId);

        assertEquals(PolicyStatus.ACTIVE, response.status());
        verify(outboxEventService).saveEvent(eq("Policy"), eq(policyId), eq("PolicyActivatedEvent"), any(Object.class));
    }

    @Test
    void cancelPolicyPublishesEvent() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        when(profileScopeClient.insuranceScope(userId)).thenReturn(scope(userId, companyId, true, true, false));
        when(policyRepository.findById(policyId)).thenReturn(Optional.of(policy(policyId, companyId, UUID.randomUUID(), UUID.randomUUID(), PolicyStatus.ACTIVE)));

        var response = service.cancelPolicy(policyId, auth(userId, RoleConstants.INSURANCE_ADMIN), "customer request");

        assertEquals(PolicyStatus.CANCELLED, response.status());
        verify(outboxEventService).saveEvent(eq("Policy"), eq(policyId), eq("PolicyCancelledEvent"), any(Object.class));
    }

    private AuthContext auth(UUID userId, String... roles) {
        return new AuthContext(userId, List.of(roles), UUID.randomUUID().toString(), Map.of());
    }

    private InsuranceScopeResponse scope(UUID userId, UUID companyId, boolean canView, boolean canManage, boolean agent) {
        return new InsuranceScopeResponse(userId, companyId, List.of(), canView, canManage, agent, canManage, false);
    }

    private UserProfileSummaryResponse summary(UUID userId, UUID patientProfileId, UUID agentProfileId, UUID companyId) {
        return new UserProfileSummaryResponse(userId, patientProfileId, null, agentProfileId, companyId, null, null, null,
                patientProfileId != null, false, agentProfileId != null, false, false);
    }

    private InsuranceProduct product(UUID productId, UUID companyId, InsuranceProductStatus status) {
        Instant now = Instant.now();
        return new InsuranceProduct(productId, companyId, "P-1", "Standard", null, CoverageType.STANDARD,
                new BigDecimal("100.00"), new BigDecimal("10000.00"), "AZN", status, now, now);
    }

    private Policy policy(UUID policyId, UUID companyId, UUID productId, UUID patientProfileId, PolicyStatus status) {
        Instant now = Instant.now();
        return new Policy(policyId, "POL-1", companyId, productId, patientProfileId, null, status,
                LocalDate.now().minusDays(1), LocalDate.now().plusYears(1), new BigDecimal("100.00"),
                new BigDecimal("10000.00"), BigDecimal.ZERO, BigDecimal.ZERO, now, now);
    }
}
