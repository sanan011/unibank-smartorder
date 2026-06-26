package az.unibank.smartorder.payment.domain.port.outbound;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentGatewayPort {
    boolean processPayment(UUID orderId, BigDecimal amount);
}
