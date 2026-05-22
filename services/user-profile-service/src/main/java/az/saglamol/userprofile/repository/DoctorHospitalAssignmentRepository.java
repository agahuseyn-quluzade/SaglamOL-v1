package az.saglamol.userprofile.repository;

import az.saglamol.userprofile.entity.DoctorHospitalAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DoctorHospitalAssignmentRepository extends JpaRepository<DoctorHospitalAssignment, UUID> {

    boolean existsByDoctorProfileIdAndHospitalId(UUID doctorProfileId, UUID hospitalId);

    List<DoctorHospitalAssignment> findAllByHospitalId(UUID hospitalId);

    List<DoctorHospitalAssignment> findAllByDoctorProfileId(UUID doctorProfileId);
}
