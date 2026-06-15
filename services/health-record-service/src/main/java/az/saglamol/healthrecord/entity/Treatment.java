package az.saglamol.healthrecord.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "treatment")
public class Treatment {
    @Id
    private UUID id;
    private UUID healthRecordId;
    private String serviceType;
    private String description;

    protected Treatment() {
    }
}
