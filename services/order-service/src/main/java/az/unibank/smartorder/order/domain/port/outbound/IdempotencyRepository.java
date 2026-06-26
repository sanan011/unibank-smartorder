package az.unibank.smartorder.order.domain.port.outbound;

public interface IdempotencyRepository {
    boolean exists(String eventId);
    void save(String eventId, String eventType);
}
