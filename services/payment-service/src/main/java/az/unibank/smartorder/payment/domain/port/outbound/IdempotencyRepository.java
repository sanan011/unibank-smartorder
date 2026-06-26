package az.unibank.smartorder.payment.domain.port.outbound;

public interface IdempotencyRepository {
    boolean exists(String eventId);
    void save(String eventId, String eventType);
}
