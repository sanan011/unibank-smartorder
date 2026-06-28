package az.unibank.smartorder.payment.domain.service;

import az.unibank.smartorder.events.order.OrderCreatedEvent;
import az.unibank.smartorder.events.payment.PaymentFailedEvent;
import az.unibank.smartorder.events.payment.PaymentProcessedEvent;
import az.unibank.smartorder.payment.domain.model.aggregate.Payment;
import az.unibank.smartorder.payment.domain.model.valueobject.PaymentId;
import az.unibank.smartorder.payment.domain.model.valueobject.PaymentStatus;
import az.unibank.smartorder.payment.domain.port.outbound.IdempotencyRepository;
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
public class PaymentProcessingService {

    private final IdempotencyRepository idempotencyRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment initializePayment(OrderCreatedEvent event) {
        String eventId = event.eventId().toString();

        if (idempotencyRepository.exists(eventId)) {
            log.info("Event {} already processed, skipping duplicate", eventId);
            return null; // Signals it's already processed
        }

        UUID orderId = event.payload().orderId();
        UUID customerId = event.payload().customerId();
        BigDecimal amount = event.payload().totalAmount();
        String currency = event.payload().currency();

        // 1. Check if payment already exists for this order
        Payment payment = paymentRepository.findByOrderId(orderId).orElseGet(() -> {
            Payment newPayment = Payment.builder()
                    .id(PaymentId.of(UUID.randomUUID()))
                    .orderId(orderId)
                    .customerId(customerId != null ? customerId : UUID.randomUUID())
                    .amount(amount)
                    .currency(currency != null ? currency : "AZN")
                    .status(PaymentStatus.PENDING)
                    .attemptCount(0)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            return paymentRepository.save(newPayment);
        });

        // 2. State transition
        if (payment.getStatus() == PaymentStatus.PENDING || payment.getStatus() == PaymentStatus.FAILED) {
            if (payment.getStatus() == PaymentStatus.FAILED) {
                payment.resetToPending();
            }
            payment.process();
            return paymentRepository.save(payment);
        }

        return payment;
    }

    @Transactional
    public az.unibank.smartorder.events.DomainEvent finalizePayment(PaymentId paymentId, boolean success, OrderCreatedEvent event) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("Payment not found"));

        az.unibank.smartorder.events.DomainEvent resultEvent;

        if (success) {
            String gatewayRef = "MOCK-REF-" + payment.getId().value().toString().substring(0, 8);
            payment.markSuccess(gatewayRef);
            paymentRepository.save(payment);

            PaymentProcessedEvent.Payload processedPayload = new PaymentProcessedEvent.Payload(
                    payment.getOrderId(), payment.getCustomerId(), payment.getId().value(), "SUCCESS", payment.getAmount(), payment.getCurrency(), gatewayRef
            );
            resultEvent = new PaymentProcessedEvent(event.correlationId(), processedPayload);
        } else {
            payment.markFailed("Payment declined by mock gateway");
            paymentRepository.save(payment);

            PaymentFailedEvent.Payload failedPayload = new PaymentFailedEvent.Payload(
                    payment.getOrderId(), payment.getCustomerId(), payment.getId().value(), "Payment declined by mock gateway", payment.getAttemptCount()
            );
            resultEvent = new PaymentFailedEvent(event.correlationId(), failedPayload);
        }

        // Save Idempotency
        idempotencyRepository.save(event.eventId().toString(), event.eventType());
        return resultEvent;
    }

    @Transactional
    public void markEventProcessed(OrderCreatedEvent event) {
        idempotencyRepository.save(event.eventId().toString(), event.eventType());
    }
}
