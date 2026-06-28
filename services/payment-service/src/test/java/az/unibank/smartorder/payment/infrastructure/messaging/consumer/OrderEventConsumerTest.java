package az.unibank.smartorder.payment.infrastructure.messaging.consumer;

import az.unibank.smartorder.events.order.OrderCreatedEvent;
import az.unibank.smartorder.payment.domain.port.inbound.ProcessOrderPaymentUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock
    private ProcessOrderPaymentUseCase processOrderPaymentUseCase;

    private ObjectMapper objectMapper;

    private OrderEventConsumer orderEventConsumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        orderEventConsumer = new OrderEventConsumer(processOrderPaymentUseCase, objectMapper);
    }

    @Test
    void onOrderCreated_whenEventIsValid_delegatesToUseCase() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();

        Map<String, Object> event = new HashMap<>();
        event.put("eventId", eventId.toString());
        event.put("correlationId", correlationId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", orderId.toString());
        payload.put("totalAmount", 100.5);
        event.put("payload", payload);

        orderEventConsumer.onOrderCreated(event);

        verify(processOrderPaymentUseCase).processPayment(any(OrderCreatedEvent.class));
    }
}
