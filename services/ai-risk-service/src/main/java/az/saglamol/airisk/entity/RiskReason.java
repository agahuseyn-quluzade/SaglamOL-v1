package az.saglamol.airisk.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "risk_reason")
public class RiskReason {
    @Id
    private UUID id;
    private UUID analysisId;
    private String reasonCode;
    private String description;

    protected RiskReason() {
    }
}
