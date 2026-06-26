package az.unibank.smartorder.payment.domain.port.inbound;

import az.unibank.smartorder.events.order.OrderCreatedEvent;

public interface ProcessOrderPaymentUseCase {
    void processPayment(OrderCreatedEvent event);
}
