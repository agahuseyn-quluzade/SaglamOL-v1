package az.saglamol.claim.config;

import az.saglamol.claim.repository.ClaimProcessedEventRepository;
import az.saglamol.common.kafka.consumer.ProcessedEventService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClaimConsumerConfig {

    @Bean
    ProcessedEventService claimProcessedEventService(ClaimProcessedEventRepository repository) {
        return new ProcessedEventService(repository);
    }
}
