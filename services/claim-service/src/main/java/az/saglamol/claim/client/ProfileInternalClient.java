package az.saglamol.claim.client;

import az.saglamol.claim.client.dto.UserProfileSummaryResponse;

import java.util.UUID;

public interface ProfileInternalClient {
    UserProfileSummaryResponse userSummary(UUID iamUserId);
}
