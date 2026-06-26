package az.unibank.smartorder.payment.domain.port.outbound;

import az.unibank.smartorder.payment.domain.model.aggregate.Payment;
import az.unibank.smartorder.payment.domain.model.valueobject.PaymentId;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(PaymentId id);
    Optional<Payment> findByOrderId(UUID orderId);
}
