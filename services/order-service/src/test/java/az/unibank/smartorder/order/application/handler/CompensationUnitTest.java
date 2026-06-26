package az.unibank.smartorder.order.application.handler;

import az.unibank.smartorder.events.payment.PaymentFailedEvent;
import az.unibank.smartorder.order.domain.model.aggregate.Order;
import az.unibank.smartorder.order.domain.model.valueobject.OrderId;
import az.unibank.smartorder.order.domain.model.valueobject.OrderStatus;
import az.unibank.smartorder.order.domain.port.outbound.IdempotencyRepository;
import az.unibank.smartorder.order.domain.port.outbound.OrderRepository;
import az.unibank.smartorder.order.domain.service.StockReservationService;
import az.unibank.smartorder.web.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompensationUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private StockReservationService stockReservationService;

    @Mock
    private IdempotencyRepository idempotencyRepository;

    @InjectMocks
    private OrderCommandHandlers orderCommandHandlers;

    private Order processingOrder;
    private Order failedOrder;
    private OrderId orderId;

    @BeforeEach
    void setUp() {
        orderId = OrderId.of(UUID.randomUUID());
        processingOrder = Order.builder()
                .id(orderId)
                .status(OrderStatus.PROCESSING)
                .build();
                
        failedOrder = Order.builder()
                .id(orderId)
                .status(OrderStatus.PAYMENT_FAILED)
                .build();
    }

    @Test
    void shouldSuccessfullyCompensateProcessingOrder() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(processingOrder));
        when(orderRepository.save(processingOrder)).thenReturn(processingOrder);

        Order result = orderCommandHandlers.failOrder(orderId, "Insufficient funds");

        assertThat(result.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
        verify(stockReservationService).releaseStockFor(processingOrder);
        verify(orderRepository).save(processingOrder);
    }

    @Test
    void shouldBeIdempotentForAlreadyFailedOrder() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(failedOrder));

        Order result = orderCommandHandlers.failOrder(orderId, "Insufficient funds");

        assertThat(result.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
        verify(stockReservationService, never()).releaseStockFor(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldSkipDuplicateCompensationEvents() {
        UUID eventId = UUID.randomUUID();
        PaymentFailedEvent event = new PaymentFailedEvent(eventId, "PaymentFailedEvent", "1.0", null, UUID.randomUUID(),
                new PaymentFailedEvent.Payload(orderId.getValue(), UUID.randomUUID(), "Failed", 1));

        when(idempotencyRepository.exists(eventId.toString())).thenReturn(true);

        orderCommandHandlers.processPaymentFailedEvent(event);

        verify(orderRepository, never()).findById(any());
        verify(stockReservationService, never()).releaseStockFor(any());
    }

    @Test
    void shouldSkipDuplicateEventsOnConcurrentDelivery() {
        UUID eventId = UUID.randomUUID();
        az.unibank.smartorder.events.payment.PaymentProcessedEvent event = new az.unibank.smartorder.events.payment.PaymentProcessedEvent(
                orderId.getValue(),
                new az.unibank.smartorder.events.payment.PaymentProcessedEvent.Payload(orderId.getValue(), UUID.randomUUID(), "SUCCESS", new java.math.BigDecimal("150.00"), "USD", "ref-123")
        );
        // Override the generated eventId to our specific one for testing
        az.unibank.smartorder.events.payment.PaymentProcessedEvent eventWithId = new az.unibank.smartorder.events.payment.PaymentProcessedEvent(
                eventId,
                "PaymentProcessedEvent",
                "1.0",
                java.time.Instant.now(),
                orderId.getValue(),
                event.payload()
        );

        when(idempotencyRepository.exists(eventId.toString())).thenReturn(false);
        doThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate"))
                .when(idempotencyRepository).save(eventId.toString(), "PaymentProcessedEvent");

        orderCommandHandlers.processPaymentProcessedEvent(eventWithId);

        verify(orderRepository, never()).findById(any());
    }

    @Test
    void shouldThrowWhenTransitionIsInvalid() {
        Order pendingOrder = Order.builder().id(orderId).status(OrderStatus.PAID).build();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(pendingOrder));

        assertThrows(BusinessException.class, () -> orderCommandHandlers.failOrder(orderId, "Fail"));
    }
}
