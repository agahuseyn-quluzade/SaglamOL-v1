package az.saglamol.payment.service;

import az.saglamol.common.events.payment.ClaimPayoutCompletedEvent;
import az.saglamol.common.events.payment.ClaimPayoutFailedEvent;
import az.saglamol.common.events.payment.PaymentCompletedEvent;
import az.saglamol.common.events.payment.PaymentFailedEvent;
import az.saglamol.common.events.payment.RefundCompletedEvent;
import az.saglamol.common.events.claim.ClaimApprovedEvent;
import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.common.security.AuthContext;
import az.saglamol.payment.client.PolicyInternalClient;
import az.saglamol.payment.dto.request.CreateClaimPayoutRequest;
import az.saglamol.payment.dto.request.CreatePolicyPremiumPaymentRequest;
import az.saglamol.payment.dto.response.PaymentResponse;
import az.saglamol.payment.entity.OutboxEvent;
import az.saglamol.payment.entity.Payment;
import az.saglamol.payment.entity.PaymentProvider;
import az.saglamol.payment.entity.PaymentStatus;
import az.saglamol.payment.entity.PaymentTransaction;
import az.saglamol.payment.entity.PaymentType;
import az.saglamol.payment.entity.TransactionStatus;
import az.saglamol.payment.entity.TransactionType;
import az.saglamol.payment.exception.PaymentException;
import az.saglamol.payment.mapper.PaymentMapper;
import az.saglamol.payment.repository.PaymentRepository;
import az.saglamol.payment.repository.PaymentTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentMapper mapper;
    private final PaymentAccessService accessService;
    private final MockPaymentGatewayClient gatewayClient;
    private final PolicyInternalClient policyInternalClient;
    private final OutboxEventService<OutboxEvent> outboxEventService;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentTransactionRepository transactionRepository,
            PaymentMapper mapper,
            PaymentAccessService accessService,
            MockPaymentGatewayClient gatewayClient,
            PolicyInternalClient policyInternalClient,
            OutboxEventService<OutboxEvent> outboxEventService
    ) {
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
        this.accessService = accessService;
        this.gatewayClient = gatewayClient;
        this.policyInternalClient = policyInternalClient;
        this.outboxEventService = outboxEventService;
    }

    @Transactional
    public PaymentResponse createPolicyPremiumPayment(AuthContext authContext, CreatePolicyPremiumPaymentRequest request) {
        accessService.requireCanCreateCompanyPayment(authContext, request.insuranceCompanyId(), request.patientProfileId());
        Instant now = Instant.now();
        Payment payment = new Payment(
                UUID.randomUUID(),
                nextPaymentNumber(),
                request.policyId(),
                request.insuranceCompanyId(),
                request.patientProfileId(),
                null,
                null,
                request.amount(),
                currency(request.currency()),
                PaymentType.POLICY_PREMIUM,
                PaymentStatus.PENDING,
                PaymentProvider.MOCK,
                null,
                now,
                now
        );
        Payment saved = paymentRepository.save(payment);
        addTransaction(saved, TransactionType.AUTHORIZE, TransactionStatus.SUCCESS, "MOCK_AUTHORIZE_PENDING");
        return mapper.toResponse(saved);
    }

    @Transactional
    public PaymentResponse createClaimPayout(AuthContext authContext, CreateClaimPayoutRequest request) {
        accessService.requireCanCreatePayout(authContext, request.insuranceCompanyId(), request.hospitalId());
        return createClaimPayoutInternal(request);
    }

    @Transactional
    public PaymentResponse createClaimPayoutFromApprovedClaim(ClaimApprovedEvent event) {
        if (!paymentRepository.findByClaimId(event.claimId()).isEmpty()) {
            return mapper.toResponse(paymentRepository.findByClaimId(event.claimId()).get(0));
        }
        return createClaimPayoutInternal(new CreateClaimPayoutRequest(
                event.claimId(),
                event.companyId(),
                event.patientProfileId(),
                null,
                event.approvedAmount(),
                "AZN"
        ));
    }

    private PaymentResponse createClaimPayoutInternal(CreateClaimPayoutRequest request) {
        Instant now = Instant.now();
        Payment payment = new Payment(
                UUID.randomUUID(),
                nextPaymentNumber(),
                null,
                request.insuranceCompanyId(),
                request.patientProfileId(),
                request.claimId(),
                request.hospitalId(),
                request.amount(),
                currency(request.currency()),
                request.hospitalId() == null ? PaymentType.CLAIM_PAYOUT : PaymentType.HOSPITAL_PAYOUT,
                PaymentStatus.PENDING,
                PaymentProvider.MOCK,
                null,
                now,
                now
        );
        Payment saved = paymentRepository.save(payment);
        addTransaction(saved, TransactionType.PAYOUT, TransactionStatus.SUCCESS, "MOCK_PAYOUT_PENDING");
        return mapper.toResponse(saved);
    }

    @Transactional
    public PaymentResponse completeMock(UUID paymentId, AuthContext authContext) {
        Payment payment = paymentById(paymentId);
        accessService.requireCanViewPayment(authContext, payment);
        requirePending(payment);
        MockPaymentGatewayClient.MockGatewayResult result = payment.getPaymentType() == PaymentType.POLICY_PREMIUM
                ? gatewayClient.capture(payment.getAmount())
                : gatewayClient.payout(payment.getAmount());
        if (!result.success()) {
            return failPayment(payment, result.reference(), result.providerResponse());
        }
        payment.complete(result.reference(), Instant.now());
        addTransaction(payment, payment.getPaymentType() == PaymentType.POLICY_PREMIUM ? TransactionType.CAPTURE : TransactionType.PAYOUT,
                TransactionStatus.SUCCESS, result.providerResponse());
        if (payment.getPaymentType() == PaymentType.POLICY_PREMIUM && payment.getPolicyId() != null) {
            policyInternalClient.activateAfterPayment(payment.getPolicyId());
        }
        publishCompleted(payment);
        return mapper.toResponse(payment);
    }

    @Transactional
    public PaymentResponse failMock(UUID paymentId, AuthContext authContext, String reason) {
        Payment payment = paymentById(paymentId);
        accessService.requireCanViewPayment(authContext, payment);
        requirePending(payment);
        return failPayment(payment, "MOCK-FAIL-" + UUID.randomUUID().toString().substring(0, 8), reason == null ? "MOCK_FAILED" : reason);
    }

    @Transactional
    public PaymentResponse refundMock(UUID paymentId, AuthContext authContext) {
        Payment payment = paymentById(paymentId);
        accessService.requireCanViewPayment(authContext, payment);
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentException("PAYMENT_NOT_COMPLETED", "Only completed payments can be refunded");
        }
        MockPaymentGatewayClient.MockGatewayResult result = gatewayClient.refund(payment.getAmount());
        if (!result.success()) {
            addTransaction(payment, TransactionType.REFUND, TransactionStatus.FAILED, result.providerResponse());
            throw new PaymentException("PAYMENT_REFUND_FAILED", "Mock refund failed");
        }
        payment.refund(result.reference(), Instant.now());
        addTransaction(payment, TransactionType.REFUND, TransactionStatus.SUCCESS, result.providerResponse());
        outboxEventService.saveEvent("Payment", payment.getId(), "RefundCompletedEvent", new RefundCompletedEvent(
                payment.getId(),
                payment.getInsuranceCompanyId(),
                payment.getAmount(),
                Instant.now()
        ));
        return mapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId, AuthContext authContext) {
        Payment payment = paymentById(paymentId);
        accessService.requireCanViewPayment(authContext, payment);
        return mapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments(AuthContext authContext) {
        UUID patientProfileId = accessService.currentPatientProfileId(authContext);
        if (patientProfileId == null) {
            return List.of();
        }
        return paymentRepository.findByPatientProfileId(patientProfileId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getByPolicy(UUID policyId, AuthContext authContext) {
        return paymentRepository.findByPolicyId(policyId).stream()
                .peek(payment -> accessService.requireCanViewPayment(authContext, payment))
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getByClaim(UUID claimId, AuthContext authContext) {
        return paymentRepository.findByClaimId(claimId).stream()
                .peek(payment -> accessService.requireCanViewPayment(authContext, payment))
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getByCompany(UUID companyId, Pageable pageable, AuthContext authContext) {
        if (!accessService.isAdmin(authContext)) {
            UUID scopedCompanyId = accessService.resolveCompanyScope(authContext);
            if (!companyId.equals(scopedCompanyId)) {
                throw new PaymentException("FORBIDDEN", "Current user cannot access this company payments");
            }
        }
        return paymentRepository.findByInsuranceCompanyId(companyId, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getByHospital(UUID hospitalId, Pageable pageable, AuthContext authContext) {
        if (!accessService.isAdmin(authContext)) {
            UUID scopedHospitalId = accessService.currentHospitalId(authContext);
            if (!hospitalId.equals(scopedHospitalId)) {
                throw new PaymentException("FORBIDDEN", "Current user cannot access this hospital payments");
            }
        }
        return paymentRepository.findByHospitalId(hospitalId, pageable).map(mapper::toResponse);
    }

    private PaymentResponse failPayment(Payment payment, String providerReference, String reason) {
        payment.fail(providerReference, Instant.now());
        addTransaction(payment, payment.getPaymentType() == PaymentType.POLICY_PREMIUM ? TransactionType.CAPTURE : TransactionType.PAYOUT,
                TransactionStatus.FAILED, reason);
        outboxEventService.saveEvent("Payment", payment.getId(), "PaymentFailedEvent", new PaymentFailedEvent(
                payment.getId(),
                payment.getInsuranceCompanyId(),
                payment.getPolicyId(),
                reason,
                Instant.now()
        ));
        if (payment.getClaimId() != null) {
            outboxEventService.saveEvent("Payment", payment.getId(), "ClaimPayoutFailedEvent", new ClaimPayoutFailedEvent(
                    payment.getId(),
                    payment.getClaimId(),
                    payment.getInsuranceCompanyId(),
                    reason,
                    Instant.now()
            ));
        }
        return mapper.toResponse(payment);
    }

    private void publishCompleted(Payment payment) {
        outboxEventService.saveEvent("Payment", payment.getId(), "PaymentCompletedEvent", new PaymentCompletedEvent(
                payment.getId(),
                payment.getPaymentNumber(),
                payment.getInsuranceCompanyId(),
                payment.getPolicyId(),
                payment.getClaimId(),
                payment.getPatientProfileId(),
                payment.getHospitalId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentType().name(),
                Instant.now()
        ));
        if (payment.getClaimId() != null) {
            outboxEventService.saveEvent("Payment", payment.getId(), "ClaimPayoutCompletedEvent", new ClaimPayoutCompletedEvent(
                    payment.getId(),
                    payment.getClaimId(),
                    payment.getInsuranceCompanyId(),
                    payment.getAmount(),
                    Instant.now()
            ));
        }
    }

    private void addTransaction(Payment payment, TransactionType type, TransactionStatus status, String providerResponse) {
        transactionRepository.save(new PaymentTransaction(
                UUID.randomUUID(),
                payment.getId(),
                type,
                payment.getAmount(),
                status,
                providerResponse,
                Instant.now()
        ));
    }

    private Payment paymentById(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("PAYMENT_NOT_FOUND", "Payment was not found"));
    }

    private void requirePending(Payment payment) {
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentException("INVALID_PAYMENT_STATUS", "Only pending payments can be completed or failed");
        }
    }

    private String currency(String currency) {
        return currency == null || currency.isBlank() ? "AZN" : currency;
    }

    private String nextPaymentNumber() {
        return "PAY-" + Year.now().getValue() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
