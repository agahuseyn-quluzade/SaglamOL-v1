package az.saglamol.claim.config;

import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Bean
    ProducerFactory<String, Object> claimProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> properties = kafkaProperties.buildProducerProperties();
        return new DefaultKafkaProducerFactory<>(properties, new StringSerializer(), new JsonSerializer<>());
    }

    @Bean
    KafkaTemplate<String, Object> claimKafkaTemplate(ProducerFactory<String, Object> claimProducerFactory) {
        return new KafkaTemplate<>(claimProducerFactory);
    }
}
