package az.saglamol.userprofile.repository;

import az.saglamol.userprofile.entity.AgentProfile;
import az.saglamol.userprofile.entity.ProfileStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AgentProfileRepository extends JpaRepository<AgentProfile, UUID> {

    Optional<AgentProfile> findByIamUserId(UUID iamUserId);

    boolean existsByIamUserId(UUID iamUserId);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByEmployeeCodeAndIamUserIdNot(String employeeCode, UUID iamUserId);

    @Query("""
            select a from AgentProfile a
            where (:status is null or a.profileStatus = :status)
              and (:query is null or :query = ''
                   or lower(a.firstName) like lower(concat('%', :query, '%'))
                   or lower(a.lastName) like lower(concat('%', :query, '%'))
                   or lower(a.email) like lower(concat('%', :query, '%'))
                   or lower(a.employeeCode) like lower(concat('%', :query, '%'))
                   or lower(a.department) like lower(concat('%', :query, '%')))
            """)
    Page<AgentProfile> search(String query, ProfileStatus status, Pageable pageable);
}
