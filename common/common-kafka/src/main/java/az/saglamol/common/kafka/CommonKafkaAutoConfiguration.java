package az.saglamol.common.kafka;

import az.saglamol.common.kafka.consumer.ProcessedEventRepository;
import az.saglamol.common.kafka.consumer.ProcessedEventService;
import az.saglamol.common.kafka.outbox.EventEnvelopeFactory;
import az.saglamol.common.kafka.outbox.KafkaPublisher;
import az.saglamol.common.kafka.outbox.OutboxProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;

@AutoConfiguration
@EnableConfigurationProperties(OutboxProperties.class)
public class CommonKafkaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    EventEnvelopeFactory eventEnvelopeFactory(ObjectMapper objectMapper) {
        return new EventEnvelopeFactory(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    KafkaPublisher kafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        return new KafkaPublisher(kafkaTemplate);
    }

    @Bean
    @ConditionalOnBean(ProcessedEventRepository.class)
    @ConditionalOnMissingBean
    ProcessedEventService processedEventService(ProcessedEventRepository repository) {
        return new ProcessedEventService(repository);
    }
}
