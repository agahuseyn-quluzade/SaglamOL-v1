package az.saglamol.userprofile.repository;

import az.saglamol.userprofile.entity.DoctorProfile;
import az.saglamol.userprofile.entity.ProfileStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, UUID> {

    Optional<DoctorProfile> findByIamUserId(UUID iamUserId);

    boolean existsByIamUserId(UUID iamUserId);

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByLicenseNumberAndIamUserIdNot(String licenseNumber, UUID iamUserId);

    @Query("""
            select d from DoctorProfile d
            where (:status is null or d.profileStatus = :status)
              and (:name is null or :name = ''
                   or lower(d.firstName) like lower(concat('%', :name, '%'))
                   or lower(d.lastName) like lower(concat('%', :name, '%')))
              and (:email is null or :email = '' or lower(d.email) like lower(concat('%', :email, '%')))
              and (:specialty is null or :specialty = '' or lower(d.specialty) like lower(concat('%', :specialty, '%')))
              and (:hospitalId is null or exists (
                  select 1 from DoctorHospitalAssignment a
                  where a.doctorProfileId = d.id and a.hospitalId = :hospitalId
              ))
              and (:query is null or :query = ''
                   or lower(d.firstName) like lower(concat('%', :query, '%'))
                   or lower(d.lastName) like lower(concat('%', :query, '%'))
                   or lower(d.email) like lower(concat('%', :query, '%'))
                   or lower(d.specialty) like lower(concat('%', :query, '%'))
                   or lower(d.licenseNumber) like lower(concat('%', :query, '%')))
            """)
    Page<DoctorProfile> search(String query, ProfileStatus status, String name, String email,
                               String specialty, UUID hospitalId, Pageable pageable);
}
