package az.unibank.smartorder.order.application.handler;

import az.unibank.smartorder.order.application.command.CancelOrderCommand;
import az.unibank.smartorder.order.application.command.CreateOrderCommand;
import az.unibank.smartorder.order.domain.model.aggregate.Order;
import az.unibank.smartorder.order.domain.model.aggregate.Product;
import az.unibank.smartorder.order.domain.model.entity.OrderItem;
import az.unibank.smartorder.order.domain.model.valueobject.CustomerId;
import az.unibank.smartorder.order.domain.model.valueobject.Money;
import az.unibank.smartorder.order.domain.model.valueobject.OrderId;
import az.unibank.smartorder.order.domain.model.valueobject.ProductId;
import az.unibank.smartorder.order.domain.port.inbound.CancelOrderUseCase;
import az.unibank.smartorder.order.domain.port.inbound.CreateOrderUseCase;
import az.unibank.smartorder.order.domain.port.outbound.OrderRepository;
import az.unibank.smartorder.order.domain.port.outbound.ProductRepository;
import az.unibank.smartorder.order.domain.service.OrderCancellationService;
import az.unibank.smartorder.order.domain.service.OrderCreationService;
import az.unibank.smartorder.web.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import az.unibank.smartorder.order.domain.port.inbound.CompleteOrderUseCase;
import az.unibank.smartorder.order.domain.port.inbound.FailOrderUseCase;
import az.unibank.smartorder.order.domain.port.inbound.ProcessPaymentEventUseCase;
import az.unibank.smartorder.order.domain.port.outbound.IdempotencyRepository;
import az.unibank.smartorder.events.payment.PaymentProcessedEvent;
import az.unibank.smartorder.events.payment.PaymentFailedEvent;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCommandHandlers implements CreateOrderUseCase, CancelOrderUseCase, CompleteOrderUseCase, FailOrderUseCase, ProcessPaymentEventUseCase {

    private final OrderCreationService orderCreationService;
    private final OrderCancellationService orderCancellationService;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final az.unibank.smartorder.order.domain.service.StockReservationService stockReservationService;

    @Override
    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        List<OrderItem> orderItems = command.getItems().stream().map(itemCmd -> {
            Product product = productRepository.findById(ProductId.of(itemCmd.getProductId()))
                    .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found", 404));
            
            return OrderItem.builder()
                    .id(UUID.randomUUID())
                    .productId(product.getId())
                    .productName(product.getName())
                    .unitPrice(product.getPrice())
                    .quantity(itemCmd.getQuantity())
                    .build();
        }).collect(Collectors.toList());

        Money totalAmount = orderItems.stream()
                .map(OrderItem::getSubTotal)
                .reduce(Money::add)
                .orElse(Money.ZERO);

        Order order = Order.builder()
                .id(OrderId.of(UUID.randomUUID()))
                .customerId(CustomerId.of(command.getCustomerId()))
                .items(orderItems)
                .totalAmount(totalAmount)
                .createdAt(Instant.now())
                .build();
                
        return orderCreationService.createOrder(order);
    }

    @Override
    @Transactional
    public Order cancelOrder(CancelOrderCommand command) {
        Order order = orderRepository.findById(OrderId.of(command.getOrderId()))
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found", 404));
        return orderCancellationService.cancelOrder(order);
    }

    @Override
    @Transactional
    public Order completeOrder(OrderId orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found", 404));
        
        if (order.getStatus() == az.unibank.smartorder.order.domain.model.valueobject.OrderStatus.PAID) {
            log.info("Order {} is already in PAID state. Skipping completion.", orderId.getValue());
            return order;
        }

        if (order.getStatus() == az.unibank.smartorder.order.domain.model.valueobject.OrderStatus.PENDING) {
            order.processPayment();
        }
        order.pay();
        
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order failOrder(OrderId orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found", 404));
        
        if (order.getStatus() == az.unibank.smartorder.order.domain.model.valueobject.OrderStatus.PAYMENT_FAILED) {
            log.info("Order {} is already in PAYMENT_FAILED state. Skipping compensation.", orderId.getValue());
            return order; // Idempotent compensation
        }

        if (order.getStatus() == az.unibank.smartorder.order.domain.model.valueobject.OrderStatus.PENDING) {
            order.processPayment();
        }
        
        order.failPayment();
        
        // Compensating action: Release reserved stock
        stockReservationService.releaseStockFor(order);
        log.info("Released stock for failed order {}", orderId.getValue());
        
        // Possibly store reason in an OrderHistory entity if that was modeled, but not strictly needed for MVP
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void processPaymentProcessedEvent(PaymentProcessedEvent event) {
        String eventId = event.eventId().toString();
        if (idempotencyRepository.exists(eventId)) {
            log.info("Event {} already processed, skipping duplicate", eventId);
            return;
        }

        try {
            idempotencyRepository.save(eventId, event.eventType());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.info("Event {} processed concurrently, skipping duplicate", eventId);
            return;
        }

        OrderId orderId = OrderId.of(event.payload().orderId());
        completeOrder(orderId);
        log.info("Order {} payment completed successfully", orderId.getValue());
    }

    @Override
    @Transactional
    public void processPaymentFailedEvent(PaymentFailedEvent event) {
        String eventId = event.eventId().toString();
        if (idempotencyRepository.exists(eventId)) {
            log.info("Event {} already processed, skipping duplicate", eventId);
            return;
        }

        try {
            idempotencyRepository.save(eventId, event.eventType());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            log.info("Event {} processed concurrently, skipping duplicate", eventId);
            return;
        }

        OrderId orderId = OrderId.of(event.payload().orderId());
        failOrder(orderId, event.payload().reason() != null ? event.payload().reason() : "Unknown payment failure");
        log.info("Order {} payment failed. Reason: {}", orderId.getValue(), event.payload().reason());
    }
}
