package az.saglamol.healthrecord.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "medical_documents")
public class MedicalDocument {
    @Id
    private UUID id;

    @Column(name = "health_record_id")
    private UUID healthRecordId;

    @Column(name = "treatment_id")
    private UUID treatmentId;

    @Column(name = "claim_id")
    private UUID claimId;

    @Column(name = "patient_profile_id", nullable = false)
    private UUID patientProfileId;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(name = "uploaded_by_user_id", nullable = false)
    private UUID uploadedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 40)
    private DocumentType documentType;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    @Column(name = "storage_bucket", nullable = false, length = 120)
    private String storageBucket;

    @Column(name = "minio_key", nullable = false, length = 500)
    private String minioKey;

    @Column(name = "sha256_hash", length = 64, columnDefinition = "char(64)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String sha256Hash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private MedicalDocumentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected MedicalDocument() {
    }

    public MedicalDocument(UUID id, UUID healthRecordId, UUID treatmentId, UUID claimId, UUID patientProfileId,
                           UUID hospitalId, UUID uploadedByUserId, DocumentType documentType, String fileName,
                           Long fileSize, String contentType, String storageBucket, String minioKey,
                           String sha256Hash, MedicalDocumentStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.healthRecordId = healthRecordId;
        this.treatmentId = treatmentId;
        this.claimId = claimId;
        this.patientProfileId = patientProfileId;
        this.hospitalId = hospitalId;
        this.uploadedByUserId = uploadedByUserId;
        this.documentType = documentType;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.storageBucket = storageBucket;
        this.minioKey = minioKey;
        this.sha256Hash = sha256Hash;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void confirm(String sha256Hash, Instant updatedAt) {
        this.sha256Hash = sha256Hash;
        this.status = MedicalDocumentStatus.CONFIRMED;
        this.updatedAt = updatedAt;
    }

    public void markUploadFailed(Instant updatedAt) {
        this.status = MedicalDocumentStatus.UPLOAD_FAILED;
        this.updatedAt = updatedAt;
    }

    public void softDelete(Instant updatedAt) {
        this.status = MedicalDocumentStatus.DELETED;
        this.updatedAt = updatedAt;
    }

    public UUID getHealthRecordId() {
        return healthRecordId;
    }

    public UUID getTreatmentId() {
        return treatmentId;
    }

    public UUID getClaimId() {
        return claimId;
    }

    public UUID getPatientProfileId() {
        return patientProfileId;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public UUID getUploadedByUserId() {
        return uploadedByUserId;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public String getStorageBucket() {
        return storageBucket;
    }

    public String getMinioKey() {
        return minioKey;
    }

    public String getSha256Hash() {
        return sha256Hash;
    }

    public MedicalDocumentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
