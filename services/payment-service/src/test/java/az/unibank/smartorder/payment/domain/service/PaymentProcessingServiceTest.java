package az.unibank.smartorder.payment.domain.service;

import az.unibank.smartorder.events.order.OrderCreatedEvent;
import az.unibank.smartorder.events.payment.PaymentProcessedEvent;
import az.unibank.smartorder.payment.domain.model.aggregate.Payment;
import az.unibank.smartorder.payment.domain.port.outbound.EventPublisherPort;
import az.unibank.smartorder.payment.domain.port.outbound.IdempotencyRepository;
import az.unibank.smartorder.payment.domain.port.outbound.PaymentGatewayPort;
import az.unibank.smartorder.payment.domain.port.outbound.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentProcessingServiceTest {

    @Mock
    private IdempotencyRepository idempotencyRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentGatewayPort paymentGatewayPort;
    @Mock
    private EventPublisherPort eventPublisherPort;

    private PaymentProcessingService paymentProcessingService;

    @BeforeEach
    void setUp() {
        paymentProcessingService = new PaymentProcessingService(
                idempotencyRepository, paymentRepository, paymentGatewayPort, eventPublisherPort
        );
    }

    @Test
    void processPayment_whenEventAlreadyProcessed_skipsProcessing() {
        UUID eventId = UUID.randomUUID();
        OrderCreatedEvent event = new OrderCreatedEvent(eventId, "OrderCreatedEvent", "1.0", null, UUID.randomUUID(), new OrderCreatedEvent.Payload(UUID.randomUUID(), UUID.randomUUID(), List.of(), BigDecimal.TEN, "USD"));

        when(idempotencyRepository.exists(eventId.toString())).thenReturn(true);

        paymentProcessingService.processPayment(event);

        verify(paymentRepository, never()).save(any());
        verify(paymentGatewayPort, never()).processPayment(any(), any());
    }

    @Test
    void processPayment_whenFirstEvent_processesAndSavesIdempotency() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = new OrderCreatedEvent(eventId, "OrderCreatedEvent", "1.0", null, UUID.randomUUID(), new OrderCreatedEvent.Payload(orderId, UUID.randomUUID(), List.of(), BigDecimal.TEN, "USD"));

        when(idempotencyRepository.exists(eventId.toString())).thenReturn(false);
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentGatewayPort.processPayment(eq(orderId), any())).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArguments()[0]);

        paymentProcessingService.processPayment(event);

        verify(paymentGatewayPort).processPayment(eq(orderId), any());
        verify(eventPublisherPort).publish(eq("payment.processed"), any(PaymentProcessedEvent.class));
        verify(idempotencyRepository).save(eventId.toString(), "OrderCreatedEvent");
    }
}
