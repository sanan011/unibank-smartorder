package az.unibank.smartorder.order.infrastructure.messaging.adapter;

import az.unibank.smartorder.order.domain.port.outbound.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Architectural justification")
public class RabbitMQEventPublisherAdapter implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(String exchange, String routingKey, String payload) {
        Message message = MessageBuilder.withBody(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();
        
        rabbitTemplate.send(exchange, routingKey, message);
        log.debug("Published event to exchange {} with routingKey {}", exchange, routingKey);
    }
}


