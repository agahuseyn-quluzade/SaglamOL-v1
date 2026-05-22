package az.saglamol.userprofile.security;

import az.saglamol.common.security.RoleConstants;
import az.saglamol.common.security.RoleChecker;
import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.userprofile.entity.DoctorProfile;
import az.saglamol.userprofile.entity.HospitalStaffProfile;
import az.saglamol.userprofile.exception.UserProfileException;
import az.saglamol.userprofile.repository.DoctorHospitalAssignmentRepository;
import az.saglamol.userprofile.repository.DoctorProfileRepository;
import az.saglamol.userprofile.repository.HospitalStaffProfileRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ProviderAccessService {

    private final RoleChecker roleChecker;
    private final HospitalStaffProfileRepository hospitalStaffProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DoctorHospitalAssignmentRepository doctorHospitalAssignmentRepository;

    public ProviderAccessService(
            RoleChecker roleChecker,
            HospitalStaffProfileRepository hospitalStaffProfileRepository,
            DoctorProfileRepository doctorProfileRepository,
            DoctorHospitalAssignmentRepository doctorHospitalAssignmentRepository
    ) {
        this.roleChecker = roleChecker;
        this.hospitalStaffProfileRepository = hospitalStaffProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.doctorHospitalAssignmentRepository = doctorHospitalAssignmentRepository;
    }

    public void requireCreateHospital() {
        roleChecker.requireRole(RoleConstants.ADMIN);
    }

    public void requireHospitalWrite(UUID hospitalId) {
        if (roleChecker.isAdmin()) {
            return;
        }
        if (roleChecker.isHospitalAdmin() && hospitalId.equals(requiredHospitalStaff().getHospitalId())) {
            return;
        }
        throw forbidden("Hospital write permission is required");
    }

    public void requireHospitalRead(UUID hospitalId) {
        if (roleChecker.isAdmin() || roleChecker.isAgent()) {
            return;
        }
        if ((roleChecker.isHospitalAdmin() || roleChecker.isHospitalStaff())
                && hospitalId.equals(requiredHospitalStaff().getHospitalId())) {
            return;
        }
        if (roleChecker.isDoctor() && readableDoctorHospitalIds().contains(hospitalId)) {
            return;
        }
        throw forbidden("Hospital read permission is required");
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

    private HospitalStaffProfile requiredHospitalStaff() {
        UUID userId = AuthContextHolder.getRequired().userId();
        return hospitalStaffProfileRepository.findByUserId(userId)
                .orElseThrow(() -> forbidden("Hospital staff profile is required"));
    }

    private Set<UUID> readableDoctorHospitalIds() {
        UUID userId = AuthContextHolder.getRequired().userId();
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
