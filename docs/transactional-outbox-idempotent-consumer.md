# Transactional Outbox and Idempotent Consumer

SaglamOL microservice-lərində event publish və event consume mexanizmi
`common/common-kafka` modulu ilə standartlaşdırılır. Məqsəd sonrakı biznes
servislərinin Kafka inteqrasiyasını eyni contract üzərində qurmaqdır.

## Modul

Gradle module:

```text
common:common-kafka
```

Əsas dependency-lər:

- `common:common-events`
- `spring-kafka`
- `jackson-databind`
- `spring-boot-starter-data-jpa` compile-only

`common-kafka` JPA domain model yaratmır. Hər service öz database-i və öz
`outbox_events` cədvəli ilə işləyir.

Auto-configuration bu bean-ləri təmin edir:

- `OutboxProperties`
- `EventEnvelopeFactory`
- `KafkaPublisher`
- `ProcessedEventService`, əgər service-də `ProcessedEventRepository` bean-i varsa

## Outbox Model

`BaseOutboxEvent` `@MappedSuperclass`-dur. Hər event-producing service öz
modulunda konkret entity yaratmalıdır:

```java
@Entity
@Table(name = "outbox_events")
public class OutboxEvent extends BaseOutboxEvent {
}
```

Repository:

```java
public interface OutboxEventRepository
        extends BaseOutboxEventRepository<OutboxEvent> {
}
```

Bean nümunəsi:

```java
@Configuration
@EnableScheduling
public class OutboxConfig {

    @Bean
    OutboxEventService<OutboxEvent> outboxEventService(
            OutboxEventRepository repository,
            ObjectMapper objectMapper,
            OutboxProperties properties
    ) {
        return new OutboxEventService<>(repository, objectMapper, OutboxEvent::new, properties);
    }

    @Bean
    OutboxPublisherScheduler<OutboxEvent> outboxPublisherScheduler(
            OutboxEventRepository repository,
            EventEnvelopeFactory envelopeFactory,
            KafkaPublisher kafkaPublisher,
            OutboxProperties properties,
            @Value("${spring.application.name}") String serviceName
    ) {
        return new OutboxPublisherScheduler<>(
                repository,
                envelopeFactory,
                kafkaPublisher,
                properties,
                serviceName
        );
    }
}
```

Service logic transaction daxilində domain change etdikdən sonra outbox-a
event yazmalıdır:

```java
outboxEventService.saveEvent(
        "POLICY",
        policyId,
        "PolicyCreatedEvent",
        new PolicyCreatedEvent(...)
);
```

Scheduler `PENDING` event-ləri oxuyur, `EventEnvelope<JsonNode>` yaradır və
Kafka-ya publish edir. Uğurlu publish `PUBLISHED`, xəta isə retry metadata-sı
yazır. `retryCount >= maxRetries` olduqda status `FAILED` olur.

## Consumer Model

`ProcessedEvent` common entity-dir və hər consumer service-in öz database-ində
`processed_events` cədvəlindən istifadə edir.

Consumer service `ProcessedEventService` və `IdempotentEventConsumer` istifadə
etməlidir:

```java
public class ClaimEventConsumer extends IdempotentEventConsumer {

    public ClaimEventConsumer(ProcessedEventService processedEventService) {
        super(processedEventService, "claim-service-policy-consumer");
    }

    @Override
    protected void handleEvent(EventEnvelope<?> envelope) {
        // business logic
    }
}
```

`consume(envelope)` axını:

1. `eventId + consumerName` üzrə artıq işlənib-işlənmədiyini yoxlayır.
2. Duplicate event-dirsə skip edir.
3. Yeni event-dirsə `handleEvent` çağırır.
4. Uğurlu işlənəndən sonra `processed_events` cədvəlinə yazır.

## Liquibase

Hər service öz changelog-unda reusable template-i include etməlidir:

```xml
<include file="db/outbox-tables-template.xml" relativeToChangelogFile="false"/>
```

Template bu cədvəlləri yaradır:

- `outbox_events`
- `processed_events`

Index-lər:

- `outbox_events.status`
- `outbox_events.next_retry_at`
- `outbox_events.status + next_retry_at`
- `processed_events.event_id` unique

## Config

Minimum config:

```yaml
outbox:
  topic: claim.events
  maxRetries: 5
  scheduler:
    enabled: true
    interval: 5000

spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

Qeyd: prompt-da `kafka.bootstrap-servers` adı qeyd edilib, Spring Kafka runtime
üçün canonical property `spring.kafka.bootstrap-servers`-dir. Servis config-ləri
bu canonical property ilə yazılmalıdır.

## Multi-company Contract

Event payload-lar `common-events` modulundakı record-lardan gəlməlidir. Policy,
claim, payment, risk və fraud event-lərində `companyId` saxlanılır. Patient
tərəfi üçün `patientProfileId` istifadə olunur; `iamUserId` yalnız IAM və staff
linking contract-larında saxlanılır.

## Test

```powershell
.\gradlew.bat :common:common-kafka:test
```
