package az.saglamol.userprofile.entity;

import az.saglamol.common.kafka.outbox.BaseOutboxEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent extends BaseOutboxEvent {
}
