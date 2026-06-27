package az.unibank.smartorder.payment.infrastructure.messaging.consumer;

import az.unibank.smartorder.events.order.OrderCreatedEvent;
import az.unibank.smartorder.payment.domain.port.inbound.ProcessOrderPaymentUseCase;
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
public class OrderEventConsumer {

    private final ProcessOrderPaymentUseCase processOrderPaymentUseCase;
    private final ObjectMapper objectMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "payment.order.created.queue", durable = "true"),
            exchange = @Exchange(value = "smartorder.events", type = "topic", durable = "true"),
            key = "order.created"
    ))
    public void onOrderCreated(@Payload Map<String, Object> eventMap) {
        log.info("Received OrderCreatedEvent: {}", eventMap);

        try {
            OrderCreatedEvent event = objectMapper.convertValue(eventMap, OrderCreatedEvent.class);
            
            if (event.eventId() == null || event.payload() == null || event.payload().orderId() == null) {
                log.warn("Invalid event payload, skipping: {}", eventMap);
                return;
            }

            processOrderPaymentUseCase.processPayment(event);
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse event map into OrderCreatedEvent: {}", eventMap, e);
            throw new org.springframework.amqp.AmqpRejectAndDontRequeueException("Unparseable event payload", e);
        } catch (Exception e) {
            log.error("Error processing event", e);
            throw new org.springframework.amqp.AmqpRejectAndDontRequeueException("Error processing event", e);
        }
    }
}
