package az.saglamol.policy.client;

import az.saglamol.policy.client.dto.InsuranceScopeResponse;
import az.saglamol.policy.client.dto.UserProfileSummaryResponse;
import az.saglamol.policy.dto.response.AgentCompanyResponse;

import java.util.UUID;

public interface ProfileScopeClient {
    InsuranceScopeResponse insuranceScope(UUID iamUserId);

    UserProfileSummaryResponse userSummary(UUID iamUserId);

    boolean patientExists(UUID patientProfileId);

    AgentCompanyResponse agentCompany(UUID agentProfileId);
}
