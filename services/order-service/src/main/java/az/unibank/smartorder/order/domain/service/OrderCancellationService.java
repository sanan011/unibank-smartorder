package az.unibank.smartorder.order.domain.service;

import az.unibank.smartorder.events.order.OrderCancelledEvent;
import az.unibank.smartorder.order.domain.model.aggregate.Order;
import az.unibank.smartorder.order.domain.model.aggregate.Product;
import az.unibank.smartorder.order.domain.model.entity.OrderItem;
import az.unibank.smartorder.order.domain.port.outbound.OrderRepository;
import az.unibank.smartorder.order.domain.port.outbound.OutboxRepository;
import az.unibank.smartorder.order.domain.port.outbound.ProductRepository;
import az.unibank.smartorder.web.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCancellationService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OutboxRepository outboxRepository;

    public Order cancelOrder(Order order) {
        order.cancel();

        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findByIdWithPessimisticLock(item.getProductId())
                .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "Product not found", 404));

            product.increaseStock(item.getQuantity());
            productRepository.save(product);
        }
        
        Order savedOrder = orderRepository.save(order);

        OrderCancelledEvent.Payload payload = new OrderCancelledEvent.Payload(
                savedOrder.getId().getValue(),
                savedOrder.getCustomerId().getValue(),
                savedOrder.getItems().stream()
                        .map(item -> new OrderCancelledEvent.OrderItemPayload(
                                item.getProductId().getValue(),
                                item.getQuantity()
                        ))
                        .toList()
        );
        OrderCancelledEvent event = new OrderCancelledEvent(savedOrder.getId().getValue(), payload);
        outboxRepository.save(event);

        return savedOrder;
    }
}
