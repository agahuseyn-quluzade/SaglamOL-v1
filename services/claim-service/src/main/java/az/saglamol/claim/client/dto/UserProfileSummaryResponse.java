package az.saglamol.claim.client.dto;

import java.util.UUID;

public record UserProfileSummaryResponse(
        UUID iamUserId,
        UUID patientProfileId,
        UUID doctorProfileId,
        UUID agentProfileId,
        UUID insuranceCompanyId,
        UUID hospitalStaffProfileId,
        UUID hospitalId,
        UUID hospitalBranchId,
        boolean hasPatientProfile,
        boolean hasDoctorProfile,
        boolean hasAgentProfile,
        boolean hasHospitalStaffProfile,
        boolean hasInsuranceStaffProfile
) {
}
