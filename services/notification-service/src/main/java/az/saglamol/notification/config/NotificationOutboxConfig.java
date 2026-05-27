package az.saglamol.notification.config;

import az.saglamol.common.kafka.consumer.ProcessedEventService;
import az.saglamol.common.kafka.outbox.EventEnvelopeFactory;
import az.saglamol.common.kafka.outbox.KafkaPublisher;
import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.common.kafka.outbox.OutboxProperties;
import az.saglamol.common.kafka.outbox.OutboxPublisherScheduler;
import az.saglamol.notification.entity.OutboxEvent;
import az.saglamol.notification.repository.NotificationProcessedEventRepository;
import az.saglamol.notification.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class NotificationOutboxConfig {

    @Bean
    OutboxEventService<OutboxEvent> notificationOutboxEventService(
            OutboxEventRepository repository,
            ObjectMapper objectMapper,
            OutboxProperties properties
    ) {
        return new OutboxEventService<>(repository, objectMapper, OutboxEvent::new, properties);
    }

    @Bean
    ProcessedEventService notificationProcessedEventService(NotificationProcessedEventRepository repository) {
        return new ProcessedEventService(repository);
    }

    @Bean
    @ConditionalOnBean(KafkaPublisher.class)
    OutboxPublisherScheduler<OutboxEvent> notificationOutboxPublisherScheduler(
            OutboxEventRepository repository,
            EventEnvelopeFactory envelopeFactory,
            KafkaPublisher kafkaPublisher,
            OutboxProperties properties
    ) {
        return new OutboxPublisherScheduler<>(repository, envelopeFactory, kafkaPublisher, properties, "notification-service");
    }
}
