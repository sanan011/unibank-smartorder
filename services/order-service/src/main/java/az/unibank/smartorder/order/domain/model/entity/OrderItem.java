package az.unibank.smartorder.order.domain.model.entity;

import az.unibank.smartorder.order.domain.model.valueobject.Money;
import az.unibank.smartorder.order.domain.model.valueobject.ProductId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class OrderItem {
    private final UUID id;
    private final ProductId productId;
    private final String productName;
    private final Money unitPrice;
    private final int quantity;

    public Money getSubTotal() {
        return unitPrice.multiply(quantity);
    }
}
