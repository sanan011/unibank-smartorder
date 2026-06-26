package az.unibank.smartorder.notification.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
public final class NotificationId {
    private final UUID value;

    public NotificationId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Notification ID cannot be null");
        }
        this.value = value;
    }

    public static NotificationId of(UUID value) {
        return new NotificationId(value);
    }
}
