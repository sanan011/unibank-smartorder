package az.unibank.smartorder.order.infrastructure.persistence.jpa.mapper;

import az.unibank.smartorder.order.domain.model.aggregate.Product;
import az.unibank.smartorder.order.domain.model.valueobject.Money;
import az.unibank.smartorder.order.domain.model.valueobject.ProductId;
import az.unibank.smartorder.order.domain.model.valueobject.StockQuantity;
import az.unibank.smartorder.order.infrastructure.persistence.jpa.entity.ProductJpaEntity;
import java.math.BigDecimal;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-27T00:11:42+0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class ProductPersistenceMapperImpl implements ProductPersistenceMapper {

    @Override
    public ProductJpaEntity toJpaEntity(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductJpaEntity.ProductJpaEntityBuilder productJpaEntity = ProductJpaEntity.builder();

        productJpaEntity.id( productIdValue( product ) );
        productJpaEntity.price( productPriceAmount( product ) );
        productJpaEntity.currency( productPriceCurrency( product ) );
        productJpaEntity.stockQuantity( productStockQuantityValue( product ) );
        productJpaEntity.name( product.getName() );
        productJpaEntity.version( product.getVersion() );

        return productJpaEntity.build();
    }

    @Override
    public Product toDomainModel(ProductJpaEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Product.ProductBuilder product = Product.builder();

        product.name( entity.getName() );
        product.version( entity.getVersion() );

        product.id( az.unibank.smartorder.order.domain.model.valueobject.ProductId.of(entity.getId()) );
        product.price( az.unibank.smartorder.order.domain.model.valueobject.Money.of(entity.getPrice(), entity.getCurrency()) );
        product.stockQuantity( az.unibank.smartorder.order.domain.model.valueobject.StockQuantity.of(entity.getStockQuantity()) );

        return product.build();
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
