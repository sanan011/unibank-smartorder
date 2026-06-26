package az.unibank.smartorder.order.application.command;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}, justification = "Immutable lists are used")
public class CreateOrderCommand {
    private final UUID customerId;
    private final List<OrderItemCommand> items;

    public CreateOrderCommand(UUID customerId, List<OrderItemCommand> items) {
        this.customerId = customerId;
        this.items = items == null ? List.of() : List.copyOf(items);
    }

    public List<OrderItemCommand> getItems() {
        return List.copyOf(items);
    }

    @Getter
    @Builder
    public static class OrderItemCommand {
        private final UUID productId;
        private final int quantity;
    }
}
