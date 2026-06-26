package az.unibank.smartorder.order.domain.model.aggregate;

import az.unibank.smartorder.order.domain.model.valueobject.Money;
import az.unibank.smartorder.order.domain.model.valueobject.ProductId;
import az.unibank.smartorder.order.domain.model.valueobject.StockQuantity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Product {
    private final ProductId id;
    private final String name;
    private final Money price;
    private StockQuantity stockQuantity;
    private Long version;

    public void updateStock(StockQuantity newQuantity) {
        this.stockQuantity = newQuantity;
    }

    public void decreaseStock(int quantity) {
        this.stockQuantity = this.stockQuantity.subtract(quantity);
    }

    public void increaseStock(int quantity) {
        this.stockQuantity = this.stockQuantity.add(quantity);
    }
}
