package az.unibank.smartorder.order.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CancelOrderCommand {
    private final UUID orderId;
}
