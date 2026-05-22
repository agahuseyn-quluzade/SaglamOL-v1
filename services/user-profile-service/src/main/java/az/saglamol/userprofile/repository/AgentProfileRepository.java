package az.saglamol.userprofile.repository;

import az.saglamol.userprofile.entity.AgentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AgentProfileRepository extends JpaRepository<AgentProfile, UUID> {

    Optional<AgentProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    boolean existsByEmployeeNo(String employeeNo);

    boolean existsByEmployeeNoAndUserIdNot(String employeeNo, UUID userId);
}
