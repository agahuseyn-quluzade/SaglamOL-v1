package az.saglamol.common.kafka.outbox;

public final class OutboxEventStatus {

    public static final String PENDING = "PENDING";
    public static final String PUBLISHED = "PUBLISHED";
    public static final String FAILED = "FAILED";

    private OutboxEventStatus() {
    }
}
