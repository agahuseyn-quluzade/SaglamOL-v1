package az.saglamol.healthrecord.repository;

import az.saglamol.healthrecord.entity.HealthRecord;
import az.saglamol.healthrecord.entity.HealthRecordStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, UUID> {
    List<HealthRecord> findByPatientProfileId(UUID patientProfileId);

    Page<HealthRecord> findByPatientProfileId(UUID patientProfileId, Pageable pageable);

    List<HealthRecord> findByDoctorProfileId(UUID doctorProfileId);

    List<HealthRecord> findByHospitalId(UUID hospitalId);

    List<HealthRecord> findByClaimId(UUID claimId);

    @Query("""
            select h from HealthRecord h
            where (:patientProfileId is null or h.patientProfileId = :patientProfileId)
              and (:hospitalId is null or h.hospitalId = :hospitalId)
              and (:claimId is null or h.claimId = :claimId)
              and (:status is null or h.status = :status)
              and (:fromDate is null or h.visitDate >= :fromDate)
              and (:toDate is null or h.visitDate <= :toDate)
            """)
    Page<HealthRecord> search(UUID patientProfileId, UUID hospitalId, UUID claimId, HealthRecordStatus status,
                              LocalDate fromDate, LocalDate toDate, Pageable pageable);
}
