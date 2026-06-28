package az.unibank.smartorder.order.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
public final class ProductId {
    private final UUID value;

    public ProductId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        this.value = value;
    }

    public static ProductId of(UUID value) {
        return new ProductId(value);
    }
}

