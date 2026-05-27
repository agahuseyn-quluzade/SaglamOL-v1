package az.saglamol.airisk.service;

import az.saglamol.airisk.client.ClaimDetailResponse;
import az.saglamol.airisk.client.FraudSummaryResponse;
import az.saglamol.airisk.client.PolicyDetailResponse;
import az.saglamol.airisk.model.AiRiskPayload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AiPromptBuilder {

    public AiRiskPayload build(ClaimDetailResponse claim, PolicyDetailResponse policy, FraudSummaryResponse fraudSummary) {
        AiRiskPayload.PolicyContext policyContext = policy == null ? null : new AiRiskPayload.PolicyContext(
                policy.status(),
                policy.startDate(),
                policy.endDate(),
                policy.annualLimit(),
                policy.usedLimit(),
                policy.reservedLimit()
        );
        AiRiskPayload.FraudContext fraudContext = fraudSummary == null ? null : new AiRiskPayload.FraudContext(
                fraudSummary.averageFraudScore() == null ? BigDecimal.ZERO : fraudSummary.averageFraudScore(),
                fraudSummary.completedCount(),
                fraudSummary.failedCount()
        );
        return new AiRiskPayload(
                claim.id(),
                claim.insuranceCompanyId(),
                claim.policyId(),
                claim.hospitalId(),
                claim.doctorProfileId(),
                claim.serviceType(),
                claim.treatmentDate(),
                claim.claimAmount(),
                claim.status(),
                policyContext,
                fraudContext
        );
    }

    public String systemPrompt() {
        return """
                You are a healthcare insurance risk model. Return only valid JSON with keys:
                riskScore (number 0-1), riskLevel (LOW/MEDIUM/HIGH/CRITICAL), confidence (number 0-1), reasons (array of short strings).
                Use only the provided minimized claim, policy and fraud context. Do not infer personal identity.
                """;
    }
}
