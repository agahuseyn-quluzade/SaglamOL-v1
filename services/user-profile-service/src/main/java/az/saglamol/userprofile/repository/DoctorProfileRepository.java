package az.saglamol.userprofile.repository;

import az.saglamol.userprofile.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, UUID> {
}
