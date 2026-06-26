package az.unibank.smartorder.identity.domain.event;

import az.unibank.smartorder.events.DomainEvent;
import az.unibank.smartorder.identity.domain.model.valueobject.Email;
import az.unibank.smartorder.identity.domain.model.valueobject.Role;
import az.unibank.smartorder.identity.domain.model.valueobject.UserId;

import java.time.Instant;
import java.util.UUID;

public record UserRegisteredEvent(
        UUID eventId,
        UserId userId,
        Email email,
        Role role,
        Instant occurredAt,
        UUID correlationId
) implements DomainEvent {
    
    public UserRegisteredEvent(UserId userId, Email email, Role role) {
        this(UUID.randomUUID(), userId, email, role, Instant.now(), UUID.randomUUID());
    }

    @Override
    public String eventType() {
        return "UserRegisteredEvent";
    }

    @Override
    public String eventVersion() {
        return "1.0";
    }
}
