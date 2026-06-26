package az.unibank.smartorder.order.domain.port.inbound;

import az.unibank.smartorder.order.application.query.ListOrdersQuery;
import az.unibank.smartorder.order.domain.model.aggregate.Order;
import org.springframework.data.domain.Page;

public interface ListOrdersUseCase {
    Page<Order> listOrders(ListOrdersQuery query);
}
