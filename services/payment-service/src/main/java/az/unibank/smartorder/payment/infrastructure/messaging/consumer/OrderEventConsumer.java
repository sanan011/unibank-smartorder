package az.unibank.smartorder.payment.infrastructure.messaging.consumer;

import az.unibank.smartorder.payment.infrastructure.gateway.MockPaymentGatewayAdapter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class OrderEventConsumer {

    private final MockPaymentGatewayAdapter paymentGatewayAdapter;

    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed bean")
    public OrderEventConsumer(MockPaymentGatewayAdapter paymentGatewayAdapter) {
        this.paymentGatewayAdapter = paymentGatewayAdapter;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "payment.order.created.queue", durable = "true"),
            exchange = @Exchange(value = "smartorder.events", type = "topic", durable = "true"),
            key = "order.created"
    ))
    public void onOrderCreated(@Payload Map<String, Object> event) {
        log.info("Received OrderCreatedEvent: {}", event);

        try {
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");
            if (payload == null) {
                log.warn("Event payload is null, skipping");
                return;
            }

            String orderIdStr = (String) payload.get("orderId");
            Number amountNum = (Number) payload.get("totalAmount");
            
            if (orderIdStr == null || amountNum == null) {
                log.warn("Missing orderId or totalAmount in payload: {}", payload);
                return;
            }

            UUID orderId = UUID.fromString(orderIdStr);
            BigDecimal amount = new BigDecimal(amountNum.toString());

            boolean success = paymentGatewayAdapter.processPayment(orderId, amount);
            
            if (success) {
                log.info("Successfully processed payment for order: {}", orderId);
                // In a complete implementation, we would publish a PaymentProcessedEvent here
            } else {
                log.warn("Failed to process payment for order: {} after retries", orderId);
                // In a complete implementation, we would publish a PaymentFailedEvent here
            }

        } catch (Exception e) {
            log.error("Error processing OrderCreatedEvent", e);
            throw e; // Rethrow to requeue or DLQ depending on config
        }
    }
}
