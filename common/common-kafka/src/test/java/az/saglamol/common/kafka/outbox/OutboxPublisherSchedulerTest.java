package az.saglamol.common.kafka.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxPublisherSchedulerTest {

    @Test
    void publishesPendingEventAndMarksPublished() {
        TestOutboxEvent event = outboxEvent(0, 5);
        @SuppressWarnings("unchecked")
        BaseOutboxEventRepository<TestOutboxEvent> repository = mock(BaseOutboxEventRepository.class);
        when(repository.findTop100ByStatusAndNextRetryAtLessThanEqualAndRetryCountLessThanOrderByCreatedAtAsc(
                eq(OutboxEventStatus.PENDING), any(Instant.class), eq(5)
        )).thenReturn(List.of(event));
        KafkaPublisher publisher = mock(KafkaPublisher.class);

        scheduler(repository, publisher, properties("claim.events", 5)).publishPendingEvents();

        verify(publisher).publish(eq("claim.events"), eq(event.getAggregateId().toString()), any());
        verify(repository).save(event);
        assertEquals(OutboxEventStatus.PUBLISHED, event.getStatus());
        assertNotNull(event.getPublishedAt());
    }

    @Test
    void failedPublishSchedulesRetry() {
        TestOutboxEvent event = outboxEvent(0, 5);
        @SuppressWarnings("unchecked")
        BaseOutboxEventRepository<TestOutboxEvent> repository = mock(BaseOutboxEventRepository.class);
        when(repository.findTop100ByStatusAndNextRetryAtLessThanEqualAndRetryCountLessThanOrderByCreatedAtAsc(
                eq(OutboxEventStatus.PENDING), any(Instant.class), eq(5)
        )).thenReturn(List.of(event));
        KafkaPublisher publisher = mock(KafkaPublisher.class);
        doThrow(new IllegalStateException("kafka down")).when(publisher).publish(any(), any(), any());

        scheduler(repository, publisher, properties("claim.events", 5)).publishPendingEvents();

        verify(repository).save(event);
        assertEquals(OutboxEventStatus.PENDING, event.getStatus());
        assertEquals(1, event.getRetryCount());
        assertEquals("kafka down", event.getErrorMessage());
        assertNotNull(event.getNextRetryAt());
    }

    @Test
    void failedPublishMarksFailedAtMaxRetry() {
        TestOutboxEvent event = outboxEvent(4, 5);
        @SuppressWarnings("unchecked")
        BaseOutboxEventRepository<TestOutboxEvent> repository = mock(BaseOutboxEventRepository.class);
        when(repository.findTop100ByStatusAndNextRetryAtLessThanEqualAndRetryCountLessThanOrderByCreatedAtAsc(
                eq(OutboxEventStatus.PENDING), any(Instant.class), eq(5)
        )).thenReturn(List.of(event));
        KafkaPublisher publisher = mock(KafkaPublisher.class);
        doThrow(new IllegalStateException("permanent failure")).when(publisher).publish(any(), any(), any());

        scheduler(repository, publisher, properties("claim.events", 5)).publishPendingEvents();

        verify(repository).save(event);
        assertEquals(OutboxEventStatus.FAILED, event.getStatus());
        assertEquals(5, event.getRetryCount());
        assertEquals("permanent failure", event.getErrorMessage());
    }

    private OutboxPublisherScheduler<TestOutboxEvent> scheduler(
            BaseOutboxEventRepository<TestOutboxEvent> repository,
            KafkaPublisher publisher,
            OutboxProperties properties
    ) {
        return new OutboxPublisherScheduler<>(
                repository,
                new EventEnvelopeFactory(new ObjectMapper()),
                publisher,
                properties,
                "claim-service"
        );
    }

    private OutboxProperties properties(String topic, int maxRetries) {
        OutboxProperties properties = new OutboxProperties();
        properties.setTopic(topic);
        properties.setMaxRetries(maxRetries);
        return properties;
    }

    private TestOutboxEvent outboxEvent(int retryCount, int maxRetries) {
        TestOutboxEvent event = new TestOutboxEvent();
        event.setId(UUID.randomUUID());
        event.setAggregateType("CLAIM");
        event.setAggregateId(UUID.randomUUID());
        event.setEventType("ClaimSubmittedEvent");
        event.setPayload("{\"claimNumber\":\"CLM-1\"}");
        event.setStatus(OutboxEventStatus.PENDING);
        event.setRetryCount(retryCount);
        event.setMaxRetries(maxRetries);
        event.setCreatedAt(Instant.parse("2026-05-25T08:00:00Z"));
        event.setNextRetryAt(Instant.parse("2026-05-25T08:00:00Z"));
        return event;
    }
}
