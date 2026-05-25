package az.saglamol.userprofile.service;

import az.saglamol.userprofile.dto.response.UserProfileSummaryResponse;
import az.saglamol.userprofile.entity.AgentProfile;
import az.saglamol.userprofile.entity.DoctorProfile;
import az.saglamol.userprofile.entity.HospitalStatus;
import az.saglamol.userprofile.entity.InsuranceCompanyStatus;
import az.saglamol.userprofile.entity.PatientProfile;
import az.saglamol.userprofile.repository.AgentProfileRepository;
import az.saglamol.userprofile.repository.DoctorProfileRepository;
import az.saglamol.userprofile.repository.HospitalRepository;
import az.saglamol.userprofile.repository.HospitalStaffProfileRepository;
import az.saglamol.userprofile.repository.InsuranceCompanyRepository;
import az.saglamol.userprofile.repository.InsuranceCompanyStaffProfileRepository;
import az.saglamol.userprofile.repository.PatientProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InternalProfileQueryService {

    private final PatientProfileRepository patientRepository;
    private final DoctorProfileRepository doctorRepository;
    private final AgentProfileRepository agentRepository;
    private final HospitalStaffProfileRepository hospitalStaffRepository;
    private final InsuranceCompanyStaffProfileRepository insuranceStaffRepository;
    private final HospitalRepository hospitalRepository;
    private final InsuranceCompanyRepository insuranceCompanyRepository;

    public InternalProfileQueryService(
            PatientProfileRepository patientRepository,
            DoctorProfileRepository doctorRepository,
            AgentProfileRepository agentRepository,
            HospitalStaffProfileRepository hospitalStaffRepository,
            InsuranceCompanyStaffProfileRepository insuranceStaffRepository,
            HospitalRepository hospitalRepository,
            InsuranceCompanyRepository insuranceCompanyRepository
    ) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.agentRepository = agentRepository;
        this.hospitalStaffRepository = hospitalStaffRepository;
        this.insuranceStaffRepository = insuranceStaffRepository;
        this.hospitalRepository = hospitalRepository;
        this.insuranceCompanyRepository = insuranceCompanyRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileSummaryResponse summary(UUID iamUserId) {
        var patient = patientRepository.findByIamUserId(iamUserId);
        var doctor = doctorRepository.findByIamUserId(iamUserId);
        var agent = agentRepository.findByIamUserId(iamUserId);
        var hospitalStaff = hospitalStaffRepository.findByUserId(iamUserId);
        var insuranceStaff = insuranceStaffRepository.findByIamUserId(iamUserId);

        UUID patientProfileId = patient.map(PatientProfile::getId).orElse(null);
        UUID doctorProfileId = doctor.map(DoctorProfile::getId).orElse(null);
        UUID agentProfileId = agent.map(AgentProfile::getId).orElse(null);
        UUID agentCompanyId = agent.map(AgentProfile::getInsuranceCompanyId).orElse(null);
        UUID insuranceStaffCompanyId = insuranceStaff.map(staff -> staff.getInsuranceCompanyId()).orElse(null);

        return new UserProfileSummaryResponse(
                iamUserId,
                patientProfileId,
                doctorProfileId,
                agentProfileId,
                insuranceStaffCompanyId != null ? insuranceStaffCompanyId : agentCompanyId,
                hospitalStaff.map(staff -> staff.getId()).orElse(null),
                hospitalStaff.map(staff -> staff.getHospitalId()).orElse(null),
                hospitalStaff.map(staff -> staff.getBranchId()).orElse(null),
                patient.isPresent(),
                doctor.isPresent(),
                agent.isPresent(),
                hospitalStaff.isPresent(),
                insuranceStaff.isPresent()
        );
    }

    @Transactional(readOnly = true)
    public boolean patientExists(UUID patientProfileId) {
        return patientRepository.existsById(patientProfileId);
    }

    @Transactional(readOnly = true)
    public boolean doctorExists(UUID doctorProfileId) {
        return doctorRepository.existsById(doctorProfileId);
    }

    @Transactional(readOnly = true)
    public boolean hospitalActive(UUID hospitalId) {
        return hospitalRepository.existsByIdAndStatus(hospitalId, HospitalStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public boolean insuranceCompanyActive(UUID companyId) {
        return insuranceCompanyRepository.existsByIdAndStatus(companyId, InsuranceCompanyStatus.ACTIVE);
    }
}
