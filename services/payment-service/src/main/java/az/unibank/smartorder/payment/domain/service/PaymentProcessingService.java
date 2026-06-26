package az.unibank.smartorder.payment.domain.service;

import az.unibank.smartorder.events.order.OrderCreatedEvent;
import az.unibank.smartorder.events.payment.PaymentFailedEvent;
import az.unibank.smartorder.events.payment.PaymentProcessedEvent;
import az.unibank.smartorder.payment.domain.model.aggregate.Payment;
import az.unibank.smartorder.payment.domain.model.valueobject.PaymentId;
import az.unibank.smartorder.payment.domain.model.valueobject.PaymentStatus;
import az.unibank.smartorder.payment.domain.port.inbound.ProcessOrderPaymentUseCase;
import az.unibank.smartorder.payment.domain.port.outbound.EventPublisherPort;
import az.unibank.smartorder.payment.domain.port.outbound.IdempotencyRepository;
import az.unibank.smartorder.payment.domain.port.outbound.PaymentGatewayPort;
import az.unibank.smartorder.payment.domain.port.outbound.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessingService implements ProcessOrderPaymentUseCase {

    private final IdempotencyRepository idempotencyRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final EventPublisherPort eventPublisherPort;

    @Override
    @Transactional
    public void processPayment(OrderCreatedEvent event) {
        String eventId = event.eventId().toString();

        if (idempotencyRepository.exists(eventId)) {
            log.info("Event {} already processed, skipping duplicate", eventId);
            return;
        }

        UUID orderId = event.payload().orderId();
        UUID customerId = event.payload().customerId();
        BigDecimal amount = event.payload().totalAmount();
        String currency = event.payload().currency();
        UUID correlationId = event.correlationId();

        // 1. Check if payment already exists for this order (safeguard)
        Payment payment = paymentRepository.findByOrderId(orderId).orElseGet(() -> {
            Payment newPayment = Payment.builder()
                    .id(PaymentId.of(UUID.randomUUID()))
                    .orderId(orderId)
                    .customerId(customerId != null ? customerId : UUID.randomUUID()) // Fallback if missing
                    .amount(amount)
                    .currency(currency != null ? currency : "AZN") // Fallback if missing
                    .status(PaymentStatus.PENDING)
                    .attemptCount(0)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            return paymentRepository.save(newPayment);
        });

        // 2. State transition
        if (payment.getStatus() == PaymentStatus.PENDING || payment.getStatus() == PaymentStatus.FAILED) {
            payment.process();
            payment = paymentRepository.save(payment);
        } else {
            log.info("Payment for order {} is in status {}, skipping gateway call", orderId, payment.getStatus());
            idempotencyRepository.save(eventId, event.eventType());
            return;
        }

        // 3. Gateway Call
        boolean success;
        try {
            success = paymentGatewayPort.processPayment(orderId, amount);
        } catch (Exception e) {
            log.error("Gateway exception", e);
            success = false;
        }

        // 4. Update state and publish events
        if (success) {
            String gatewayRef = "MOCK-REF-" + payment.getId().value().toString().substring(0, 8);
            payment.markSuccess(gatewayRef);
            paymentRepository.save(payment);

            PaymentProcessedEvent.Payload processedPayload = new PaymentProcessedEvent.Payload(
                    orderId, payment.getId().value(), "SUCCESS", amount, payment.getCurrency(), gatewayRef
            );
            eventPublisherPort.publish("payment.processed", new PaymentProcessedEvent(correlationId, processedPayload));
        } else {
            payment.markFailed("Payment declined by mock gateway");
            paymentRepository.save(payment);

            PaymentFailedEvent.Payload failedPayload = new PaymentFailedEvent.Payload(
                    orderId, payment.getId().value(), "Payment declined by mock gateway", payment.getAttemptCount()
            );
            eventPublisherPort.publish("payment.failed", new PaymentFailedEvent(correlationId, failedPayload));
        }

        // 5. Save Idempotency
        idempotencyRepository.save(eventId, event.eventType());
    }
}
