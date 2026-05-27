package az.saglamol.claim.service;

import az.saglamol.claim.client.ProfileInternalClient;
import az.saglamol.claim.client.dto.PolicyDetailResponse;
import az.saglamol.claim.client.dto.UserProfileSummaryResponse;
import az.saglamol.claim.entity.Claim;
import az.saglamol.claim.exception.ClaimException;
import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ClaimAccessService {

    private final ProfileInternalClient profileClient;

    public ClaimAccessService(ProfileInternalClient profileClient) {
        this.profileClient = profileClient;
    }

    public void requireCanCreateClaim(AuthContext authContext, PolicyDetailResponse policy, UUID requestPatientId, UUID hospitalId) {
        if (isAdmin(authContext)) {
            return;
        }
        UserProfileSummaryResponse summary = summary(authContext);
        if (authContext.hasRole(RoleConstants.PATIENT)
                && policy.patientProfileId().equals(summary.patientProfileId())
                && (requestPatientId == null || requestPatientId.equals(policy.patientProfileId()))) {
            return;
        }
        if (authContext.hasRole(RoleConstants.HOSPITAL_STAFF)
                && hospitalId != null
                && hospitalId.equals(summary.hospitalId())) {
            return;
        }
        throw forbidden("Claim creation is forbidden");
    }

    public void requireCanMutateDraft(AuthContext authContext, Claim claim) {
        requireCanViewClaim(authContext, claim);
    }

    public void requireCanViewClaim(AuthContext authContext, Claim claim) {
        if (canViewClaim(authContext, claim)) {
            return;
        }
        throw forbidden("Claim access is forbidden");
    }

    public void requireCanReviewClaim(AuthContext authContext, Claim claim) {
        if (isAdmin(authContext)) {
            return;
        }
        UserProfileSummaryResponse summary = summary(authContext);
        if (claim.getInsuranceCompanyId().equals(summary.insuranceCompanyId())
                && authContext.hasAnyRole(RoleConstants.AGENT, RoleConstants.INSURANCE_ADMIN, RoleConstants.INSURANCE_STAFF)) {
            return;
        }
        throw forbidden("Claim review permission is required");
    }

    public UUID resolveMyPatientProfileId(AuthContext authContext) {
        UserProfileSummaryResponse summary = summary(authContext);
        if (summary.patientProfileId() == null) {
            throw forbidden("Patient profile is required");
        }
        return summary.patientProfileId();
    }

    public ClaimSearchScope resolveSearchScope(AuthContext authContext, UUID requestedCompanyId,
                                               UUID requestedPatientId, UUID requestedHospitalId) {
        if (isAdmin(authContext)) {
            return new ClaimSearchScope(requestedCompanyId, requestedPatientId, requestedHospitalId);
        }
        UserProfileSummaryResponse summary = summary(authContext);
        if (authContext.hasRole(RoleConstants.PATIENT)) {
            UUID patientId = summary.patientProfileId();
            if (patientId == null || (requestedPatientId != null && !requestedPatientId.equals(patientId))) {
                throw forbidden("Requested patient is outside current user scope");
            }
            return new ClaimSearchScope(requestedCompanyId, patientId, requestedHospitalId);
        }
        if (authContext.hasRole(RoleConstants.HOSPITAL_STAFF)) {
            UUID hospitalId = summary.hospitalId();
            if (hospitalId == null || (requestedHospitalId != null && !requestedHospitalId.equals(hospitalId))) {
                throw forbidden("Requested hospital is outside current user scope");
            }
            return new ClaimSearchScope(requestedCompanyId, requestedPatientId, hospitalId);
        }
        if (authContext.hasAnyRole(RoleConstants.AGENT, RoleConstants.INSURANCE_ADMIN, RoleConstants.INSURANCE_STAFF)) {
            UUID companyId = summary.insuranceCompanyId();
            if (companyId == null || (requestedCompanyId != null && !requestedCompanyId.equals(companyId))) {
                throw forbidden("Requested company is outside current user scope");
            }
            return new ClaimSearchScope(companyId, requestedPatientId, requestedHospitalId);
        }
        throw forbidden("Claim search permission is required");
    }

    private boolean canViewClaim(AuthContext authContext, Claim claim) {
        if (isAdmin(authContext)) {
            return true;
        }
        UserProfileSummaryResponse summary = summary(authContext);
        if (authContext.hasRole(RoleConstants.PATIENT) && claim.getPatientProfileId().equals(summary.patientProfileId())) {
            return true;
        }
        if (authContext.hasRole(RoleConstants.HOSPITAL_STAFF) && claim.getHospitalId() != null
                && claim.getHospitalId().equals(summary.hospitalId())) {
            return true;
        }
        return authContext.hasAnyRole(RoleConstants.AGENT, RoleConstants.INSURANCE_ADMIN, RoleConstants.INSURANCE_STAFF)
                && claim.getInsuranceCompanyId().equals(summary.insuranceCompanyId());
    }

    private UserProfileSummaryResponse summary(AuthContext authContext) {
        return profileClient.userSummary(authContext.userId());
    }

    private boolean isAdmin(AuthContext authContext) {
        return authContext.hasRole(RoleConstants.ADMIN);
    }

    private ClaimException forbidden(String message) {
        return new ClaimException("FORBIDDEN", message);
    }

    public record ClaimSearchScope(UUID companyId, UUID patientProfileId, UUID hospitalId) {
    }
}
