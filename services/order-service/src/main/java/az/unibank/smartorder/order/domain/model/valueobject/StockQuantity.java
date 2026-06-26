package az.unibank.smartorder.order.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public final class StockQuantity {
    private final int value;

    public StockQuantity(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        this.value = value;
    }

    public static StockQuantity of(int value) {
        return new StockQuantity(value);
    }
    
    public StockQuantity add(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to add cannot be negative");
        }
        return new StockQuantity(this.value + amount);
    }
    
    public StockQuantity subtract(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to subtract cannot be negative");
        }
        return new StockQuantity(this.value - amount);
    }
}

