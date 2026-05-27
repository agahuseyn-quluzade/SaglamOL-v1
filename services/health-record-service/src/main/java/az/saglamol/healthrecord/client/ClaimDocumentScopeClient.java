package az.saglamol.healthrecord.client;

import az.saglamol.common.security.AuthContext;

import java.util.UUID;

public interface ClaimDocumentScopeClient {
    boolean canAccessClaimDocuments(AuthContext authContext, UUID claimId);
}
