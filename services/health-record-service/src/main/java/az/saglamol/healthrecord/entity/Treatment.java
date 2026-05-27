package az.saglamol.healthrecord.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "treatments")
public class Treatment {
    @Id
    private UUID id;

    @Column(name = "health_record_id", nullable = false)
    private UUID healthRecordId;

    @Column(name = "service_type", nullable = false, length = 80)
    private String serviceType;

    @Column(name = "treatment_type", nullable = false, length = 80)
    private String treatmentType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT")
    private String medications;

    @Column(name = "estimated_cost", precision = 14, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "actual_cost", precision = 14, scale = 2)
    private BigDecimal actualCost;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Treatment() {
    }

    public Treatment(UUID id, UUID healthRecordId, String serviceType, String treatmentType, String description,
                     LocalDate startDate, LocalDate endDate, String medications, BigDecimal estimatedCost,
                     BigDecimal actualCost, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.healthRecordId = healthRecordId;
        this.serviceType = serviceType;
        this.treatmentType = treatmentType;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.medications = medications;
        this.estimatedCost = estimatedCost;
        this.actualCost = actualCost;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getHealthRecordId() {
        return healthRecordId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getTreatmentType() {
        return treatmentType;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getMedications() {
        return medications;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public BigDecimal getActualCost() {
        return actualCost;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
