package az.unibank.smartorder.payment.infrastructure.gateway;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class MockPaymentGatewayAdapter {

    private final AtomicBoolean forceFailure = new AtomicBoolean(false);

    /**
     * Set to true to force failures to test circuit breaker
     */
    public void setForceFailure(boolean force) {
        this.forceFailure.set(force);
    }

    @CircuitBreaker(name = "paymentGateway", fallbackMethod = "processPaymentFallback")
    @Retry(name = "paymentGateway")
    public boolean processPayment(UUID orderId, BigDecimal amount) {
        log.info("Attempting to process payment for order: {}, amount: {}", orderId, amount);

        if (forceFailure.get()) {
            log.error("Simulating payment gateway failure for order: {}", orderId);
            throw new PaymentGatewayException("Simulated gateway failure");
        }

        // Simulate 20% random failure rate in real-world scenario if not forced
        if (Math.random() < 0.2) {
            log.warn("Random payment gateway timeout for order: {}", orderId);
            throw new PaymentGatewayException("Random gateway timeout");
        }

        log.info("Payment processed successfully for order: {}", orderId);
        return true;
    }

    public boolean processPaymentFallback(UUID orderId, BigDecimal amount, Throwable t) {
        log.error("Fallback invoked for order: {} after exceptions. Reason: {}", orderId, t.getMessage());
        return false;
    }
}
