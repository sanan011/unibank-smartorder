package az.unibank.smartorder.payment.infrastructure.messaging.publisher;

import az.unibank.smartorder.events.DomainEvent;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import az.unibank.smartorder.payment.domain.port.outbound.EventPublisherPort;

@Component
@RequiredArgsConstructor
@Slf4j
@SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "Spring-managed bean")
public class PaymentEventPublisher implements EventPublisherPort {

    private final RabbitTemplate rabbitTemplate;
    
    private static final String EXCHANGE = "smartorder.events";

    @Override
    public void publish(String routingKey, DomainEvent event) {
        log.info("Publishing event {} to exchange {} with routing key {}", event.eventType(), EXCHANGE, routingKey);
        rabbitTemplate.convertAndSend(EXCHANGE, routingKey, event);
    }
}
