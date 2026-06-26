package az.unibank.smartorder.notification.application.handler;

import az.unibank.smartorder.events.order.OrderCancelledEvent;
import az.unibank.smartorder.events.order.OrderCreatedEvent;
import az.unibank.smartorder.events.payment.PaymentFailedEvent;
import az.unibank.smartorder.events.payment.PaymentProcessedEvent;
import az.unibank.smartorder.notification.domain.model.aggregate.Notification;
import az.unibank.smartorder.notification.domain.model.valueobject.NotificationId;
import az.unibank.smartorder.notification.domain.model.valueobject.NotificationStatus;
import az.unibank.smartorder.notification.domain.model.valueobject.NotificationType;
import az.unibank.smartorder.notification.domain.port.outbound.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventHandlers {

    private final NotificationRepository notificationRepository;

    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        Notification notification = Notification.builder()
                .id(NotificationId.of(UUID.randomUUID()))
                .orderId(event.payload().orderId())
                .customerId(event.payload().customerId())
                .type(NotificationType.ORDER_CREATED)
                .status(NotificationStatus.SENT)
                .message("Order created successfully with total amount: " + event.payload().totalAmount())
                .occurredAt(event.occurredAt())
                .createdAt(Instant.now())
                .build();
        notificationRepository.save(notification);
    }

    public void handlePaymentProcessedEvent(PaymentProcessedEvent event) {
        UUID customerId = event.payload().customerId();
        Notification notification = Notification.builder()
                .id(NotificationId.of(UUID.randomUUID()))
                .orderId(event.payload().orderId())
                .customerId(customerId)
                .type(NotificationType.PAYMENT_SUCCESS)
                .status(NotificationStatus.SENT)
                .message("Payment processed successfully. Amount: " + event.payload().amount())
                .occurredAt(event.occurredAt())
                .createdAt(Instant.now())
                .build();
        notificationRepository.save(notification);
    }

    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        UUID customerId = event.payload().customerId();
        Notification notification = Notification.builder()
                .id(NotificationId.of(UUID.randomUUID()))
                .orderId(event.payload().orderId())
                .customerId(customerId)
                .type(NotificationType.PAYMENT_FAILED)
                .status(NotificationStatus.SENT)
                .message("Payment failed. Reason: " + event.payload().reason())
                .occurredAt(event.occurredAt())
                .createdAt(Instant.now())
                .build();
        notificationRepository.save(notification);
    }

    public void handleOrderCancelledEvent(OrderCancelledEvent event) {
        Notification notification = Notification.builder()
                .id(NotificationId.of(UUID.randomUUID()))
                .orderId(event.payload().orderId())
                .customerId(event.payload().customerId())
                .type(NotificationType.ORDER_CANCELLED)
                .status(NotificationStatus.SENT)
                .message("Order was cancelled.")
                .occurredAt(event.occurredAt())
                .createdAt(Instant.now())
                .build();
        notificationRepository.save(notification);
    }

}
