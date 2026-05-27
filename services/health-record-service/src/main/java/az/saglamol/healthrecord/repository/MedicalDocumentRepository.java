package az.saglamol.healthrecord.repository;

import az.saglamol.healthrecord.entity.DocumentType;
import az.saglamol.healthrecord.entity.MedicalDocument;
import az.saglamol.healthrecord.entity.MedicalDocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, UUID> {
    List<MedicalDocument> findByHealthRecordId(UUID healthRecordId);

    List<MedicalDocument> findByTreatmentId(UUID treatmentId);

    List<MedicalDocument> findByClaimId(UUID claimId);

    Page<MedicalDocument> findByClaimId(UUID claimId, Pageable pageable);

    List<MedicalDocument> findByPatientProfileId(UUID patientProfileId);

    Page<MedicalDocument> findByPatientProfileId(UUID patientProfileId, Pageable pageable);

    List<MedicalDocument> findByHospitalId(UUID hospitalId);

    Page<MedicalDocument> findByHospitalId(UUID hospitalId, Pageable pageable);

    List<MedicalDocument> findByPatientProfileIdAndDocumentType(UUID patientProfileId, DocumentType documentType);

    List<MedicalDocument> findByPatientProfileIdAndStatus(UUID patientProfileId, MedicalDocumentStatus status);

    Optional<MedicalDocument> findBySha256Hash(String sha256Hash);
}
