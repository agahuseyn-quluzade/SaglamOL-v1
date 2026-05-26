package az.saglamol.policy.service;

import az.saglamol.policy.dto.request.EligibilityCheckRequest;
import az.saglamol.policy.dto.response.EligibilityCheckResponse;
import az.saglamol.policy.entity.CoverageRule;
import az.saglamol.policy.entity.InsuranceProduct;
import az.saglamol.policy.entity.InsuranceProductStatus;
import az.saglamol.policy.entity.Policy;
import az.saglamol.policy.entity.PolicyStatus;
import az.saglamol.policy.entity.ProviderContract;
import az.saglamol.policy.entity.ProviderContractStatus;
import az.saglamol.policy.entity.RuleStatus;
import az.saglamol.policy.exception.PolicyException;
import az.saglamol.policy.repository.CoverageRuleRepository;
import az.saglamol.policy.repository.InsuranceProductRepository;
import az.saglamol.policy.repository.PolicyRepository;
import az.saglamol.policy.repository.ProviderContractRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EligibilityService {

    private final PolicyRepository policyRepository;
    private final InsuranceProductRepository productRepository;
    private final CoverageRuleRepository coverageRuleRepository;
    private final ProviderContractRepository contractRepository;

    public EligibilityService(
            PolicyRepository policyRepository,
            InsuranceProductRepository productRepository,
            CoverageRuleRepository coverageRuleRepository,
            ProviderContractRepository contractRepository
    ) {
        this.policyRepository = policyRepository;
        this.productRepository = productRepository;
        this.coverageRuleRepository = coverageRuleRepository;
        this.contractRepository = contractRepository;
    }

    @Transactional(readOnly = true)
    public EligibilityCheckResponse checkEligibility(EligibilityCheckRequest request) {
        Policy policy = policyRepository.findById(request.policyId())
                .orElseThrow(() -> new PolicyException("POLICY_NOT_FOUND", "Policy was not found"));
        InsuranceProduct product = productRepository.findById(policy.getProductId())
                .orElseThrow(() -> new PolicyException("PRODUCT_NOT_FOUND", "Insurance product was not found"));

        List<String> reasons = new ArrayList<>();
        CoverageRule rule = null;
        boolean inNetwork = true;

        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            reasons.add("POLICY_NOT_ACTIVE");
        }
        if (!policy.getInsuranceCompanyId().equals(request.insuranceCompanyId())) {
            reasons.add("INSURANCE_COMPANY_MISMATCH");
        }
        if (!policy.getPatientProfileId().equals(request.patientProfileId())) {
            reasons.add("PATIENT_PROFILE_MISMATCH");
        }
        if (request.treatmentDate().isBefore(policy.getStartDate()) || request.treatmentDate().isAfter(policy.getEndDate())) {
            reasons.add("TREATMENT_DATE_OUTSIDE_POLICY_RANGE");
        }
        if (product.getStatus() != InsuranceProductStatus.ACTIVE) {
            reasons.add("PRODUCT_NOT_ACTIVE");
        }

        var ruleOptional = coverageRuleRepository.findByProductIdAndServiceTypeAndStatus(
                product.getId(),
                request.serviceType(),
                RuleStatus.ACTIVE
        );
        if (ruleOptional.isEmpty()) {
            reasons.add("COVERAGE_RULE_NOT_FOUND");
        } else {
            rule = ruleOptional.get();
            LocalDate eligibleFrom = policy.getStartDate().plusDays(rule.getWaitingPeriodDays());
            if (request.treatmentDate().isBefore(eligibleFrom)) {
                reasons.add("WAITING_PERIOD_NOT_PASSED");
            }
        }

        BigDecimal availableLimit = policy.availableLimit();
        if (availableLimit.compareTo(request.claimAmount()) < 0) {
            reasons.add("LIMIT_EXCEEDED");
        }

        if (request.hospitalId() != null) {
            inNetwork = hasActiveProviderContract(
                    request.insuranceCompanyId(),
                    request.hospitalId(),
                    product.getId(),
                    request.treatmentDate()
            );
            if (!inNetwork) {
                reasons.add("OUT_OF_NETWORK_PROVIDER");
            }
        }

        BigDecimal coveredAmount = rule == null
                ? BigDecimal.ZERO
                : coveredAmount(request.claimAmount(), rule);

        return new EligibilityCheckResponse(
                reasons.isEmpty(),
                request.policyId(),
                request.insuranceCompanyId(),
                request.patientProfileId(),
                request.hospitalId(),
                request.serviceType(),
                request.claimAmount(),
                coveredAmount,
                availableLimit,
                rule == null ? null : rule.getCoveragePercent(),
                inNetwork,
                List.copyOf(reasons)
        );
    }

    private boolean hasActiveProviderContract(UUID companyId, UUID hospitalId, UUID productId, LocalDate treatmentDate) {
        return contractRepository.findByInsuranceCompanyIdAndHospitalIdAndStatus(
                        companyId,
                        hospitalId,
                        ProviderContractStatus.ACTIVE
                ).stream()
                .filter(contract -> includesDate(contract, treatmentDate))
                .anyMatch(contract -> contract.getProductId() == null || contract.getProductId().equals(productId));
    }

    private boolean includesDate(ProviderContract contract, LocalDate date) {
        return !date.isBefore(contract.getStartDate()) && !date.isAfter(contract.getEndDate());
    }

    private BigDecimal coveredAmount(BigDecimal claimAmount, CoverageRule rule) {
        BigDecimal percentCovered = claimAmount
                .multiply(BigDecimal.valueOf(rule.getCoveragePercent()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return percentCovered.min(rule.getMaxAmount());
    }
}
