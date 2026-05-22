package az.saglamol.userprofile.service;

import az.saglamol.userprofile.dto.request.AssignDoctorToHospitalRequest;
import az.saglamol.userprofile.dto.request.CreateHospitalBranchRequest;
import az.saglamol.userprofile.dto.request.CreateHospitalRequest;
import az.saglamol.userprofile.dto.request.CreateHospitalStaffRequest;
import az.saglamol.userprofile.dto.response.DoctorHospitalAssignmentResponse;
import az.saglamol.userprofile.dto.response.HospitalBranchResponse;
import az.saglamol.userprofile.dto.response.HospitalResponse;
import az.saglamol.userprofile.dto.response.HospitalStaffResponse;
import az.saglamol.userprofile.entity.DoctorHospitalAssignment;
import az.saglamol.userprofile.entity.Hospital;
import az.saglamol.userprofile.entity.HospitalBranch;
import az.saglamol.userprofile.entity.HospitalStaffProfile;
import az.saglamol.userprofile.entity.HospitalStatus;
import az.saglamol.userprofile.exception.UserProfileException;
import az.saglamol.userprofile.repository.DoctorHospitalAssignmentRepository;
import az.saglamol.userprofile.repository.DoctorProfileRepository;
import az.saglamol.userprofile.repository.HospitalBranchRepository;
import az.saglamol.userprofile.repository.HospitalRepository;
import az.saglamol.userprofile.repository.HospitalStaffProfileRepository;
import az.saglamol.userprofile.security.ProviderAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final HospitalBranchRepository hospitalBranchRepository;
    private final HospitalStaffProfileRepository hospitalStaffProfileRepository;
    private final DoctorHospitalAssignmentRepository doctorHospitalAssignmentRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final ProviderAccessService providerAccessService;

    public HospitalService(
            HospitalRepository hospitalRepository,
            HospitalBranchRepository hospitalBranchRepository,
            HospitalStaffProfileRepository hospitalStaffProfileRepository,
            DoctorHospitalAssignmentRepository doctorHospitalAssignmentRepository,
            DoctorProfileRepository doctorProfileRepository,
            ProviderAccessService providerAccessService
    ) {
        this.hospitalRepository = hospitalRepository;
        this.hospitalBranchRepository = hospitalBranchRepository;
        this.hospitalStaffProfileRepository = hospitalStaffProfileRepository;
        this.doctorHospitalAssignmentRepository = doctorHospitalAssignmentRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.providerAccessService = providerAccessService;
    }

    @Transactional
    public HospitalResponse createHospital(CreateHospitalRequest request) {
        providerAccessService.requireCreateHospital();
        if (hospitalRepository.existsByTaxId(request.taxId()) || hospitalRepository.existsByLicenseNo(request.licenseNo())) {
            throw new UserProfileException("HOSPITAL_ALREADY_EXISTS", "Hospital tax id or license number already exists");
        }
        Hospital hospital = new Hospital(
                UUID.randomUUID(),
                request.name(),
                request.taxId(),
                request.licenseNo(),
                request.phone(),
                request.email(),
                HospitalStatus.PENDING,
                Instant.now()
        );
        return toResponse(hospitalRepository.save(hospital));
    }

    @Transactional(readOnly = true)
    public List<HospitalResponse> hospitals() {
        var scope = providerAccessService.readableHospitalScope();
        var hospitals = scope
                .map(hospitalRepository::findAllById)
                .orElseGet(hospitalRepository::findAll);
        return hospitals.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public HospitalResponse hospital(UUID hospitalId) {
        Hospital hospital = findHospital(hospitalId);
        providerAccessService.requireHospitalRead(hospitalId);
        return toResponse(hospital);
    }

    @Transactional
    public HospitalBranchResponse createBranch(UUID hospitalId, CreateHospitalBranchRequest request) {
        findHospital(hospitalId);
        providerAccessService.requireHospitalWrite(hospitalId);
        HospitalBranch branch = new HospitalBranch(
                UUID.randomUUID(),
                hospitalId,
                request.name(),
                request.city(),
                request.addressLine(),
                request.phone(),
                Instant.now()
        );
        return toResponse(hospitalBranchRepository.save(branch));
    }

    @Transactional(readOnly = true)
    public List<HospitalBranchResponse> branches(UUID hospitalId) {
        findHospital(hospitalId);
        providerAccessService.requireHospitalRead(hospitalId);
        UUID userId = providerAccessService.currentUserId();
        return hospitalBranchRepository.findAllByHospitalId(hospitalId).stream()
                .filter(branch -> providerAccessService.canViewBranch(userId, branch.getId()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public HospitalStaffResponse createStaff(UUID hospitalId, CreateHospitalStaffRequest request) {
        findHospital(hospitalId);
        providerAccessService.requireHospitalStaffManage(hospitalId);
        if (request.branchId() != null) {
            validateBranch(hospitalId, request.branchId());
            providerAccessService.requireBranchWrite(request.branchId());
        }
        if (hospitalStaffProfileRepository.existsByUserId(request.userId())) {
            throw new UserProfileException("STAFF_ALREADY_EXISTS", "User already has a hospital staff profile");
        }
        HospitalStaffProfile staff = new HospitalStaffProfile(
                UUID.randomUUID(),
                request.userId(),
                hospitalId,
                request.branchId(),
                request.position(),
                Instant.now()
        );
        return toResponse(hospitalStaffProfileRepository.save(staff));
    }

    @Transactional(readOnly = true)
    public List<HospitalStaffResponse> staff(UUID hospitalId) {
        findHospital(hospitalId);
        providerAccessService.requireHospitalRead(hospitalId);
        UUID userId = providerAccessService.currentUserId();
        return hospitalStaffProfileRepository.findAllByHospitalId(hospitalId).stream()
                .filter(staff -> providerAccessService.canViewHospitalStaffProfile(userId, staff))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DoctorHospitalAssignmentResponse assignDoctor(UUID hospitalId, UUID doctorProfileId,
                                                         AssignDoctorToHospitalRequest request) {
        findHospital(hospitalId);
        if (!doctorProfileRepository.existsById(doctorProfileId)) {
            throw new UserProfileException("DOCTOR_NOT_FOUND", "Doctor profile was not found");
        }
        providerAccessService.requireDoctorAssignment(hospitalId, doctorProfileId);
        if (request.branchId() != null) {
            validateBranch(hospitalId, request.branchId());
            providerAccessService.requireBranchWrite(request.branchId());
        }
        if (doctorHospitalAssignmentRepository.existsByDoctorProfileIdAndHospitalId(doctorProfileId, hospitalId)) {
            throw new UserProfileException("DOCTOR_ALREADY_ASSIGNED", "Doctor is already assigned to this hospital");
        }
        DoctorHospitalAssignment assignment = new DoctorHospitalAssignment(
                UUID.randomUUID(),
                doctorProfileId,
                hospitalId,
                request.branchId(),
                request.department(),
                request.primaryAssignment(),
                Instant.now()
        );
        return toResponse(doctorHospitalAssignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public List<DoctorHospitalAssignmentResponse> doctorAssignments(UUID hospitalId) {
        findHospital(hospitalId);
        providerAccessService.requireHospitalRead(hospitalId);
        UUID userId = providerAccessService.currentUserId();
        return doctorHospitalAssignmentRepository.findAllByHospitalId(hospitalId).stream()
                .filter(assignment -> providerAccessService.canViewDoctorAssignment(userId, assignment))
                .map(this::toResponse)
                .toList();
    }

    private Hospital findHospital(UUID hospitalId) {
        return hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new UserProfileException("HOSPITAL_NOT_FOUND", "Hospital was not found"));
    }

    private void validateBranch(UUID hospitalId, UUID branchId) {
        HospitalBranch branch = hospitalBranchRepository.findById(branchId)
                .orElseThrow(() -> new UserProfileException("BRANCH_NOT_FOUND", "Hospital branch was not found"));
        if (!hospitalId.equals(branch.getHospitalId())) {
            throw new UserProfileException("BRANCH_NOT_FOUND", "Hospital branch was not found");
        }
    }

    private HospitalResponse toResponse(Hospital hospital) {
        return new HospitalResponse(
                hospital.getId(),
                hospital.getName(),
                hospital.getTaxId(),
                hospital.getLicenseNo(),
                hospital.getPhone(),
                hospital.getEmail(),
                hospital.getStatus().name(),
                hospital.getCreatedAt()
        );
    }

    private HospitalBranchResponse toResponse(HospitalBranch branch) {
        return new HospitalBranchResponse(
                branch.getId(),
                branch.getHospitalId(),
                branch.getName(),
                branch.getCity(),
                branch.getAddressLine(),
                branch.getPhone(),
                branch.getCreatedAt()
        );
    }

    private HospitalStaffResponse toResponse(HospitalStaffProfile staff) {
        return new HospitalStaffResponse(
                staff.getId(),
                staff.getUserId(),
                staff.getHospitalId(),
                staff.getBranchId(),
                staff.getPosition(),
                staff.getCreatedAt()
        );
    }

    private DoctorHospitalAssignmentResponse toResponse(DoctorHospitalAssignment assignment) {
        return new DoctorHospitalAssignmentResponse(
                assignment.getId(),
                assignment.getDoctorProfileId(),
                assignment.getHospitalId(),
                assignment.getBranchId(),
                assignment.getDepartment(),
                assignment.isPrimaryAssignment(),
                assignment.getCreatedAt()
        );
    }
}
