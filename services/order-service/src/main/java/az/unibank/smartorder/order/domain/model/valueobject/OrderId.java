package az.unibank.smartorder.order.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
public final class OrderId {
    private final UUID value;

    public OrderId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        this.value = value;
    }

    public static OrderId of(UUID value) {
        return new OrderId(value);
    }
}

