package az.saglamol.common.kafka.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxEventServiceTest {

    @Test
    void savesSerializedOutboxEvent() {
        @SuppressWarnings("unchecked")
        BaseOutboxEventRepository<TestOutboxEvent> repository = mock(BaseOutboxEventRepository.class);
        when(repository.save(any(TestOutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        OutboxProperties properties = new OutboxProperties();
        properties.setMaxRetries(7);
        OutboxEventService<TestOutboxEvent> service = new OutboxEventService<>(
                repository,
                new ObjectMapper(),
                TestOutboxEvent::new,
                properties
        );

        UUID aggregateId = UUID.randomUUID();
        service.saveEvent("POLICY", aggregateId, "PolicyCreatedEvent", Map.of("policyNumber", "POL-1"));

        ArgumentCaptor<TestOutboxEvent> captor = ArgumentCaptor.forClass(TestOutboxEvent.class);
        verify(repository).save(captor.capture());
        TestOutboxEvent saved = captor.getValue();
        assertEquals("POLICY", saved.getAggregateType());
        assertEquals(aggregateId, saved.getAggregateId());
        assertEquals("PolicyCreatedEvent", saved.getEventType());
        assertEquals(OutboxEventStatus.PENDING, saved.getStatus());
        assertEquals(7, saved.getMaxRetries());
        assertNotNull(saved.getNextRetryAt());
    }
}
