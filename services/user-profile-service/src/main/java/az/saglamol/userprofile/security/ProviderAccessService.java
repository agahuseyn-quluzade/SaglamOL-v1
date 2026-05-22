package az.saglamol.userprofile.security;

import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.userprofile.entity.DoctorProfile;
import az.saglamol.userprofile.entity.DoctorHospitalAssignment;
import az.saglamol.userprofile.entity.HospitalBranch;
import az.saglamol.userprofile.entity.HospitalStaffProfile;
import az.saglamol.userprofile.exception.UserProfileException;
import az.saglamol.userprofile.repository.DoctorHospitalAssignmentRepository;
import az.saglamol.userprofile.repository.DoctorProfileRepository;
import az.saglamol.userprofile.repository.HospitalBranchRepository;
import az.saglamol.userprofile.repository.HospitalStaffProfileRepository;
import az.saglamol.common.security.RoleConstants;
import az.saglamol.common.security.RoleChecker;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ProviderAccessService {

    private final RoleChecker roleChecker;
    private final HospitalStaffProfileRepository hospitalStaffProfileRepository;
    private final HospitalBranchRepository hospitalBranchRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DoctorHospitalAssignmentRepository doctorHospitalAssignmentRepository;

    public ProviderAccessService(
            RoleChecker roleChecker,
            HospitalStaffProfileRepository hospitalStaffProfileRepository,
            HospitalBranchRepository hospitalBranchRepository,
            DoctorProfileRepository doctorProfileRepository,
            DoctorHospitalAssignmentRepository doctorHospitalAssignmentRepository
    ) {
        this.roleChecker = roleChecker;
        this.hospitalStaffProfileRepository = hospitalStaffProfileRepository;
        this.hospitalBranchRepository = hospitalBranchRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.doctorHospitalAssignmentRepository = doctorHospitalAssignmentRepository;
    }

    public void requireCreateHospital() {
        roleChecker.requireRole(RoleConstants.ADMIN);
    }

    public void requireHospitalWrite(UUID hospitalId) {
        if (canManageHospital(currentUserId(), hospitalId)) {
            return;
        }
        throw forbidden("Hospital write permission is required");
    }

    public void requireHospitalRead(UUID hospitalId) {
        if (canViewHospital(currentUserId(), hospitalId)) {
            return;
        }
        throw forbidden("Hospital read permission is required");
    }

    public void requireBranchWrite(UUID branchId) {
        if (canManageBranch(currentUserId(), branchId)) {
            return;
        }
        throw forbidden("Hospital branch write permission is required");
    }

    public void requireBranchRead(UUID branchId) {
        if (canViewBranch(currentUserId(), branchId)) {
            return;
        }
        throw forbidden("Hospital branch read permission is required");
    }

    public void requireHospitalStaffManage(UUID hospitalId) {
        if (canManageHospitalStaff(currentUserId(), hospitalId)) {
            return;
        }
        throw forbidden("Hospital staff management permission is required");
    }

    public void requireDoctorAssignment(UUID hospitalId, UUID doctorProfileId) {
        if (canAssignDoctor(currentUserId(), hospitalId, doctorProfileId)) {
            return;
        }
        throw forbidden("Doctor assignment permission is required");
    }

    public boolean canManageHospital(UUID userId, UUID hospitalId) {
        if (roleChecker.isAdmin()) {
            return true;
        }
        return roleChecker.isHospitalAdmin()
                && hospitalStaffProfileRepository.findByUserId(userId)
                .map(staff -> hospitalId.equals(staff.getHospitalId()))
                .orElse(false);
    }

    public boolean canViewHospital(UUID userId, UUID hospitalId) {
        if (roleChecker.isAdmin() || roleChecker.isAgent()) {
            return true;
        }
        if (roleChecker.isHospitalAdmin() || roleChecker.isHospitalStaff()) {
            return hospitalStaffProfileRepository.findByUserId(userId)
                    .map(staff -> hospitalId.equals(staff.getHospitalId()))
                    .orElse(false);
        }
        return roleChecker.isDoctor() && canDoctorAccessHospital(userId, hospitalId);
    }

    public boolean canManageBranch(UUID userId, UUID branchId) {
        if (roleChecker.isAdmin()) {
            return true;
        }
        if (!roleChecker.isHospitalAdmin()) {
            return false;
        }
        return hospitalBranchRepository.findById(branchId)
                .map(branch -> canManageHospital(userId, branch.getHospitalId()))
                .orElse(false);
    }

    public boolean canViewBranch(UUID userId, UUID branchId) {
        Optional<HospitalBranch> branch = hospitalBranchRepository.findById(branchId);
        if (branch.isEmpty()) {
            return false;
        }
        UUID hospitalId = branch.get().getHospitalId();
        if (roleChecker.isAdmin() || roleChecker.isAgent()) {
            return true;
        }
        if (roleChecker.isHospitalAdmin()) {
            return canManageHospital(userId, hospitalId);
        }
        if (roleChecker.isHospitalStaff()) {
            return hospitalStaffProfileRepository.findByUserId(userId)
                    .map(staff -> hospitalId.equals(staff.getHospitalId())
                            && (staff.getBranchId() == null || branchId.equals(staff.getBranchId())))
                    .orElse(false);
        }
        return roleChecker.isDoctor() && canDoctorAccessBranch(userId, hospitalId, branchId);
    }

    public boolean canManageHospitalStaff(UUID userId, UUID hospitalId) {
        return canManageHospital(userId, hospitalId);
    }

    public boolean canAssignDoctor(UUID userId, UUID hospitalId, UUID doctorProfileId) {
        return doctorProfileRepository.existsById(doctorProfileId) && canManageHospital(userId, hospitalId);
    }

    public boolean canDoctorAccessHospital(UUID userId, UUID hospitalId) {
        return doctorProfileRepository.findByUserId(userId)
                .map(doctor -> doctorHospitalAssignmentRepository
                        .existsByDoctorProfileIdAndHospitalId(doctor.getId(), hospitalId))
                .orElse(false);
    }

    public boolean canDoctorAccessBranch(UUID userId, UUID hospitalId, UUID branchId) {
        return doctorProfileRepository.findByUserId(userId)
                .map(doctor -> doctorHospitalAssignmentRepository
                        .existsByDoctorProfileIdAndHospitalIdAndBranchId(doctor.getId(), hospitalId, branchId)
                        || doctorHospitalAssignmentRepository
                        .existsByDoctorProfileIdAndHospitalIdAndBranchIdIsNull(doctor.getId(), hospitalId))
                .orElse(false);
    }

    public boolean canViewHospitalStaffProfile(UUID userId, HospitalStaffProfile staffProfile) {
        if (roleChecker.isAdmin() || roleChecker.isAgent() || canManageHospital(userId, staffProfile.getHospitalId())) {
            return true;
        }
        if (!roleChecker.isHospitalStaff()) {
            return false;
        }
        return hospitalStaffProfileRepository.findByUserId(userId)
                .map(currentStaff -> staffProfile.getHospitalId().equals(currentStaff.getHospitalId())
                        && (currentStaff.getBranchId() == null
                        || currentStaff.getBranchId().equals(staffProfile.getBranchId())
                        || staffProfile.getUserId().equals(userId)))
                .orElse(false);
    }

    public boolean canViewDoctorAssignment(UUID userId, DoctorHospitalAssignment assignment) {
        if (roleChecker.isAdmin() || roleChecker.isAgent() || canManageHospital(userId, assignment.getHospitalId())) {
            return true;
        }
        if (roleChecker.isHospitalStaff()) {
            if (assignment.getBranchId() == null) {
                return canViewHospital(userId, assignment.getHospitalId());
            }
            return canViewBranch(userId, assignment.getBranchId());
        }
        if (roleChecker.isDoctor()) {
            return doctorProfileRepository.findByUserId(userId)
                    .map(doctor -> doctor.getId().equals(assignment.getDoctorProfileId()))
                    .orElse(false);
        }
        return false;
    }

    public Optional<Set<UUID>> readableHospitalScope() {
        if (roleChecker.isAdmin() || roleChecker.isAgent()) {
            return Optional.empty();
        }
        if (roleChecker.isHospitalAdmin() || roleChecker.isHospitalStaff()) {
            return Optional.of(Set.of(requiredHospitalStaff().getHospitalId()));
        }
        if (roleChecker.isDoctor()) {
            return Optional.of(readableDoctorHospitalIds());
        }
        throw forbidden("Hospital read permission is required");
    }

    public UUID currentUserId() {
        return AuthContextHolder.getRequired().userId();
    }

    public Optional<UUID> currentHospitalStaffBranchId() {
        if (!roleChecker.isHospitalStaff()) {
            return Optional.empty();
        }
        return hospitalStaffProfileRepository.findByUserId(currentUserId())
                .map(HospitalStaffProfile::getBranchId);
    }

    private HospitalStaffProfile requiredHospitalStaff() {
        UUID userId = currentUserId();
        return hospitalStaffProfileRepository.findByUserId(userId)
                .orElseThrow(() -> forbidden("Hospital staff profile is required"));
    }

    private Set<UUID> readableDoctorHospitalIds() {
        UUID userId = currentUserId();
        DoctorProfile doctor = doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> forbidden("Doctor profile is required"));
        return doctorHospitalAssignmentRepository.findAllByDoctorProfileId(doctor.getId()).stream()
                .map(assignment -> assignment.getHospitalId())
                .collect(Collectors.toSet());
    }

    private UserProfileException forbidden(String message) {
        return new UserProfileException("FORBIDDEN", message);
    }
}
