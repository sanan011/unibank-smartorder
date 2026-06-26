package az.unibank.smartorder.notification.domain.port.inbound;

import az.unibank.smartorder.notification.domain.model.aggregate.Notification;

import java.util.List;
import java.util.UUID;

public interface QueryNotificationUseCase {
    Notification getNotificationById(UUID id);
    List<Notification> getNotificationsByCustomerId(UUID customerId, int page, int size);
}
