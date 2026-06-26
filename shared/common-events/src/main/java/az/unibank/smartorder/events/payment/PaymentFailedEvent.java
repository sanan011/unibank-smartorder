package az.unibank.smartorder.events.payment;

import az.unibank.smartorder.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record PaymentFailedEvent(
    UUID eventId,
    String eventType,
    String eventVersion,
    Instant occurredAt,
    UUID correlationId,
    Payload payload
) implements DomainEvent {

    public PaymentFailedEvent(UUID correlationId, Payload payload) {
        this(UUID.randomUUID(), "PaymentFailedEvent", "1.0", Instant.now(), correlationId, payload);
    }

    public record Payload(
        UUID orderId,
        UUID paymentId,
        String reason,
        int attemptCount
    ) {}
}
