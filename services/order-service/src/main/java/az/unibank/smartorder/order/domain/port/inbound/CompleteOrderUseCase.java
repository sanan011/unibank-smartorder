package az.unibank.smartorder.order.domain.port.inbound;

import az.unibank.smartorder.order.domain.model.aggregate.Order;
import az.unibank.smartorder.order.domain.model.valueobject.OrderId;

public interface CompleteOrderUseCase {
    Order completeOrder(OrderId orderId);
}
