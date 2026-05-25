package az.saglamol.common.kafka.consumer;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessedEventServiceTest {

    @Test
    void detectsAlreadyProcessedEvent() {
        ProcessedEventRepository repository = mock(ProcessedEventRepository.class);
        UUID eventId = UUID.randomUUID();
        when(repository.existsByEventIdAndConsumerName(eventId, "claim-consumer")).thenReturn(true);
        ProcessedEventService service = new ProcessedEventService(repository);

        assertTrue(service.isAlreadyProcessed(eventId, "claim-consumer"));
        assertFalse(service.isAlreadyProcessed(UUID.randomUUID(), "claim-consumer"));
    }

    @Test
    void marksProcessedEvent() {
        ProcessedEventRepository repository = mock(ProcessedEventRepository.class);
        ProcessedEventService service = new ProcessedEventService(repository);
        UUID eventId = UUID.randomUUID();

        service.markProcessed(eventId, "ClaimSubmittedEvent", "claim-consumer");

        ArgumentCaptor<ProcessedEvent> captor = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(repository).save(captor.capture());
        assertEquals(eventId, captor.getValue().getEventId());
        assertEquals("ClaimSubmittedEvent", captor.getValue().getEventType());
        assertEquals("claim-consumer", captor.getValue().getConsumerName());
    }
}
