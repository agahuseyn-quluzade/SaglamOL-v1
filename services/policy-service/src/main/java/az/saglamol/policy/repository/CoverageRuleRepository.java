package az.saglamol.policy.repository;

import az.saglamol.policy.entity.CoverageRule;
import az.saglamol.policy.entity.RuleStatus;
import az.saglamol.policy.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CoverageRuleRepository extends JpaRepository<CoverageRule, UUID> {
    List<CoverageRule> findByProductId(UUID productId);

    Optional<CoverageRule> findByProductIdAndServiceType(UUID productId, ServiceType serviceType);

    Optional<CoverageRule> findByProductIdAndServiceTypeAndStatus(UUID productId, ServiceType serviceType, RuleStatus status);
}
