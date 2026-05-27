package az.saglamol.payment.config;

import az.saglamol.common.kafka.consumer.ProcessedEventService;
import az.saglamol.common.kafka.outbox.EventEnvelopeFactory;
import az.saglamol.common.kafka.outbox.KafkaPublisher;
import az.saglamol.common.kafka.outbox.OutboxEventService;
import az.saglamol.common.kafka.outbox.OutboxProperties;
import az.saglamol.common.kafka.outbox.OutboxPublisherScheduler;
import az.saglamol.payment.entity.OutboxEvent;
import az.saglamol.payment.repository.OutboxEventRepository;
import az.saglamol.payment.repository.PaymentProcessedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class PaymentOutboxConfig {

    @Bean
    OutboxEventService<OutboxEvent> outboxEventService(
            OutboxEventRepository repository,
            ObjectMapper objectMapper,
            OutboxProperties properties
    ) {
        return new OutboxEventService<>(repository, objectMapper, OutboxEvent::new, properties);
    }

    @Bean
    ProcessedEventService paymentProcessedEventService(PaymentProcessedEventRepository repository) {
        return new ProcessedEventService(repository);
    }

    @Bean
    @ConditionalOnBean(KafkaPublisher.class)
    OutboxPublisherScheduler<OutboxEvent> outboxPublisherScheduler(
            OutboxEventRepository repository,
            EventEnvelopeFactory envelopeFactory,
            KafkaPublisher kafkaPublisher,
            OutboxProperties properties
    ) {
        return new OutboxPublisherScheduler<>(repository, envelopeFactory, kafkaPublisher, properties, "payment-service");
    }
}
