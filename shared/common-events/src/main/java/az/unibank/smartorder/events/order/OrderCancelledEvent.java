package az.unibank.smartorder.events.order;

import az.unibank.smartorder.events.DomainEvent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCancelledEvent(
    UUID eventId,
    String eventType,
    String eventVersion,
    Instant occurredAt,
    UUID correlationId,
    Payload payload
) implements DomainEvent {

    public OrderCancelledEvent(UUID correlationId, Payload payload) {
        this(UUID.randomUUID(), "OrderCancelledEvent", "1.0", Instant.now(), correlationId, payload);
    }

    public record Payload(
        UUID orderId,
        UUID customerId,
        List<OrderItemPayload> items
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
        int quantity
    ) {}
}
