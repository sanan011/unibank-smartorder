package az.unibank.smartorder.order.application.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class GetOrderQuery {
    private final UUID orderId;
}
