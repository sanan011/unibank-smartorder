package az.unibank.smartorder.payment.application.handler;

import az.unibank.smartorder.events.DomainEvent;
import az.unibank.smartorder.events.order.OrderCreatedEvent;
import az.unibank.smartorder.events.payment.PaymentProcessedEvent;
import az.unibank.smartorder.events.payment.PaymentFailedEvent;
import az.unibank.smartorder.payment.domain.model.aggregate.Payment;
import az.unibank.smartorder.payment.domain.model.valueobject.PaymentStatus;
import az.unibank.smartorder.payment.domain.port.inbound.ProcessOrderPaymentUseCase;
import az.unibank.smartorder.payment.domain.port.outbound.EventPublisherPort;
import az.unibank.smartorder.payment.domain.port.outbound.PaymentGatewayPort;
import az.unibank.smartorder.payment.domain.service.PaymentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCommandHandlers implements ProcessOrderPaymentUseCase {

    private final PaymentProcessingService paymentProcessingService;
    private final PaymentGatewayPort paymentGatewayPort;
    private final EventPublisherPort eventPublisherPort;

    @Override
    public void processPayment(OrderCreatedEvent event) {
        // 1. Initialize Payment (Transactional)
        Payment payment = paymentProcessingService.initializePayment(event);
        if (payment == null) {
            // Already processed
            return;
        }

        // initializePayment() transitions a chargeable payment to PROCESSING. Any other status
        // (e.g. SUCCESS for an already-paid order) means there is nothing to charge — skip the gateway.
        if (payment.getStatus() != PaymentStatus.PROCESSING) {
            log.info("Payment for order {} is already in status {}, skipping gateway call", payment.getOrderId(), payment.getStatus());
            paymentProcessingService.markEventProcessed(event);
            return;
        }

        // 2. Call Gateway (Non-Transactional)
        boolean success;
        try {
            success = paymentGatewayPort.processPayment(payment.getOrderId(), payment.getAmount());
        } catch (Exception e) {
            log.error("Gateway exception", e);
            success = false;
        }

        // 3. Finalize Payment (Transactional)
        DomainEvent resultEvent = paymentProcessingService.finalizePayment(payment.getId(), success, event);

        // 4. Publish Event
        if (resultEvent instanceof PaymentProcessedEvent) {
            eventPublisherPort.publish("payment.processed", resultEvent);
        } else if (resultEvent instanceof PaymentFailedEvent) {
            eventPublisherPort.publish("payment.failed", resultEvent);
        }
    }
}
