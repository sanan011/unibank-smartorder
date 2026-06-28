package az.unibank.smartorder.payment.infrastructure.gateway;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("local") // local profile disables auth if needed, but not web here
class CircuitBreakerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("payment_db")
            .withUsername("payment_user")
            .withPassword("payment_pass");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.12-management"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
    }

    @Autowired
    private MockPaymentGatewayAdapter paymentGatewayAdapter;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("paymentGateway");
        circuitBreaker.transitionToClosedState();
        paymentGatewayAdapter.setForceFailure(false);
    }

    @Test
    void shouldOpenCircuitBreakerAfterFailuresAndFallback() {
        // Force failures
        paymentGatewayAdapter.setForceFailure(true);

        UUID orderId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.00");

        // The configured minimumNumberOfCalls is 3, failure threshold is 50%
        // Max attempts for retry is 3. So a single call that fails 3 times triggers an open circuit!
        
        // Attempt 1 -> fails 3 times internally due to @Retry
        boolean result1 = paymentGatewayAdapter.processPayment(orderId, amount);
        assertThat(result1).isFalse(); // Fallback returns false
        
        // After 3 failed internal calls (from 1 method invocation with 3 retries), the circuit breaker should be OPEN.
        // Wait, Resilience4j records each retry attempt if configured, or just the final exception.
        // Let's make 5 separate calls to be absolutely sure we hit minimumNumberOfCalls=3 for slidingWindowSize=5.
        
        for (int i = 0; i < 4; i++) {
            paymentGatewayAdapter.processPayment(orderId, amount);
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // Once OPEN, subsequent calls fail immediately and return fallback (false) without executing logic
        paymentGatewayAdapter.setForceFailure(false); // Gateway recovers, but circuit is OPEN
        boolean resultWhenOpen = paymentGatewayAdapter.processPayment(orderId, amount);
        
        assertThat(resultWhenOpen).isFalse(); // Still false because circuit is open and invokes fallback
    }
}
