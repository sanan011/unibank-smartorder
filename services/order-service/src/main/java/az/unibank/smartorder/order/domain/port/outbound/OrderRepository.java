package az.unibank.smartorder.order.domain.port.outbound;

import az.unibank.smartorder.order.domain.model.aggregate.Order;
import az.unibank.smartorder.order.domain.model.valueobject.OrderId;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId id);
    Page<Order> findAllByCustomerId(az.unibank.smartorder.order.domain.model.valueobject.CustomerId customerId, String status, Pageable pageable);
}
