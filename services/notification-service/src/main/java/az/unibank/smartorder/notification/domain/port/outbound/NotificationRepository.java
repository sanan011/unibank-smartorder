package az.unibank.smartorder.notification.domain.port.outbound;

import az.unibank.smartorder.notification.domain.model.aggregate.Notification;
import az.unibank.smartorder.notification.domain.model.valueobject.NotificationId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findById(NotificationId id);
    Optional<Notification> findFirstByOrderId(UUID orderId);
    List<Notification> findByCustomerId(UUID customerId, int page, int size);
}
