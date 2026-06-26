package az.unibank.smartorder.order.domain.port.inbound;

import az.unibank.smartorder.order.application.command.CreateOrderCommand;
import az.unibank.smartorder.order.domain.model.aggregate.Order;

public interface CreateOrderUseCase {
    Order createOrder(CreateOrderCommand command);
}
