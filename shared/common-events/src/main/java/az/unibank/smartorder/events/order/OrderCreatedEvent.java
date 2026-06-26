package az.unibank.smartorder.events.order;

import az.unibank.smartorder.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID eventId,
    String eventType,
    String eventVersion,
    Instant occurredAt,
    UUID correlationId,
    Payload payload
) implements DomainEvent {

    public OrderCreatedEvent(UUID correlationId, Payload payload) {
        this(UUID.randomUUID(), "OrderCreatedEvent", "1.0", Instant.now(), correlationId, payload);
    }

    public record Payload(
        UUID orderId,
        UUID customerId,
        List<OrderItemPayload> items,
        BigDecimal totalAmount,
        String currency
    ) {
        public Payload {
            items = items == null ? List.of() : List.copyOf(items);
        }

        @Override
        public List<OrderItemPayload> items() {
            return List.copyOf(items);
        }
    }

    public record OrderItemPayload(
        UUID productId,
        String productName,
        BigDecimal unitPrice,
        String currency,
        int quantity
    ) {}
}
