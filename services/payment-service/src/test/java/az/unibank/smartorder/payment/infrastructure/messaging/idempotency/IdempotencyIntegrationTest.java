package az.unibank.smartorder.payment.infrastructure.messaging.idempotency;

import az.unibank.smartorder.events.order.OrderCreatedEvent;
import az.unibank.smartorder.payment.domain.port.inbound.ProcessOrderPaymentUseCase;
import az.unibank.smartorder.payment.infrastructure.persistence.jpa.repository.ProcessedEventJpaRepository;
import az.unibank.smartorder.payment.infrastructure.persistence.jpa.repository.PaymentJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("local")
@Disabled("Requires Docker for Testcontainers")
class IdempotencyIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("payment_db")
            .withUsername("payment_user")
            .withPassword("payment_pass");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management-alpine"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
    }

    @Autowired
    private ProcessOrderPaymentUseCase processOrderPaymentUseCase;

    @Autowired
    private ProcessedEventJpaRepository processedEventRepository;

    @Autowired
    private PaymentJpaRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        processedEventRepository.deleteAll();
    }

    @Test
    void shouldProcessMessageExactlyOnceUnderConcurrentDelivery() throws InterruptedException {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        
        OrderCreatedEvent event = new OrderCreatedEvent(
                eventId, "OrderCreatedEvent", "1.0", null, UUID.randomUUID(),
                new OrderCreatedEvent.Payload(orderId, UUID.randomUUID(), List.of(), BigDecimal.TEN, "USD")
        );

        int threadCount = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    latch.await();
                    processOrderPaymentUseCase.processPayment(event);
                } catch (Exception e) {
                    // One thread will succeed, others might fail with DataIntegrityViolationException or just return cleanly depending on timing
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        boolean awaited = doneLatch.await(5, TimeUnit.SECONDS);
        assertThat(awaited).isTrue();

        assertThat(processedEventRepository.findById(eventId)).isPresent();
        assertThat(paymentRepository.findByOrderId(orderId)).isPresent();
        
        // Assert that attempt count is exactly 1 (meaning it was processed exactly once)
        assertThat(paymentRepository.findByOrderId(orderId).get().getAttemptCount()).isEqualTo(1);
    }
}
