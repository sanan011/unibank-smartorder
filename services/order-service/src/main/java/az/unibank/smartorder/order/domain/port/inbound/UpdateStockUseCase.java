package az.unibank.smartorder.order.domain.port.inbound;

import az.unibank.smartorder.order.application.command.UpdateStockCommand;

public interface UpdateStockUseCase {
    void updateStock(UpdateStockCommand command);
}
