package az.saglamol.userprofile.config;

import az.saglamol.common.kafka.outbox.EventEnvelopeFactory;
import az.saglamol.common.kafka.outbox.KafkaPublisher;
import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.common.kafka.outbox.OutboxProperties;
import az.saglamol.common.kafka.outbox.OutboxPublisherScheduler;
import az.saglamol.userprofile.entity.OutboxEvent;
import az.saglamol.userprofile.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class UserProfileOutboxConfig {

    @Bean
    OutboxEventService<OutboxEvent> userProfileOutboxEventService(
            OutboxEventRepository repository,
            ObjectMapper objectMapper,
            OutboxProperties properties
    ) {
        return new OutboxEventService<>(repository, objectMapper, OutboxEvent::new, properties);
    }

    @Bean
    @ConditionalOnBean(KafkaPublisher.class)
    OutboxPublisherScheduler<OutboxEvent> userProfileOutboxPublisherScheduler(
            OutboxEventRepository repository,
            EventEnvelopeFactory envelopeFactory,
            KafkaPublisher kafkaPublisher,
            OutboxProperties properties
    ) {
        return new OutboxPublisherScheduler<>(repository, envelopeFactory, kafkaPublisher, properties, "user-profile-service");
    }
}
