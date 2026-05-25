package az.saglamol.userprofile.repository;

import az.saglamol.userprofile.entity.Hospital;
import az.saglamol.userprofile.entity.HospitalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface HospitalRepository extends JpaRepository<Hospital, UUID> {

    boolean existsByTaxId(String taxId);

    boolean existsByLicenseNo(String licenseNo);

    boolean existsByIdAndStatus(UUID id, HospitalStatus status);

    @Query("""
            select h from Hospital h
            where (:status is null or h.status = :status)
              and (:name is null or :name = '' or lower(h.name) like lower(concat('%', :name, '%')))
              and (:email is null or :email = '' or lower(h.email) like lower(concat('%', :email, '%')))
              and (:city is null or :city = '' or exists (
                  select 1 from HospitalBranch b
                  where b.hospitalId = h.id and lower(b.city) like lower(concat('%', :city, '%'))
              ))
            """)
    Page<Hospital> search(HospitalStatus status, String name, String email, String city, Pageable pageable);
}
