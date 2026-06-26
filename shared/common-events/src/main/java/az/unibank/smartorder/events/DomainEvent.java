package az.unibank.smartorder.events;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID eventId();
    String eventType();
    String eventVersion();
    Instant occurredAt();
    UUID correlationId();
}
