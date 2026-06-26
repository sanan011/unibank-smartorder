package az.unibank.smartorder.order.adapter.inbound.web.mapper;

import az.unibank.smartorder.order.adapter.inbound.web.dto.response.ProductResponse;
import az.unibank.smartorder.order.domain.model.aggregate.Product;
import az.unibank.smartorder.order.domain.model.valueobject.Money;
import az.unibank.smartorder.order.domain.model.valueobject.ProductId;
import az.unibank.smartorder.order.domain.model.valueobject.StockQuantity;
import java.math.BigDecimal;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-26T17:38:29+0400",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.7.jar, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class ProductWebMapperImpl implements ProductWebMapper {

    @Override
    public ProductResponse toResponse(Product product) {
        if ( product == null ) {
            return null;
        }

        UUID id = null;
        BigDecimal price = null;
        String currency = null;
        int stockQuantity = 0;
        String name = null;

        id = productIdValue( product );
        price = productPriceAmount( product );
        currency = productPriceCurrency( product );
        stockQuantity = productStockQuantityValue( product );
        name = product.getName();

        ProductResponse productResponse = new ProductResponse( id, name, price, currency, stockQuantity );

        return productResponse;
    }

    private UUID productIdValue(Product product) {
        if ( product == null ) {
            return null;
        }
        ProductId id = product.getId();
        if ( id == null ) {
            return null;
        }
        UUID value = id.getValue();
        if ( value == null ) {
            return null;
        }
        return value;
    }

    private BigDecimal productPriceAmount(Product product) {
        if ( product == null ) {
            return null;
        }
        Money price = product.getPrice();
        if ( price == null ) {
            return null;
        }
        BigDecimal amount = price.getAmount();
        if ( amount == null ) {
            return null;
        }
        return amount;
    }

    private String productPriceCurrency(Product product) {
        if ( product == null ) {
            return null;
        }
        Money price = product.getPrice();
        if ( price == null ) {
            return null;
        }
        String currency = price.getCurrency();
        if ( currency == null ) {
            return null;
        }
        return currency;
    }

    private int productStockQuantityValue(Product product) {
        if ( product == null ) {
            return 0;
        }
        StockQuantity stockQuantity = product.getStockQuantity();
        if ( stockQuantity == null ) {
            return 0;
        }
        int value = stockQuantity.getValue();
        return value;
    }
}
