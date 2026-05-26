package az.saglamol.payment.service;

import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.payment.client.PolicyInternalClient;
import az.saglamol.payment.client.ProfileScopeClient;
import az.saglamol.payment.client.dto.InsuranceScopeResponse;
import az.saglamol.payment.client.dto.UserProfileSummaryResponse;
import az.saglamol.payment.dto.request.CreateClaimPayoutRequest;
import az.saglamol.payment.dto.request.CreateInvoiceRequest;
import az.saglamol.payment.dto.request.CreatePolicyPremiumPaymentRequest;
import az.saglamol.payment.entity.InvoiceStatus;
import az.saglamol.payment.entity.OutboxEvent;
import az.saglamol.payment.entity.Payment;
import az.saglamol.payment.entity.PaymentProvider;
import az.saglamol.payment.entity.PaymentStatus;
import az.saglamol.payment.entity.PaymentType;
import az.saglamol.payment.mapper.PaymentMapper;
import az.saglamol.payment.repository.InvoiceRepository;
import az.saglamol.payment.repository.PaymentRepository;
import az.saglamol.payment.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentBusinessServiceTest {

    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final PaymentTransactionRepository transactionRepository = mock(PaymentTransactionRepository.class);
    private final ProfileScopeClient profileScopeClient = mock(ProfileScopeClient.class);
    private final PaymentAccessService accessService = new PaymentAccessService(profileScopeClient);
    private final PaymentMapper mapper = new PaymentMapper();
    private final MockPaymentGatewayClient gatewayClient = mock(MockPaymentGatewayClient.class);
    private final PolicyInternalClient policyInternalClient = mock(PolicyInternalClient.class);
    @SuppressWarnings("unchecked")
    private final OutboxEventService<OutboxEvent> outboxEventService = mock(OutboxEventService.class);
    private final PaymentService paymentService = new PaymentService(
            paymentRepository,
            transactionRepository,
            mapper,
            accessService,
            gatewayClient,
            policyInternalClient,
            outboxEventService
    );

    @Test
    void createPremiumPaymentCreatesPendingPayment() {
        UUID companyId = UUID.randomUUID();
        UUID patientProfileId = UUID.randomUUID();
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = paymentService.createPolicyPremiumPayment(
                auth(UUID.randomUUID(), RoleConstants.ADMIN),
                new CreatePolicyPremiumPaymentRequest(UUID.randomUUID(), companyId, patientProfileId, new BigDecimal("120.00"), null)
        );

        assertEquals(PaymentStatus.PENDING, response.status());
        assertEquals(PaymentType.POLICY_PREMIUM, response.paymentType());
        assertEquals(companyId, response.insuranceCompanyId());
    }

    @Test
    void completePremiumPaymentActivatesPolicyAndPublishesEvent() {
        UUID policyId = UUID.randomUUID();
        Payment payment = payment(policyId, UUID.randomUUID(), UUID.randomUUID(), null, null,
                PaymentType.POLICY_PREMIUM, PaymentStatus.PENDING);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(gatewayClient.capture(payment.getAmount()))
                .thenReturn(new MockPaymentGatewayClient.MockGatewayResult(true, "CAP-1", "ok"));

        var response = paymentService.completeMock(payment.getId(), auth(UUID.randomUUID(), RoleConstants.ADMIN));

        assertEquals(PaymentStatus.COMPLETED, response.status());
        verify(policyInternalClient).activateAfterPayment(policyId);
        verify(outboxEventService).saveEvent(eq("Payment"), eq(payment.getId()), eq("PaymentCompletedEvent"), any(Object.class));
    }

    @Test
    void failedPremiumPaymentDoesNotActivatePolicy() {
        Payment payment = payment(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, null,
                PaymentType.POLICY_PREMIUM, PaymentStatus.PENDING);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));

        var response = paymentService.failMock(payment.getId(), auth(UUID.randomUUID(), RoleConstants.ADMIN), "declined");

        assertEquals(PaymentStatus.FAILED, response.status());
        verify(policyInternalClient, never()).activateAfterPayment(any());
        verify(outboxEventService).saveEvent(eq("Payment"), eq(payment.getId()), eq("PaymentFailedEvent"), any(Object.class));
    }

    @Test
    void completedPaymentCanBeRefunded() {
        Payment payment = payment(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, null,
                PaymentType.POLICY_PREMIUM, PaymentStatus.COMPLETED);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(gatewayClient.refund(payment.getAmount()))
                .thenReturn(new MockPaymentGatewayClient.MockGatewayResult(true, "REF-1", "ok"));

        var response = paymentService.refundMock(payment.getId(), auth(UUID.randomUUID(), RoleConstants.ADMIN));

        assertEquals(PaymentStatus.REFUNDED, response.status());
        verify(outboxEventService).saveEvent(eq("Payment"), eq(payment.getId()), eq("RefundCompletedEvent"), any(Object.class));
    }

    @Test
    void agentCanCreatePaymentForOwnCompany() {
        UUID userId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        when(profileScopeClient.insuranceScope(userId)).thenReturn(scope(userId, companyId));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = paymentService.createPolicyPremiumPayment(
                auth(userId, RoleConstants.AGENT),
                new CreatePolicyPremiumPaymentRequest(UUID.randomUUID(), companyId, UUID.randomUUID(), new BigDecimal("120.00"), "AZN")
        );

        assertEquals(companyId, response.insuranceCompanyId());
    }

    @Test
    void patientSeesOwnPayments() {
        UUID userId = UUID.randomUUID();
        UUID patientProfileId = UUID.randomUUID();
        when(profileScopeClient.userSummary(userId)).thenReturn(summary(userId, patientProfileId, null, null));
        when(paymentRepository.findByPatientProfileId(patientProfileId)).thenReturn(List.of(
                payment(UUID.randomUUID(), UUID.randomUUID(), patientProfileId, null, null, PaymentType.POLICY_PREMIUM, PaymentStatus.COMPLETED)
        ));

        var payments = paymentService.getMyPayments(auth(userId, RoleConstants.PATIENT));

        assertEquals(1, payments.size());
        assertEquals(patientProfileId, payments.getFirst().patientProfileId());
    }

    @Test
    void hospitalAdminCanCreateOwnHospitalPayout() {
        UUID userId = UUID.randomUUID();
        UUID hospitalId = UUID.randomUUID();
        when(profileScopeClient.userSummary(userId)).thenReturn(summary(userId, null, null, hospitalId));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = paymentService.createClaimPayout(
                auth(userId, RoleConstants.HOSPITAL_ADMIN),
                new CreateClaimPayoutRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), hospitalId, new BigDecimal("500.00"), null)
        );

        assertEquals(PaymentType.HOSPITAL_PAYOUT, response.paymentType());
        assertEquals(hospitalId, response.hospitalId());
    }

    @Test
    void invoiceLifecycleWorks() {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        InvoiceService invoiceService = new InvoiceService(invoiceRepository, mapper, accessService);
        when(invoiceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var created = invoiceService.createInvoice(
                auth(UUID.randomUUID(), RoleConstants.ADMIN),
                new CreateInvoiceRequest(UUID.randomUUID(), null, UUID.randomUUID(), null, UUID.randomUUID(), new BigDecimal("120.00"), null)
        );
        when(invoiceRepository.findById(created.id())).thenReturn(Optional.of(new az.saglamol.payment.entity.Invoice(
                created.id(),
                created.invoiceNumber(),
                created.policyId(),
                created.claimId(),
                created.insuranceCompanyId(),
                created.hospitalId(),
                created.patientProfileId(),
                created.amount(),
                created.currency(),
                created.status(),
                created.issuedAt(),
                created.paidAt(),
                created.createdAt(),
                created.updatedAt()
        )));

        var issued = invoiceService.issueInvoice(created.id(), auth(UUID.randomUUID(), RoleConstants.ADMIN));

        assertEquals(InvoiceStatus.ISSUED, issued.status());
    }

    private AuthContext auth(UUID userId, String... roles) {
        return new AuthContext(userId, List.of(roles), UUID.randomUUID().toString(), Map.of());
    }

    private InsuranceScopeResponse scope(UUID userId, UUID companyId) {
        return new InsuranceScopeResponse(userId, companyId, List.of(), true, true, true, false, false);
    }

    private UserProfileSummaryResponse summary(UUID userId, UUID patientProfileId, UUID companyId, UUID hospitalId) {
        return new UserProfileSummaryResponse(userId, patientProfileId, null, null, companyId, null, hospitalId, null,
                patientProfileId != null, false, false, hospitalId != null, companyId != null);
    }

    private Payment payment(UUID policyId, UUID companyId, UUID patientProfileId, UUID claimId, UUID hospitalId,
                            PaymentType type, PaymentStatus status) {
        Instant now = Instant.now();
        return new Payment(UUID.randomUUID(), "PAY-1", policyId, companyId, patientProfileId, claimId, hospitalId,
                new BigDecimal("120.00"), "AZN", type, status, PaymentProvider.MOCK, null, now, now);
    }
}
