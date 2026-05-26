package az.saglamol.policy.service;

import az.saglamol.common.events.policy.PolicyActivatedEvent;
import az.saglamol.common.events.policy.PolicyCancelledEvent;
import az.saglamol.common.events.policy.PolicyCreatedEvent;
import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.policy.client.ProfileScopeClient;
import az.saglamol.policy.dto.request.IssuePolicyRequest;
import az.saglamol.policy.dto.response.PolicyResponse;
import az.saglamol.policy.entity.InsuranceProduct;
import az.saglamol.policy.entity.InsuranceProductStatus;
import az.saglamol.policy.entity.OutboxEvent;
import az.saglamol.policy.entity.Policy;
import az.saglamol.policy.entity.PolicyStatus;
import az.saglamol.policy.exception.PolicyException;
import az.saglamol.policy.mapper.PolicyMapper;
import az.saglamol.policy.repository.InsuranceProductRepository;
import az.saglamol.policy.repository.PolicyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final InsuranceProductRepository productRepository;
    private final ProfileScopeClient profileScopeClient;
    private final PolicyMapper policyMapper;
    private final PolicyAccessService accessService;
    private final OutboxEventService<OutboxEvent> outboxEventService;

    public PolicyService(
            PolicyRepository policyRepository,
            InsuranceProductRepository productRepository,
            ProfileScopeClient profileScopeClient,
            PolicyMapper policyMapper,
            PolicyAccessService accessService,
            OutboxEventService<OutboxEvent> outboxEventService
    ) {
        this.policyRepository = policyRepository;
        this.productRepository = productRepository;
        this.profileScopeClient = profileScopeClient;
        this.policyMapper = policyMapper;
        this.accessService = accessService;
        this.outboxEventService = outboxEventService;
    }

    @Transactional
    public PolicyResponse issuePolicy(AuthContext authContext, IssuePolicyRequest request) {
        InsuranceProduct product = productById(request.productId());
        if (product.getStatus() != InsuranceProductStatus.ACTIVE) {
            throw new PolicyException("PRODUCT_NOT_ACTIVE", "Policy can only be issued for active products");
        }
        accessService.requireCanOperateForCompany(authContext, product.getInsuranceCompanyId());
        validateDateRange(request.startDate(), request.endDate());
        if (!profileScopeClient.patientExists(request.patientProfileId())) {
            throw new PolicyException("PATIENT_PROFILE_NOT_FOUND", "Patient profile was not found");
        }

        UUID agentProfileId = resolveAgentProfileId(authContext, request.agentProfileId(), product.getInsuranceCompanyId());
        Instant now = Instant.now();
        Policy policy = new Policy(
                UUID.randomUUID(),
                nextPolicyNumber(),
                product.getInsuranceCompanyId(),
                product.getId(),
                request.patientProfileId(),
                agentProfileId,
                PolicyStatus.PAYMENT_PENDING,
                request.startDate(),
                request.endDate(),
                product.getPremiumAmount(),
                product.getAnnualLimit(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                now,
                now
        );
        Policy saved = policyRepository.save(policy);
        outboxEventService.saveEvent("Policy", saved.getId(), PolicyCreatedEvent.class.getSimpleName(), new PolicyCreatedEvent(
                saved.getId(),
                saved.getPolicyNumber(),
                saved.getInsuranceCompanyId(),
                saved.getProductId(),
                saved.getPatientProfileId(),
                saved.getAgentProfileId(),
                saved.getPremiumAmount(),
                now
        ));
        return policyMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PolicyResponse getPolicy(UUID policyId, AuthContext authContext) {
        Policy policy = policyById(policyId);
        accessService.requireCanViewPolicy(authContext, policy.getInsuranceCompanyId(), policy.getPatientProfileId());
        return policyMapper.toResponse(policy);
    }

    @Transactional(readOnly = true)
    public List<PolicyResponse> getMyPolicies(AuthContext authContext) {
        if (authContext.hasRole(RoleConstants.PATIENT)) {
            UUID patientProfileId = accessService.currentPatientProfileId(authContext);
            if (patientProfileId == null) {
                return List.of();
            }
            return policyRepository.findByPatientProfileId(patientProfileId).stream()
                    .map(policyMapper::toResponse)
                    .toList();
        }
        if (authContext.hasRole(RoleConstants.ADMIN)) {
            return policyRepository.findAll().stream().map(policyMapper::toResponse).toList();
        }
        UUID companyId = accessService.resolveSearchCompany(authContext, null);
        return policyRepository.findByInsuranceCompanyId(companyId).stream()
                .map(policyMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PolicyResponse> searchPolicies(UUID companyId, UUID patientProfileId, PolicyStatus status,
                                               Pageable pageable, AuthContext authContext) {
        if (authContext.hasRole(RoleConstants.PATIENT)) {
            UUID currentPatientProfileId = accessService.currentPatientProfileId(authContext);
            if (currentPatientProfileId == null) {
                throw new PolicyException("FORBIDDEN", "Patient profile is required");
            }
            if (patientProfileId != null && !patientProfileId.equals(currentPatientProfileId)) {
                throw new PolicyException("FORBIDDEN", "Patients can only search own policies");
            }
            return policyRepository.search(companyId, currentPatientProfileId, status, pageable)
                    .map(policyMapper::toResponse);
        }
        UUID scopedCompanyId = accessService.resolveSearchCompany(authContext, companyId);
        return policyRepository.search(scopedCompanyId, patientProfileId, status, pageable)
                .map(policyMapper::toResponse);
    }

    @Transactional
    public PolicyResponse activatePolicy(UUID policyId) {
        Policy policy = policyById(policyId);
        if (policy.getStatus() == PolicyStatus.ACTIVE) {
            return policyMapper.toResponse(policy);
        }
        if (policy.getStatus() != PolicyStatus.PAYMENT_PENDING) {
            throw new PolicyException("INVALID_POLICY_STATUS", "Only payment pending policies can be activated");
        }
        Instant now = Instant.now();
        policy.activate(now);
        outboxEventService.saveEvent("Policy", policy.getId(), PolicyActivatedEvent.class.getSimpleName(), new PolicyActivatedEvent(
                policy.getId(),
                policy.getInsuranceCompanyId(),
                policy.getPatientProfileId(),
                now
        ));
        return policyMapper.toResponse(policy);
    }

    @Transactional
    public PolicyResponse cancelPolicy(UUID policyId, AuthContext authContext, String reason) {
        Policy policy = policyById(policyId);
        accessService.requireCanOperateForCompany(authContext, policy.getInsuranceCompanyId());
        Instant now = Instant.now();
        policy.cancel(now);
        outboxEventService.saveEvent("Policy", policy.getId(), PolicyCancelledEvent.class.getSimpleName(), new PolicyCancelledEvent(
                policy.getId(),
                policy.getInsuranceCompanyId(),
                policy.getPatientProfileId(),
                reason,
                now
        ));
        return policyMapper.toResponse(policy);
    }

    @Transactional
    public PolicyResponse suspendPolicy(UUID policyId, AuthContext authContext, String reason) {
        Policy policy = policyById(policyId);
        accessService.requireCanManageCompany(authContext, policy.getInsuranceCompanyId());
        policy.suspend(Instant.now());
        return policyMapper.toResponse(policy);
    }

    @Transactional(readOnly = true)
    public PolicyResponse getPolicyInternal(UUID policyId) {
        return policyMapper.toResponse(policyById(policyId));
    }

    @Transactional(readOnly = true)
    public boolean isPolicyActive(UUID policyId) {
        return policyById(policyId).getStatus() == PolicyStatus.ACTIVE;
    }

    private UUID resolveAgentProfileId(AuthContext authContext, UUID requestedAgentProfileId, UUID companyId) {
        UUID agentProfileId = requestedAgentProfileId;
        if (agentProfileId == null && authContext.hasRole(RoleConstants.AGENT)) {
            var summary = profileScopeClient.userSummary(authContext.userId());
            agentProfileId = summary == null ? null : summary.agentProfileId();
        }
        if (agentProfileId != null) {
            var agentCompany = profileScopeClient.agentCompany(agentProfileId);
            if (agentCompany == null || !companyId.equals(agentCompany.insuranceCompanyId())) {
                throw new PolicyException("FORBIDDEN", "Agent is outside insurance company scope");
            }
        }
        return agentProfileId;
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new PolicyException("INVALID_POLICY_DATE_RANGE", "Policy end date cannot be before start date");
        }
    }

    private String nextPolicyNumber() {
        String number;
        do {
            number = "POL-%d-%s".formatted(Year.now().getValue(), UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        } while (policyRepository.existsByPolicyNumber(number));
        return number;
    }

    private Policy policyById(UUID policyId) {
        return policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyException("POLICY_NOT_FOUND", "Policy was not found"));
    }

    private InsuranceProduct productById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new PolicyException("PRODUCT_NOT_FOUND", "Insurance product was not found"));
    }
}
