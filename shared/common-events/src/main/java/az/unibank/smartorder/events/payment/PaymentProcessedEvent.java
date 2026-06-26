package az.unibank.smartorder.events.payment;

import az.unibank.smartorder.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentProcessedEvent(
    UUID eventId,
    String eventType,
    String eventVersion,
    Instant occurredAt,
    UUID correlationId,
    Payload payload
) implements DomainEvent {

    public PaymentProcessedEvent(UUID correlationId, Payload payload) {
        this(UUID.randomUUID(), "PaymentProcessedEvent", "1.0", Instant.now(), correlationId, payload);
    }

    public record Payload(
        UUID orderId,
        UUID customerId,
        UUID paymentId,
        String status,
        BigDecimal amount,
        String currency,
        String gatewayReference
    ) {}
}
