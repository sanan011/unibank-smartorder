package az.unibank.smartorder.payment.infrastructure.messaging.consumer;

import az.unibank.smartorder.events.order.OrderCreatedEvent;
import az.unibank.smartorder.payment.domain.port.inbound.ProcessOrderPaymentUseCase;
import az.unibank.smartorder.payment.infrastructure.persistence.jpa.entity.PaymentJpaEntity;
import az.unibank.smartorder.payment.infrastructure.persistence.jpa.repository.PaymentJpaRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("test")
class SagaPaymentE2EIntegrationTest {

    @Container
    @SuppressWarnings("resource")
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
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
    }

    @Autowired
    private ProcessOrderPaymentUseCase processOrderPaymentUseCase;

    @Autowired
    private PaymentJpaRepository paymentJpaRepository;

    @Test
    void shouldProcessOrderCreatedEventAndCreatePayment() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId,
                new OrderCreatedEvent.Payload(
                        orderId,
                        customerId,
                        List.of(),
                        new BigDecimal("100.00"),
                        "USD"
                )
        );
        
        // Act - call use case directly, bypassing messaging
        processOrderPaymentUseCase.processPayment(event);
        
        // Assert
        Optional<PaymentJpaEntity> payment = paymentJpaRepository.findByOrderId(orderId);
        assertThat(payment).isPresent();
        assertThat(payment.get().getOrderId()).isEqualTo(orderId);
    }
}
