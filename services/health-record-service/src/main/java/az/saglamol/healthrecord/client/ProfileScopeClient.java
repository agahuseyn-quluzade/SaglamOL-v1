package az.saglamol.healthrecord.client;

import java.util.UUID;

public interface ProfileScopeClient {
    UserProfileSummaryResponse userSummary(UUID iamUserId);
}
