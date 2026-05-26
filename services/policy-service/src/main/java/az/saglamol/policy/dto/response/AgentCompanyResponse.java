package az.saglamol.policy.dto.response;

import java.util.UUID;

public record AgentCompanyResponse(
        UUID agentProfileId,
        UUID insuranceCompanyId,
        String companyName
) {
}
