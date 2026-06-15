package az.saglamol.userprofile.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "address")
public class Address {
    @Id
    private UUID id;
    private UUID ownerId;
    private String ownerType;
    private String city;
    private String line1;
    private Instant createdAt;

    protected Address() {
    }
}
