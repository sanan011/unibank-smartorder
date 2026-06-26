package az.unibank.smartorder.notification.application.handler;

import az.unibank.smartorder.notification.domain.model.aggregate.Notification;
import az.unibank.smartorder.notification.domain.port.inbound.QueryNotificationUseCase;
import az.unibank.smartorder.notification.domain.port.outbound.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationQueryHandlers implements QueryNotificationUseCase {

    private final NotificationRepository notificationRepository;

    @Override
    public Notification getNotificationById(UUID id) {
        return notificationRepository.findById(new az.unibank.smartorder.notification.domain.model.valueobject.NotificationId(id))
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
    }

    @Override
    public List<Notification> getNotificationsByCustomerId(UUID customerId, int page, int size) {
        return notificationRepository.findByCustomerId(customerId, page, size);
    }
}
