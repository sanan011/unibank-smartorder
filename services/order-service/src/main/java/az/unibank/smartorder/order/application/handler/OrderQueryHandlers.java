package az.unibank.smartorder.order.application.handler;

import az.unibank.smartorder.order.application.query.GetOrderQuery;
import az.unibank.smartorder.order.application.query.ListOrdersQuery;
import az.unibank.smartorder.order.domain.model.aggregate.Order;
import az.unibank.smartorder.order.domain.model.valueobject.CustomerId;
import az.unibank.smartorder.order.domain.model.valueobject.OrderId;
import az.unibank.smartorder.order.domain.port.inbound.GetOrderUseCase;
import az.unibank.smartorder.order.domain.port.inbound.ListOrdersUseCase;
import az.unibank.smartorder.order.domain.port.outbound.OrderRepository;
import az.unibank.smartorder.web.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderQueryHandlers implements GetOrderUseCase, ListOrdersUseCase {

    private final OrderRepository orderRepository;

    @Override
    public Order getOrder(GetOrderQuery query) {
        return orderRepository.findById(OrderId.of(query.getOrderId()))
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Order not found", 404));
    }

    @Override
    public Page<Order> listOrders(ListOrdersQuery query) {
        return orderRepository.findAllByCustomerId(
                CustomerId.of(query.getCustomerId()),
                query.getStatus(),
                PageRequest.of(query.getPage(), query.getSize())
        );
    }
}
