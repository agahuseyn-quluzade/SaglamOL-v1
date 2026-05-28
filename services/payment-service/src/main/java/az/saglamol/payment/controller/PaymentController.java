package az.saglamol.payment.controller;

import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.payment.dto.request.CreateClaimPayoutRequest;
import az.saglamol.payment.dto.request.CreatePolicyPremiumPaymentRequest;
import az.saglamol.payment.dto.request.FailPaymentRequest;
import az.saglamol.payment.dto.response.PaymentResponse;
import az.saglamol.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Premium payment and claim payout endpoints")
@SecurityRequirement(name = "BearerAuth")
@ApiResponse(responseCode = "200", description = "Successful payment operation")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/policy-premium")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create policy premium payment")
    public PaymentResponse createPolicyPremiumPayment(@Valid @RequestBody CreatePolicyPremiumPaymentRequest request) {
        return paymentService.createPolicyPremiumPayment(AuthContextHolder.getRequired(), request);
    }

    @PostMapping("/claim-payout")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create claim payout")
    public PaymentResponse createClaimPayout(@Valid @RequestBody CreateClaimPayoutRequest request) {
        return paymentService.createClaimPayout(AuthContextHolder.getRequired(), request);
    }

    @PostMapping("/{id}/complete-mock")
    @Operation(summary = "Complete mock payment")
    public PaymentResponse completeMock(@PathVariable UUID id) {
        return paymentService.completeMock(id, AuthContextHolder.getRequired());
    }

    @PostMapping("/{id}/fail-mock")
    @Operation(summary = "Fail mock payment")
    public PaymentResponse failMock(@PathVariable UUID id, @RequestBody(required = false) FailPaymentRequest request) {
        return paymentService.failMock(id, AuthContextHolder.getRequired(), request == null ? null : request.reason());
    }

    @PostMapping("/{id}/refund-mock")
    @Operation(summary = "Refund mock payment")
    public PaymentResponse refundMock(@PathVariable UUID id) {
        return paymentService.refundMock(id, AuthContextHolder.getRequired());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    public PaymentResponse getPayment(@PathVariable UUID id) {
        return paymentService.getPayment(id, AuthContextHolder.getRequired());
    }

    @GetMapping("/my")
    public List<PaymentResponse> getMyPayments() {
        return paymentService.getMyPayments(AuthContextHolder.getRequired());
    }

    @GetMapping("/by-policy")
    public List<PaymentResponse> getByPolicy(@RequestParam UUID policyId) {
        return paymentService.getByPolicy(policyId, AuthContextHolder.getRequired());
    }

    @GetMapping("/by-claim")
    public List<PaymentResponse> getByClaim(@RequestParam UUID claimId) {
        return paymentService.getByClaim(claimId, AuthContextHolder.getRequired());
    }

    @GetMapping("/by-company")
    public Page<PaymentResponse> getByCompany(@RequestParam UUID companyId, @PageableDefault(size = 20) Pageable pageable) {
        return paymentService.getByCompany(companyId, pageable, AuthContextHolder.getRequired());
    }

    @GetMapping("/by-hospital")
    public Page<PaymentResponse> getByHospital(@RequestParam UUID hospitalId, @PageableDefault(size = 20) Pageable pageable) {
        return paymentService.getByHospital(hospitalId, pageable, AuthContextHolder.getRequired());
    }
}
