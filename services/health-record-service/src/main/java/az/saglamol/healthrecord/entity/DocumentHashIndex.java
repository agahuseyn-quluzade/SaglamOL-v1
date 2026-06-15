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
    private UUID documentId;
    @Column(name = "sha256_hash", length = 64, columnDefinition = "char(64)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String sha256Hash;
    private Long fileSizeBytes;
    private String mimeType;
    private String invoiceNumber;
    private UUID clinicId;
    private UUID patientId;
    private Instant firstSeenAt;

    protected DocumentHashIndex() {
    }
}
