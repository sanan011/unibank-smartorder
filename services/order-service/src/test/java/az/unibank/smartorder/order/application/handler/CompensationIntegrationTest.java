package az.unibank.smartorder.order.application.handler;

import az.unibank.smartorder.events.payment.PaymentFailedEvent;
import az.unibank.smartorder.order.domain.model.aggregate.Order;
import az.unibank.smartorder.order.domain.model.aggregate.Product;
import az.unibank.smartorder.order.domain.model.entity.OrderItem;
import az.unibank.smartorder.order.domain.model.valueobject.CustomerId;
import az.unibank.smartorder.order.domain.model.valueobject.Money;
import az.unibank.smartorder.order.domain.model.valueobject.OrderId;
import az.unibank.smartorder.order.domain.model.valueobject.OrderStatus;
import az.unibank.smartorder.order.domain.model.valueobject.ProductId;
import az.unibank.smartorder.order.domain.model.valueobject.StockQuantity;
import az.unibank.smartorder.order.domain.port.outbound.OrderRepository;
import az.unibank.smartorder.order.domain.port.outbound.ProductRepository;
import az.unibank.smartorder.order.infrastructure.persistence.jpa.repository.ProcessedEventJpaRepository;
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
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("test")
class CompensationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("order_db")
            .withUsername("order_user")
            .withPassword("order_pass");

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
    private OrderCommandHandlers orderCommandHandlers;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProcessedEventJpaRepository processedEventRepository;

    @BeforeEach
    void setUp() {
        processedEventRepository.deleteAll();
    }

    @Test
    void shouldCompensateExactlyOnceUnderConcurrentDelivery() throws InterruptedException {
        // Setup initial product and order state
        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .id(ProductId.of(productId))
                .name("Test Product")
                .price(Money.of(new BigDecimal("100.00"), "USD"))
                .stockQuantity(new StockQuantity(10)) // Start with 10
                .build();
        productRepository.save(product);

        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(OrderId.of(orderId))
                .customerId(CustomerId.of(UUID.randomUUID()))
                .status(OrderStatus.PROCESSING) // Simulating it was reserved
                .items(List.of(OrderItem.builder()
                        .id(UUID.randomUUID())
                        .productId(ProductId.of(productId))
                        .productName("Test Product")
                        .unitPrice(Money.of(new BigDecimal("100.00"), "USD"))
                        .quantity(2)
                        .build()))
                .totalAmount(Money.of(new BigDecimal("200.00"), "USD"))
                .createdAt(Instant.now())
                .build();
        orderRepository.save(order);

        // We simulate that the stock was reduced by 2 at creation time. We manually reduce it now.
        // Reload first so the aggregate carries its persisted @Version; saving the original builder-made
        // instance (version=null) would be treated as new and fail with a duplicate-key violation.
        Product reloadedProduct = productRepository.findById(ProductId.of(productId)).orElseThrow();
        reloadedProduct.decreaseStock(2);
        productRepository.save(reloadedProduct);
        assertThat(productRepository.findById(ProductId.of(productId)).get().getStockQuantity().getValue()).isEqualTo(8);

        // Compensation event
        UUID eventId = UUID.randomUUID();
        PaymentFailedEvent event = new PaymentFailedEvent(
                eventId, "PaymentFailedEvent", "1.0", null, UUID.randomUUID(),
                new PaymentFailedEvent.Payload(orderId, UUID.randomUUID(), UUID.randomUUID(), "Insufficient funds", 1)
        );

        // Concurrent delivery test
        int threadCount = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    latch.await();
                    orderCommandHandlers.processPaymentFailedEvent(event);
                } catch (Exception e) {
                    // Ignore concurrency exceptions, one will succeed
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown();
        boolean awaited = doneLatch.await(5, TimeUnit.SECONDS);
        assertThat(awaited).isTrue();

        // Assert exactly-once compensation:
        // Stock should be 10 (8 + 2 released) instead of 12 or 14
        Product finalProduct = productRepository.findById(ProductId.of(productId)).get();
        assertThat(finalProduct.getStockQuantity().getValue()).isEqualTo(10);

        Order finalOrder = orderRepository.findById(OrderId.of(orderId)).get();
        assertThat(finalOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);

        // Assert idempotency table record exists
        assertThat(processedEventRepository.findById(eventId)).isPresent();
    }
}
