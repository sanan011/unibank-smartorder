package az.unibank.smartorder.order.domain.port.inbound;

import az.unibank.smartorder.order.application.command.CancelOrderCommand;
import az.unibank.smartorder.order.domain.model.aggregate.Order;

public interface CancelOrderUseCase {
    Order cancelOrder(CancelOrderCommand command);
}
