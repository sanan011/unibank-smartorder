package az.unibank.smartorder.payment.domain.port.outbound;

import az.unibank.smartorder.events.DomainEvent;

public interface EventPublisherPort {
    void publish(String routingKey, DomainEvent event);
}
