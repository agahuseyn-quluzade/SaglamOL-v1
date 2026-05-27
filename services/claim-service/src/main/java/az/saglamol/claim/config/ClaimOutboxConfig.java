package az.saglamol.claim.config;

import az.saglamol.claim.entity.OutboxEvent;
import az.saglamol.claim.repository.OutboxEventRepository;
import az.saglamol.common.kafka.outbox.EventEnvelopeFactory;
import az.saglamol.common.kafka.outbox.KafkaPublisher;
import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.common.kafka.outbox.OutboxProperties;
import az.saglamol.common.kafka.outbox.OutboxPublisherScheduler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class ClaimOutboxConfig {

    @Bean
    OutboxEventService<OutboxEvent> claimOutboxEventService(
            OutboxEventRepository repository,
            ObjectMapper objectMapper,
            OutboxProperties properties
    ) {
        return new OutboxEventService<>(repository, objectMapper, OutboxEvent::new, properties);
    }

    @Bean
    @ConditionalOnBean(KafkaPublisher.class)
    OutboxPublisherScheduler<OutboxEvent> claimOutboxPublisherScheduler(
            OutboxEventRepository repository,
            EventEnvelopeFactory envelopeFactory,
            KafkaPublisher kafkaPublisher,
            OutboxProperties properties
    ) {
        return new OutboxPublisherScheduler<>(repository, envelopeFactory, kafkaPublisher, properties, "claim-service");
    }
}
