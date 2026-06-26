package az.unibank.smartorder.order.domain.port.outbound;

public interface EventPublisher {
    void publish(String exchange, String routingKey, String payload);
}
