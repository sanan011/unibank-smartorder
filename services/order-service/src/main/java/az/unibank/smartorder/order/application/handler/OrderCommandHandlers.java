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

@Service
@RequiredArgsConstructor
public class OrderCommandHandlers implements CreateOrderUseCase, CancelOrderUseCase {

    private final OrderCreationService orderCreationService;
    private final OrderCancellationService orderCancellationService;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
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
                .reduce(Money.ZERO, Money::add);

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
    public Order cancelOrder(CancelOrderCommand command) {
        Order order = orderRepository.findById(OrderId.of(command.getOrderId()))
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found", 404));
        return orderCancellationService.cancelOrder(order);
    }
}
