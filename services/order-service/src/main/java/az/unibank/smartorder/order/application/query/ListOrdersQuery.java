package az.unibank.smartorder.order.application.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ListOrdersQuery {
    private final UUID customerId;
    private final String status;
    private final int page;
    private final int size;
}
