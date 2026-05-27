package az.saglamol.airisk.service;

import az.saglamol.airisk.client.ProfileScopeClient;
import az.saglamol.airisk.client.UserProfileSummaryResponse;
import az.saglamol.airisk.entity.AiRiskAssessment;
import az.saglamol.airisk.exception.AiRiskException;
import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AiRiskAccessService {

    private final ProfileScopeClient profileScopeClient;

    public AiRiskAccessService(ProfileScopeClient profileScopeClient) {
        this.profileScopeClient = profileScopeClient;
    }

    public void requireCanAssess(AuthContext authContext, UUID companyId) {
        if (authContext.hasRole(RoleConstants.PATIENT)) {
            throw new AiRiskException("PATIENT_FORBIDDEN", "Patients cannot access AI risk assessments");
        }
        if (authContext.hasRole(RoleConstants.ADMIN)) {
            return;
        }
        UserProfileSummaryResponse summary = profileScopeClient.userSummary(authContext.userId());
        if (authContext.hasAnyRole(RoleConstants.INSURANCE_ADMIN, RoleConstants.AGENT, RoleConstants.INSURANCE_STAFF)
                && companyId != null && companyId.equals(summary.insuranceCompanyId())) {
            return;
        }
        throw new AiRiskException("FORBIDDEN", "AI risk access is forbidden");
    }

    public void requireCanView(AuthContext authContext, AiRiskAssessment assessment) {
        requireCanAssess(authContext, assessment.getInsuranceCompanyId());
    }

    public UUID scopedCompany(AuthContext authContext, UUID requestedCompanyId) {
        if (authContext.hasRole(RoleConstants.ADMIN)) {
            return requestedCompanyId;
        }
        if (authContext.hasRole(RoleConstants.PATIENT)) {
            throw new AiRiskException("PATIENT_FORBIDDEN", "Patients cannot access AI risk summaries");
        }
        UserProfileSummaryResponse summary = profileScopeClient.userSummary(authContext.userId());
        if (authContext.hasAnyRole(RoleConstants.INSURANCE_ADMIN, RoleConstants.AGENT, RoleConstants.INSURANCE_STAFF)) {
            if (requestedCompanyId != null && !requestedCompanyId.equals(summary.insuranceCompanyId())) {
                throw new AiRiskException("FORBIDDEN", "Requested company is outside current scope");
            }
            return summary.insuranceCompanyId();
        }
        throw new AiRiskException("FORBIDDEN", "AI risk company summary access is forbidden");
    }

    public boolean canSeeRawProviderResponse(AuthContext authContext) {
        return authContext.hasRole(RoleConstants.ADMIN);
    }
}
