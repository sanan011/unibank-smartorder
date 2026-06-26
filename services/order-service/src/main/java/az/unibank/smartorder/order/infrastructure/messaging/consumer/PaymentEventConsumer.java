package az.unibank.smartorder.order.infrastructure.messaging.consumer;

import az.unibank.smartorder.events.payment.PaymentFailedEvent;
import az.unibank.smartorder.events.payment.PaymentProcessedEvent;
import az.unibank.smartorder.order.domain.port.inbound.ProcessPaymentEventUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed bean")
public class PaymentEventConsumer {

    private final ProcessPaymentEventUseCase processPaymentEventUseCase;
    private final ObjectMapper objectMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "order.payment.processed.queue", durable = "true"),
            exchange = @Exchange(value = "smartorder.events", type = "topic", durable = "true"),
            key = "payment.processed"
    ))
    public void onPaymentProcessed(@Payload Map<String, Object> eventMap) {
        log.info("Received PaymentProcessedEvent: {}", eventMap);

        try {
            PaymentProcessedEvent event = objectMapper.convertValue(eventMap, PaymentProcessedEvent.class);

            if (event.eventId() == null || event.payload() == null || event.payload().orderId() == null) {
                log.warn("Invalid event payload, skipping: {}", eventMap);
                return;
            }

            processPaymentEventUseCase.processPaymentProcessedEvent(event);
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse event map into PaymentProcessedEvent: {}", eventMap, e);
            throw e;
        } catch (Exception e) {
            log.error("Error processing PaymentProcessedEvent", e);
            throw e;
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "order.payment.failed.queue", durable = "true"),
            exchange = @Exchange(value = "smartorder.events", type = "topic", durable = "true"),
            key = "payment.failed"
    ))
    public void onPaymentFailed(@Payload Map<String, Object> eventMap) {
        log.info("Received PaymentFailedEvent: {}", eventMap);

        try {
            PaymentFailedEvent event = objectMapper.convertValue(eventMap, PaymentFailedEvent.class);

            if (event.eventId() == null || event.payload() == null || event.payload().orderId() == null) {
                log.warn("Invalid event payload, skipping: {}", eventMap);
                return;
            }

            processPaymentEventUseCase.processPaymentFailedEvent(event);
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse event map into PaymentFailedEvent: {}", eventMap, e);
            throw e;
        } catch (Exception e) {
            log.error("Error processing PaymentFailedEvent", e);
            throw e;
        }
    }
}
