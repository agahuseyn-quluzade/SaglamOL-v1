package az.saglamol.userprofile.dto.response;

import java.util.UUID;

public record AgentCompanyResponse(
        UUID agentProfileId,
        UUID insuranceCompanyId,
        String companyName
) {
}
