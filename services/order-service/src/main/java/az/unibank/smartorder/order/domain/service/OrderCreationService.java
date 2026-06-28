package az.unibank.smartorder.order.domain.service;

import az.unibank.smartorder.events.order.OrderCreatedEvent;
import az.unibank.smartorder.order.domain.model.aggregate.Order;
import az.unibank.smartorder.order.domain.port.outbound.OrderRepository;
import az.unibank.smartorder.order.domain.port.outbound.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderCreationService {

    private final StockReservationService stockReservationService;
    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;

    public Order createOrder(Order order) {
        order.initialize();
        stockReservationService.reserveStockFor(order);
        
        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent.Payload payload = new OrderCreatedEvent.Payload(
                savedOrder.getId().getValue(),
                savedOrder.getCustomerId().getValue(),
                savedOrder.getItems().stream()
                        .map(item -> new OrderCreatedEvent.OrderItemPayload(
                                item.getProductId().getValue(),
                                item.getProductName(),
                                item.getUnitPrice().getAmount(),
                                item.getUnitPrice().getCurrency(),
                                item.getQuantity()
                        ))
                        .collect(Collectors.toList()),
                savedOrder.getTotalAmount().getAmount(),
                savedOrder.getTotalAmount().getCurrency()
        );

        OrderCreatedEvent event = new OrderCreatedEvent(savedOrder.getId().getValue(), payload);
        outboxRepository.save(event);

        return savedOrder;
    }
}
