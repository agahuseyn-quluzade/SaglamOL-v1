package az.saglamol.healthrecord.repository;

import az.saglamol.healthrecord.entity.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TreatmentRepository extends JpaRepository<Treatment, UUID> {
    List<Treatment> findByHealthRecordId(UUID healthRecordId);
}
