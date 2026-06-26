package az.unibank.smartorder.order.domain.port.inbound;

import az.unibank.smartorder.order.application.query.GetOrderQuery;
import az.unibank.smartorder.order.domain.model.aggregate.Order;

public interface GetOrderUseCase {
    Order getOrder(GetOrderQuery query);
}
