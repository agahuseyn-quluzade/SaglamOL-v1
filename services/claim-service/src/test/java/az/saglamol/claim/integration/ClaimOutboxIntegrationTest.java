package az.saglamol.claim.integration;

import az.saglamol.claim.client.PolicyInternalClient;
import az.saglamol.claim.client.ProfileInternalClient;
import az.saglamol.claim.client.dto.EligibilityCheckResponse;
import az.saglamol.claim.client.dto.PolicyDetailResponse;
import az.saglamol.claim.client.dto.PolicyLimitReservationResponse;
import az.saglamol.claim.client.dto.UserProfileSummaryResponse;
import az.saglamol.claim.entity.Claim;
import az.saglamol.claim.entity.ClaimItem;
import az.saglamol.claim.entity.ClaimStatus;
import az.saglamol.claim.entity.OutboxEvent;
import az.saglamol.claim.entity.PayoutRecipientType;
import az.saglamol.claim.repository.ClaimItemRepository;
import az.saglamol.claim.repository.ClaimRepository;
import az.saglamol.claim.repository.OutboxEventRepository;
import az.saglamol.claim.service.ClaimService;
import az.saglamol.common.kafka.outbox.OutboxEventStatus;
import az.saglamol.common.kafka.outbox.OutboxPublisherScheduler;
import az.saglamol.common.security.AuthContext;
import az.saglamol.common.security.RoleConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "outbox.scheduler.enabled=false",
        "outbox.topic=claim.events",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
@Testcontainers(disabledWithoutDocker = true)
class ClaimOutboxIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("claim_it")
            .withUsername("saglamol")
            .withPassword("saglamol");

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1")
    );

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    ClaimService claimService;
    @Autowired
    ClaimRepository claimRepository;
    @Autowired
    ClaimItemRepository itemRepository;
    @Autowired
    OutboxEventRepository outboxRepository;
    @Autowired
    OutboxPublisherScheduler<OutboxEvent> outboxPublisherScheduler;

    @MockBean
    PolicyInternalClient policyClient;
    @MockBean
    ProfileInternalClient profileClient;

    @Test
    void submitClaimCreatesOutboxEventAndWorkerPublishesIt() {
        UUID userId = UUID.randomUUID();
        UUID claimId = UUID.randomUUID();
        UUID policyId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        UUID patientProfileId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        Instant now = Instant.now();
        claimRepository.save(new Claim(claimId, "CLM-IT-1", policyId, companyId, patientProfileId,
                null, null, ClaimStatus.DRAFT, "CONSULTATION", LocalDate.now(), BigDecimal.ZERO,
                null, BigDecimal.ZERO, BigDecimal.ZERO, PayoutRecipientType.PATIENT, null,
                null, null, null, null, null, null, null, null, now, now));
        itemRepository.save(new ClaimItem(UUID.randomUUID(), claimId, "Consultation", "CONS-1",
                new BigDecimal("100.00"), 1, LocalDate.now(), null, now, now));
        when(profileClient.userSummary(userId)).thenReturn(new UserProfileSummaryResponse(
                userId, patientProfileId, null, null, null, null, null, null,
                true, false, false, false, false));
        when(policyClient.getPolicy(policyId)).thenReturn(new PolicyDetailResponse(
                policyId, "POL-IT-1", companyId, UUID.randomUUID(), patientProfileId, null,
                "ACTIVE", LocalDate.now().minusDays(1), LocalDate.now().plusYears(1),
                BigDecimal.ZERO, new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                now, now, 0L));
        when(policyClient.checkEligibility(any())).thenReturn(new EligibilityCheckResponse(
                true, policyId, companyId, patientProfileId, null, "CONSULTATION",
                new BigDecimal("100.00"), new BigDecimal("80.00"), new BigDecimal("1000.00"),
                80, true, List.of()));
        when(policyClient.reserveLimit(eq(policyId), any())).thenReturn(new PolicyLimitReservationResponse(
                reservationId, policyId, companyId, claimId, new BigDecimal("80.00"),
                "RESERVED", now, now));

        claimService.submitClaim(auth(userId), claimId);

        List<OutboxEvent> pending = outboxRepository.findAll();
        assertEquals(1, pending.size());
        assertEquals("ClaimSubmittedEvent", pending.getFirst().getEventType());
        assertEquals(OutboxEventStatus.PENDING, pending.getFirst().getStatus());

        outboxPublisherScheduler.publishPendingEvents();

        List<OutboxEvent> published = outboxRepository.findAll();
        assertFalse(published.isEmpty());
        assertEquals(OutboxEventStatus.PUBLISHED, published.getFirst().getStatus());
    }

    private AuthContext auth(UUID userId) {
        return new AuthContext(userId, List.of(RoleConstants.PATIENT), "corr", Map.of());
    }
}
