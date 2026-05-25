package az.saglamol.policy.client;

import az.saglamol.policy.client.dto.InsuranceScopeResponse;
import az.saglamol.policy.client.dto.UserProfileSummaryResponse;

import java.util.UUID;

public interface ProfileScopeClient {
    InsuranceScopeResponse insuranceScope(UUID iamUserId);

    UserProfileSummaryResponse userSummary(UUID iamUserId);
}
