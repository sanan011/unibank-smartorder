package az.unibank.smartorder.payment.infrastructure.messaging.consumer;

import az.unibank.smartorder.events.order.OrderCreatedEvent;
import az.unibank.smartorder.payment.domain.model.aggregate.Payment;
import az.unibank.smartorder.payment.domain.port.outbound.PaymentRepository;
import az.unibank.smartorder.payment.infrastructure.persistence.jpa.repository.ProcessedEventJpaRepository;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("test")
class SagaPaymentE2EIntegrationTest {

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
    private OrderEventConsumer orderEventConsumer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ProcessedEventJpaRepository processedEventRepository;

    @BeforeEach
    void setUp() {
        processedEventRepository.deleteAll();
    }

    @Test
    void shouldProcessOrderCreatedEventAndCreatePayment() {
        // 1. Order Created Event received
        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId,
                new OrderCreatedEvent.Payload(
                        orderId,
                        UUID.randomUUID(),
                        List.of(),
                        new BigDecimal("250.00"),
                        "USD"
                )
        );

        // 2. Consume Event
        @SuppressWarnings("unchecked")
        Map<String, Object> eventMap = objectMapper.convertValue(event, Map.class);
        orderEventConsumer.onOrderCreated(eventMap);

        // 3. Verify Payment is processed and saved
        Optional<Payment> paymentOpt = paymentRepository.findByOrderId(orderId);
        assertThat(paymentOpt).isPresent();
        Payment payment = paymentOpt.get();
        assertThat(payment.getAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
        
        // Ensure state is correctly processed (SUCCESS or FAILED, mostly SUCCESS in tests based on mock gateway)
        assertThat(payment.getTransactions()).isNotEmpty();

        // 4. Verify processed_events inserted for idempotency
        assertThat(processedEventRepository.findById(event.eventId())).isPresent();
    }
}
