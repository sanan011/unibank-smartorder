package az.unibank.smartorder.notification.infrastructure.messaging.consumer;

import az.unibank.smartorder.events.order.OrderCancelledEvent;
import az.unibank.smartorder.events.order.OrderCreatedEvent;
import az.unibank.smartorder.events.payment.PaymentFailedEvent;
import az.unibank.smartorder.events.payment.PaymentProcessedEvent;
import az.unibank.smartorder.notification.application.handler.NotificationEventHandlers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Component
@RequiredArgsConstructor
@Slf4j
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring singleton injection")
public class NotificationEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationEventHandlers eventHandlers;

    @RabbitListener(queues = "notification.queue")
    public void consumeMessage(Message message) {
        try {
            JsonNode rootNode = objectMapper.readTree(message.getBody());
            String eventType = rootNode.path("eventType").asText();

            log.info("Received event for notification: {}", eventType);

            switch (eventType) {
                case "OrderCreatedEvent":
                    OrderCreatedEvent orderCreatedEvent = objectMapper.readValue(message.getBody(), OrderCreatedEvent.class);
                    eventHandlers.handleOrderCreatedEvent(orderCreatedEvent);
                    break;
                case "PaymentProcessedEvent":
                    PaymentProcessedEvent paymentProcessedEvent = objectMapper.readValue(message.getBody(), PaymentProcessedEvent.class);
                    eventHandlers.handlePaymentProcessedEvent(paymentProcessedEvent);
                    break;
                case "PaymentFailedEvent":
                    PaymentFailedEvent paymentFailedEvent = objectMapper.readValue(message.getBody(), PaymentFailedEvent.class);
                    eventHandlers.handlePaymentFailedEvent(paymentFailedEvent);
                    break;
                case "OrderCancelledEvent":
                    OrderCancelledEvent orderCancelledEvent = objectMapper.readValue(message.getBody(), OrderCancelledEvent.class);
                    eventHandlers.handleOrderCancelledEvent(orderCancelledEvent);
                    break;
                default:
                    log.debug("Ignored event type in notification-service: {}", eventType);
                    break;
            }
        } catch (java.io.IOException | IllegalArgumentException e) {
            log.error("Failed to process notification message, rejecting to DLQ", e);
            throw new AmqpRejectAndDontRequeueException("Message processing failed", e);
        }
    }
}
