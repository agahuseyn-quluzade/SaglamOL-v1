package az.saglamol.healthrecord.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_hash_index")
public class DocumentHashIndex {
    @Id
    private UUID id;

    @Column(name = "sha256_hash", length = 64, columnDefinition = "char(64)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String sha256Hash;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "patient_profile_id", nullable = false)
    private UUID patientProfileId;

    @Column(name = "claim_id")
    private UUID claimId;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected DocumentHashIndex() {
    }

    public DocumentHashIndex(UUID id, String sha256Hash, UUID documentId, UUID patientProfileId,
                             UUID claimId, UUID hospitalId, Instant createdAt) {
        this.id = id;
        this.sha256Hash = sha256Hash;
        this.documentId = documentId;
        this.patientProfileId = patientProfileId;
        this.claimId = claimId;
        this.hospitalId = hospitalId;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getSha256Hash() {
        return sha256Hash;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public UUID getPatientProfileId() {
        return patientProfileId;
    }

    public UUID getClaimId() {
        return claimId;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
