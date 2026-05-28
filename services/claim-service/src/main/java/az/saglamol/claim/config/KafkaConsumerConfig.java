package az.saglamol.claim.config;

import az.saglamol.common.events.EventEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    ConsumerFactory<String, EventEnvelope> claimConsumerFactory(
            KafkaProperties kafkaProperties,
            ObjectProvider<SslBundles> sslBundles,
            ObjectMapper objectMapper
    ) {
        Map<String, Object> properties = new HashMap<>(
                kafkaProperties.buildConsumerProperties(sslBundles.getIfAvailable())
        );
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumer().getGroupId());
        properties.remove(JsonDeserializer.TRUSTED_PACKAGES);
        properties.remove(JsonDeserializer.VALUE_DEFAULT_TYPE);
        properties.remove(JsonDeserializer.USE_TYPE_INFO_HEADERS);
        JsonDeserializer<EventEnvelope> valueDeserializer = new JsonDeserializer<>(EventEnvelope.class, objectMapper, false);
        valueDeserializer.addTrustedPackages("az.saglamol.common.events", "az.saglamol.common.events.*");
        valueDeserializer.setUseTypeHeaders(false);
        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, EventEnvelope> kafkaListenerContainerFactory(
            ConsumerFactory<String, EventEnvelope> claimConsumerFactory,
            CommonErrorHandler claimKafkaErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, EventEnvelope> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(claimConsumerFactory);
        factory.setCommonErrorHandler(claimKafkaErrorHandler);
        return factory;
    }

    @Bean
    CommonErrorHandler claimKafkaErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(record.topic() + ".dlt", record.partition())
        );
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3L));
    }
}
