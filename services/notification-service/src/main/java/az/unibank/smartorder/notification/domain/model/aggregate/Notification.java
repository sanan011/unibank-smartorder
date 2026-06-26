package az.unibank.smartorder.notification.domain.model.aggregate;

import az.unibank.smartorder.notification.domain.model.valueobject.NotificationId;
import az.unibank.smartorder.notification.domain.model.valueobject.NotificationStatus;
import az.unibank.smartorder.notification.domain.model.valueobject.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Notification {
    private final NotificationId id;
    private final UUID orderId;
    private final UUID customerId;
    private final NotificationType type;
    private NotificationStatus status;
    private final String message;
    private final Instant occurredAt;
    private final Instant createdAt;

    public void markAsSent() {
        this.status = NotificationStatus.SENT;
    }

    public void markAsFailed() {
        this.status = NotificationStatus.FAILED;
    }
}
