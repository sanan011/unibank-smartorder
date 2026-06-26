package az.unibank.smartorder.order.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
public final class CustomerId {
    private final UUID value;

    public CustomerId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        this.value = value;
    }

    public static CustomerId of(UUID value) {
        return new CustomerId(value);
    }
}

