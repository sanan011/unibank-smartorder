package az.unibank.smartorder.events;

import az.unibank.smartorder.events.order.OrderCreatedEvent;
import az.unibank.smartorder.events.payment.PaymentFailedEvent;
import az.unibank.smartorder.events.payment.PaymentProcessedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventContractValidationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void shouldSerializeAndDeserializeOrderCreatedEvent() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId,
                new OrderCreatedEvent.Payload(
                        orderId,
                        customerId,
                        java.util.List.of(),
                        new BigDecimal("150.00"),
                        "USD"
                )
        );

        String json = objectMapper.writeValueAsString(event);
        OrderCreatedEvent deserialized = objectMapper.readValue(json, OrderCreatedEvent.class);

        assertThat(deserialized.eventId()).isEqualTo(event.eventId());
        assertThat(deserialized.eventType()).isEqualTo("OrderCreatedEvent");
        assertThat(deserialized.eventVersion()).isEqualTo("1.0");
        assertThat(deserialized.occurredAt()).isEqualTo(event.occurredAt());
        assertThat(deserialized.correlationId()).isEqualTo(orderId);
        
        assertThat(deserialized.payload().orderId()).isEqualTo(orderId);
        assertThat(deserialized.payload().customerId()).isEqualTo(customerId);
        assertThat(deserialized.payload().totalAmount()).isEqualTo(new BigDecimal("150.00"));
        assertThat(deserialized.payload().currency()).isEqualTo("USD");
    }

    @Test
    void shouldSerializeAndDeserializePaymentProcessedEvent() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        PaymentProcessedEvent event = new PaymentProcessedEvent(
                orderId,
                new PaymentProcessedEvent.Payload(orderId, customerId, paymentId, "SUCCESS", new BigDecimal("150.00"), "USD", "ref-123")
        );

        String json = objectMapper.writeValueAsString(event);
        PaymentProcessedEvent deserialized = objectMapper.readValue(json, PaymentProcessedEvent.class);

        assertThat(deserialized.eventId()).isEqualTo(event.eventId());
        assertThat(deserialized.eventType()).isEqualTo("PaymentProcessedEvent");
        assertThat(deserialized.correlationId()).isEqualTo(orderId);
        
        assertThat(deserialized.payload().orderId()).isEqualTo(orderId);
        assertThat(deserialized.payload().customerId()).isEqualTo(customerId);
        assertThat(deserialized.payload().paymentId()).isEqualTo(paymentId);
        assertThat(deserialized.payload().status()).isEqualTo("SUCCESS");
    }

    @Test
    void shouldSerializeAndDeserializePaymentFailedEvent() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        PaymentFailedEvent event = new PaymentFailedEvent(
                orderId,
                new PaymentFailedEvent.Payload(orderId, customerId, paymentId, "INSUFFICIENT_FUNDS", 1)
        );

        String json = objectMapper.writeValueAsString(event);
        PaymentFailedEvent deserialized = objectMapper.readValue(json, PaymentFailedEvent.class);

        assertThat(deserialized.eventId()).isEqualTo(event.eventId());
        assertThat(deserialized.eventType()).isEqualTo("PaymentFailedEvent");
        assertThat(deserialized.correlationId()).isEqualTo(orderId);
        
        assertThat(deserialized.payload().orderId()).isEqualTo(orderId);
        assertThat(deserialized.payload().customerId()).isEqualTo(customerId);
        assertThat(deserialized.payload().paymentId()).isEqualTo(paymentId);
        assertThat(deserialized.payload().reason()).isEqualTo("INSUFFICIENT_FUNDS");
        assertThat(deserialized.payload().attemptCount()).isEqualTo(1);
    }
}
