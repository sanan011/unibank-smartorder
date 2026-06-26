package az.unibank.smartorder.order.application.handler;

import az.unibank.smartorder.events.payment.PaymentFailedEvent;
import az.unibank.smartorder.events.payment.PaymentProcessedEvent;
import az.unibank.smartorder.order.application.command.CreateOrderCommand;
import az.unibank.smartorder.order.application.command.CreateOrderCommand.OrderItemCommand;
import az.unibank.smartorder.order.domain.model.aggregate.Order;
import az.unibank.smartorder.order.domain.model.aggregate.Product;
import az.unibank.smartorder.order.domain.model.valueobject.Money;
import az.unibank.smartorder.order.domain.model.valueobject.OrderStatus;
import az.unibank.smartorder.order.domain.model.valueobject.ProductId;
import az.unibank.smartorder.order.domain.model.valueobject.StockQuantity;
import az.unibank.smartorder.order.domain.port.outbound.OrderRepository;
import az.unibank.smartorder.order.domain.port.outbound.ProductRepository;
import az.unibank.smartorder.order.infrastructure.persistence.jpa.repository.OutboxJpaRepository;
import az.unibank.smartorder.order.infrastructure.persistence.jpa.repository.ProcessedEventJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("local")
class SagaOrderE2EIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("order_db")
            .withUsername("order_user")
            .withPassword("order_pass");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management-alpine"));

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private OrderCommandHandlers orderCommandHandlers;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OutboxJpaRepository outboxRepository;

    @Autowired
    private ProcessedEventJpaRepository processedEventRepository;

    private UUID productId;

    @BeforeEach
    void setUp() {
        processedEventRepository.deleteAll();
        outboxRepository.deleteAll();
        
        productId = UUID.randomUUID();
        Product product = Product.builder()
                .id(ProductId.of(productId))
                .name("E2E Product")
                .price(Money.of(new BigDecimal("100.00"), "USD"))
                .stockQuantity(new StockQuantity(10))
                .build();
        productRepository.save(product);
    }

    @Test
    void shouldExecutePaymentSuccessFlowEndToEnd() {
        // 1. Order Created -> Stock Reserved
        CreateOrderCommand command = new CreateOrderCommand(
                UUID.randomUUID(),
                List.of(OrderItemCommand.builder().productId(productId).quantity(2).build())
        );
        Order createdOrder = orderCommandHandlers.createOrder(command);

        // Verify state is PENDING, Outbox is updated, and stock is reserved
        assertThat(createdOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(outboxRepository.findAll()).hasSize(1);
        Product productAfterOrder = productRepository.findById(ProductId.of(productId)).get();
        assertThat(productAfterOrder.getStockQuantity().getValue()).isEqualTo(8);

        // 2. Simulate Payment Processed Event (Consumed from RabbitMQ logically)
        PaymentProcessedEvent paymentEvent = new PaymentProcessedEvent(
                createdOrder.getId().getValue(),
                new PaymentProcessedEvent.Payload(
                        createdOrder.getId().getValue(), 
                        UUID.randomUUID(), 
                        UUID.randomUUID(), 
                        "SUCCESS", 
                        new BigDecimal("200.00"), 
                        "USD", 
                        "ref-123"
                )
        );

        orderCommandHandlers.processPaymentProcessedEvent(paymentEvent);

        // 3. Verify Order becomes PAID
        Order paidOrder = orderRepository.findById(createdOrder.getId()).get();
        assertThat(paidOrder.getStatus()).isEqualTo(OrderStatus.PAID);

        // 4. Verify processed_events inserted (Idempotency)
        assertThat(processedEventRepository.findById(paymentEvent.eventId())).isPresent();
    }

    @Test
    void shouldExecutePaymentFailureFlowEndToEnd() {
        // 1. Order Created -> Stock Reserved
        CreateOrderCommand command = new CreateOrderCommand(
                UUID.randomUUID(),
                List.of(OrderItemCommand.builder().productId(productId).quantity(2).build())
        );
        Order createdOrder = orderCommandHandlers.createOrder(command);

        // Verify initial state
        assertThat(createdOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        Product productAfterOrder = productRepository.findById(ProductId.of(productId)).get();
        assertThat(productAfterOrder.getStockQuantity().getValue()).isEqualTo(8);

        // 2. Simulate Payment Failed Event (Consumed from RabbitMQ logically)
        PaymentFailedEvent failureEvent = new PaymentFailedEvent(
                createdOrder.getId().getValue(),
                new PaymentFailedEvent.Payload(
                        createdOrder.getId().getValue(), 
                        UUID.randomUUID(), 
                        UUID.randomUUID(), 
                        "INSUFFICIENT_FUNDS", 
                        1
                )
        );

        orderCommandHandlers.processPaymentFailedEvent(failureEvent);

        // 3. Verify Order becomes PAYMENT_FAILED
        Order failedOrder = orderRepository.findById(createdOrder.getId()).get();
        assertThat(failedOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);

        // 4. Verify Saga Compensation -> Stock Released
        Product productAfterCompensation = productRepository.findById(ProductId.of(productId)).get();
        assertThat(productAfterCompensation.getStockQuantity().getValue()).isEqualTo(10); // Fully restored

        // 5. Verify processed_events inserted
        assertThat(processedEventRepository.findById(failureEvent.eventId())).isPresent();
    }
}
