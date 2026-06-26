package az.unibank.smartorder.order.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UpdateStockCommand {
    private final UUID productId;
    private final int quantity;
}
