package az.saglamol.fraud.entity;

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

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(name = "patient_profile_id", nullable = false)
    private UUID patientProfileId;

    @Column(name = "insurance_company_id", nullable = false)
    private UUID insuranceCompanyId;

    @Column(name = "hospital_id")
    private UUID hospitalId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected DocumentHashIndex() {
    }

    public DocumentHashIndex(UUID id, String sha256Hash, UUID claimId, UUID patientProfileId,
                             UUID insuranceCompanyId, UUID hospitalId, Instant createdAt) {
        this.id = id;
        this.sha256Hash = sha256Hash;
        this.claimId = claimId;
        this.patientProfileId = patientProfileId;
        this.insuranceCompanyId = insuranceCompanyId;
        this.hospitalId = hospitalId;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getSha256Hash() {
        return sha256Hash;
    }

    public UUID getClaimId() {
        return claimId;
    }

    public UUID getPatientProfileId() {
        return patientProfileId;
    }

    public UUID getInsuranceCompanyId() {
        return insuranceCompanyId;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
