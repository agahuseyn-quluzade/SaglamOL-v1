package az.saglamol.fraud.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.fraud.client.ProfileScopeClient;
import az.saglamol.fraud.client.UserProfileSummaryResponse;
import az.saglamol.fraud.entity.FraudAssessment;
import az.saglamol.fraud.exception.FraudException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FraudAccessService {

    private final ProfileScopeClient profileScopeClient;

    public FraudAccessService(ProfileScopeClient profileScopeClient) {
        this.profileScopeClient = profileScopeClient;
    }

    public void requireCanRunCheck(AuthContext authContext, UUID companyId, UUID hospitalId) {
        if (authContext.hasRole(RoleConstants.PATIENT)) {
            throw new FraudException("PATIENT_FORBIDDEN", "Patients cannot access fraud checks");
        }
        if (authContext.hasRole(RoleConstants.ADMIN)) {
            return;
        }
        UserProfileSummaryResponse summary = profileScopeClient.userSummary(authContext.userId());
        if (authContext.hasAnyRole(RoleConstants.INSURANCE_ADMIN, RoleConstants.AGENT, RoleConstants.INSURANCE_STAFF)
                && companyId != null && companyId.equals(summary.insuranceCompanyId())) {
            return;
        }
        if (authContext.hasRole(RoleConstants.HOSPITAL_ADMIN)
                && hospitalId != null && hospitalId.equals(summary.hospitalId())) {
            return;
        }
        throw new FraudException("FORBIDDEN", "Fraud access is forbidden");
    }

    public void requireCanView(AuthContext authContext, FraudAssessment assessment) {
        requireCanRunCheck(authContext, assessment.getInsuranceCompanyId(), assessment.getHospitalId());
    }

    public UUID scopedCompany(AuthContext authContext, UUID requestedCompanyId) {
        if (authContext.hasRole(RoleConstants.ADMIN)) {
            return requestedCompanyId;
        }
        if (authContext.hasRole(RoleConstants.PATIENT)) {
            throw new FraudException("PATIENT_FORBIDDEN", "Patients cannot access fraud checks");
        }
        UserProfileSummaryResponse summary = profileScopeClient.userSummary(authContext.userId());
        if (authContext.hasAnyRole(RoleConstants.INSURANCE_ADMIN, RoleConstants.AGENT, RoleConstants.INSURANCE_STAFF)) {
            if (requestedCompanyId != null && !requestedCompanyId.equals(summary.insuranceCompanyId())) {
                throw new FraudException("FORBIDDEN", "Requested company is outside current scope");
            }
            return summary.insuranceCompanyId();
        }
        throw new FraudException("FORBIDDEN", "Fraud company summary access is forbidden");
    }

    public UUID scopedHospital(AuthContext authContext, UUID requestedHospitalId) {
        if (authContext.hasRole(RoleConstants.ADMIN)) {
            return requestedHospitalId;
        }
        UserProfileSummaryResponse summary = profileScopeClient.userSummary(authContext.userId());
        if (authContext.hasRole(RoleConstants.HOSPITAL_ADMIN)) {
            if (requestedHospitalId != null && !requestedHospitalId.equals(summary.hospitalId())) {
                throw new FraudException("FORBIDDEN", "Requested hospital is outside current scope");
            }
            return summary.hospitalId();
        }
        throw new FraudException("FORBIDDEN", "Fraud hospital summary access is forbidden");
    }
}
