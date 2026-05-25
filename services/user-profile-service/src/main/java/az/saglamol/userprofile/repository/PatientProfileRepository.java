package az.saglamol.userprofile.repository;

import az.saglamol.userprofile.entity.PatientProfile;
import az.saglamol.userprofile.entity.ProfileStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PatientProfileRepository extends JpaRepository<PatientProfile, UUID> {

    Optional<PatientProfile> findByIamUserId(UUID iamUserId);

    boolean existsByIamUserId(UUID iamUserId);

    boolean existsById(UUID id);

    @Query("""
            select p from PatientProfile p
            where (:status is null or p.profileStatus = :status)
              and (:name is null or :name = ''
                   or lower(p.firstName) like lower(concat('%', :name, '%'))
                   or lower(p.lastName) like lower(concat('%', :name, '%')))
              and (:email is null or :email = '' or lower(p.email) like lower(concat('%', :email, '%')))
              and (:query is null or :query = ''
                   or lower(p.firstName) like lower(concat('%', :query, '%'))
                   or lower(p.lastName) like lower(concat('%', :query, '%'))
                   or lower(p.email) like lower(concat('%', :query, '%'))
                   or lower(p.phone) like lower(concat('%', :query, '%'))
                   or lower(p.nationalId) like lower(concat('%', :query, '%')))
            """)
    Page<PatientProfile> search(String query, ProfileStatus status, String name, String email, Pageable pageable);
}
