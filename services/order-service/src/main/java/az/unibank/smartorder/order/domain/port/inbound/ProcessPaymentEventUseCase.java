package az.unibank.smartorder.order.domain.port.inbound;

import az.unibank.smartorder.events.payment.PaymentProcessedEvent;
import az.unibank.smartorder.events.payment.PaymentFailedEvent;

public interface ProcessPaymentEventUseCase {
    void processPaymentProcessedEvent(PaymentProcessedEvent event);
    void processPaymentFailedEvent(PaymentFailedEvent event);
}
