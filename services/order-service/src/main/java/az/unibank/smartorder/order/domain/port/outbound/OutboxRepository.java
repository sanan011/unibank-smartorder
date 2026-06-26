package az.unibank.smartorder.order.domain.port.outbound;

import az.unibank.smartorder.events.DomainEvent;

public interface OutboxRepository {
    void save(DomainEvent event);
}
