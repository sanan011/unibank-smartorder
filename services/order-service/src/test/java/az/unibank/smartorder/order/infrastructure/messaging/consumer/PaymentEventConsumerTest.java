package az.unibank.smartorder.order.infrastructure.messaging.consumer;

import az.unibank.smartorder.events.payment.PaymentFailedEvent;
import az.unibank.smartorder.events.payment.PaymentProcessedEvent;
import az.unibank.smartorder.order.domain.port.inbound.ProcessPaymentEventUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
class PaymentEventConsumerTest {

    @Mock
    private ProcessPaymentEventUseCase processPaymentEventUseCase;

    private ObjectMapper objectMapper;

    private PaymentEventConsumer paymentEventConsumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        paymentEventConsumer = new PaymentEventConsumer(processPaymentEventUseCase, objectMapper);
    }

    @Test
    void onPaymentProcessed_whenEventIsValid_delegatesToUseCase() {
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

        paymentEventConsumer.onPaymentProcessed(event);

        verify(processPaymentEventUseCase).processPaymentProcessedEvent(any(PaymentProcessedEvent.class));
    }

    @Test
    void onPaymentFailed_whenEventIsValid_delegatesToUseCase() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();

        Map<String, Object> event = new HashMap<>();
        event.put("eventId", eventId.toString());
        event.put("correlationId", correlationId.toString());

        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", orderId.toString());
        payload.put("reason", "failed");
        event.put("payload", payload);

        paymentEventConsumer.onPaymentFailed(event);

        verify(processPaymentEventUseCase).processPaymentFailedEvent(any(PaymentFailedEvent.class));
    }
}
