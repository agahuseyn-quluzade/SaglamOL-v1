package az.saglamol.healthrecord.service;

import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.healthrecord.client.ClaimDocumentScopeClient;
import az.saglamol.healthrecord.client.ProfileScopeClient;
import az.saglamol.healthrecord.client.UserProfileSummaryResponse;
import az.saglamol.healthrecord.entity.HealthRecord;
import az.saglamol.healthrecord.entity.MedicalDocument;
import az.saglamol.healthrecord.exception.HealthRecordException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HealthRecordAccessService {

    private final ProfileScopeClient profileScopeClient;
    private final ClaimDocumentScopeClient claimScopeClient;

    public HealthRecordAccessService(ProfileScopeClient profileScopeClient, ClaimDocumentScopeClient claimScopeClient) {
        this.profileScopeClient = profileScopeClient;
        this.claimScopeClient = claimScopeClient;
    }

    public void requireCanCreateRecord(AuthContext authContext, UUID patientProfileId, UUID doctorProfileId, UUID hospitalId) {
        if (isAdmin(authContext)) {
            return;
        }
        UserProfileSummaryResponse summary = summary(authContext);
        if (authContext.hasRole(RoleConstants.PATIENT) && patientProfileId.equals(summary.patientProfileId())) {
            return;
        }
        if (authContext.hasRole(RoleConstants.DOCTOR) && doctorProfileId != null && doctorProfileId.equals(summary.doctorProfileId())) {
            return;
        }
        if (authContext.hasAnyRole(RoleConstants.HOSPITAL_STAFF, RoleConstants.HOSPITAL_ADMIN)
                && hospitalId != null && hospitalId.equals(summary.hospitalId())) {
            return;
        }
        throw forbidden("Health record creation is forbidden");
    }

    public void requireCanViewRecord(AuthContext authContext, HealthRecord record) {
        if (canViewRecord(authContext, record)) {
            return;
        }
        throw forbidden("Health record access is forbidden");
    }

    public boolean canViewRecord(AuthContext authContext, HealthRecord record) {
        if (isAdmin(authContext)) {
            return true;
        }
        UserProfileSummaryResponse summary = summary(authContext);
        if (authContext.hasRole(RoleConstants.PATIENT) && record.getPatientProfileId().equals(summary.patientProfileId())) {
            return true;
        }
        if (authContext.hasRole(RoleConstants.DOCTOR) && record.getDoctorProfileId() != null
                && record.getDoctorProfileId().equals(summary.doctorProfileId())) {
            return true;
        }
        if (authContext.hasAnyRole(RoleConstants.HOSPITAL_STAFF, RoleConstants.HOSPITAL_ADMIN)
                && record.getHospitalId() != null && record.getHospitalId().equals(summary.hospitalId())) {
            return true;
        }
        return record.getClaimId() != null
                && authContext.hasAnyRole(RoleConstants.AGENT, RoleConstants.INSURANCE_ADMIN, RoleConstants.INSURANCE_STAFF)
                && claimScopeClient.canAccessClaimDocuments(authContext, record.getClaimId());
    }

    public void requireCanUploadDocument(AuthContext authContext, UUID patientProfileId, UUID hospitalId, UUID claimId) {
        if (isAdmin(authContext)) {
            return;
        }
        UserProfileSummaryResponse summary = summary(authContext);
        if (authContext.hasRole(RoleConstants.PATIENT) && patientProfileId.equals(summary.patientProfileId())) {
            return;
        }
        if (authContext.hasAnyRole(RoleConstants.HOSPITAL_STAFF, RoleConstants.HOSPITAL_ADMIN)
                && hospitalId != null && hospitalId.equals(summary.hospitalId())) {
            return;
        }
        if (claimId != null && authContext.hasAnyRole(RoleConstants.AGENT, RoleConstants.INSURANCE_ADMIN, RoleConstants.INSURANCE_STAFF)
                && claimScopeClient.canAccessClaimDocuments(authContext, claimId)) {
            return;
        }
        throw forbidden("Document upload is forbidden");
    }

    public void requireCanViewDocument(AuthContext authContext, MedicalDocument document) {
        if (canViewDocument(authContext, document)) {
            return;
        }
        throw forbidden("Medical document access is forbidden");
    }

    public boolean canViewDocument(AuthContext authContext, MedicalDocument document) {
        if (isAdmin(authContext)) {
            return true;
        }
        UserProfileSummaryResponse summary = summary(authContext);
        if (authContext.hasRole(RoleConstants.PATIENT) && document.getPatientProfileId().equals(summary.patientProfileId())) {
            return true;
        }
        if (authContext.hasAnyRole(RoleConstants.HOSPITAL_STAFF, RoleConstants.HOSPITAL_ADMIN)
                && document.getHospitalId() != null && document.getHospitalId().equals(summary.hospitalId())) {
            return true;
        }
        return document.getClaimId() != null
                && authContext.hasAnyRole(RoleConstants.AGENT, RoleConstants.INSURANCE_ADMIN, RoleConstants.INSURANCE_STAFF)
                && claimScopeClient.canAccessClaimDocuments(authContext, document.getClaimId());
    }

    public UUID currentPatientProfileId(AuthContext authContext) {
        UUID patientProfileId = summary(authContext).patientProfileId();
        if (patientProfileId == null) {
            throw forbidden("Patient profile is required");
        }
        return patientProfileId;
    }

    private UserProfileSummaryResponse summary(AuthContext authContext) {
        return profileScopeClient.userSummary(authContext.userId());
    }

    private boolean isAdmin(AuthContext authContext) {
        return authContext.hasRole(RoleConstants.ADMIN);
    }

    private HealthRecordException forbidden(String message) {
        return new HealthRecordException("FORBIDDEN", message);
    }
}
