package az.unibank.smartorder.order.infrastructure.persistence.jpa.mapper;

import az.unibank.smartorder.order.domain.model.aggregate.Order;
import az.unibank.smartorder.order.domain.model.entity.OrderItem;
import az.unibank.smartorder.order.domain.model.valueobject.CustomerId;
import az.unibank.smartorder.order.domain.model.valueobject.Money;
import az.unibank.smartorder.order.domain.model.valueobject.OrderId;
import az.unibank.smartorder.order.domain.model.valueobject.OrderStatus;
import az.unibank.smartorder.order.domain.model.valueobject.ProductId;
import az.unibank.smartorder.order.infrastructure.persistence.jpa.entity.OrderItemJpaEntity;
import az.unibank.smartorder.order.infrastructure.persistence.jpa.entity.OrderJpaEntity;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-26T17:38:29+0400",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.7.jar, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class OrderPersistenceMapperImpl implements OrderPersistenceMapper {

    @Override
    public OrderJpaEntity toJpaEntity(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderJpaEntity.OrderJpaEntityBuilder orderJpaEntity = OrderJpaEntity.builder();

        orderJpaEntity.id( orderIdValue( order ) );
        orderJpaEntity.customerId( orderCustomerIdValue( order ) );
        orderJpaEntity.totalAmount( orderTotalAmountAmount( order ) );
        orderJpaEntity.currency( orderTotalAmountCurrency( order ) );
        if ( order.getStatus() != null ) {
            orderJpaEntity.status( order.getStatus().name() );
        }
        orderJpaEntity.version( order.getVersion() );
        orderJpaEntity.createdAt( order.getCreatedAt() );
        orderJpaEntity.updatedAt( order.getUpdatedAt() );
        orderJpaEntity.items( orderItemListToOrderItemJpaEntityList( order.getItems() ) );

        return orderJpaEntity.build();
    }

    @Override
    public Order toDomainModel(OrderJpaEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Order.OrderBuilder order = Order.builder();

        if ( entity.getStatus() != null ) {
            order.status( Enum.valueOf( OrderStatus.class, entity.getStatus() ) );
        }
        order.items( orderItemJpaEntityListToOrderItemList( entity.getItems() ) );
        order.version( entity.getVersion() );
        order.createdAt( entity.getCreatedAt() );
        order.updatedAt( entity.getUpdatedAt() );

        order.id( az.unibank.smartorder.order.domain.model.valueobject.OrderId.of(entity.getId()) );
        order.customerId( az.unibank.smartorder.order.domain.model.valueobject.CustomerId.of(entity.getCustomerId()) );
        order.totalAmount( az.unibank.smartorder.order.domain.model.valueobject.Money.of(entity.getTotalAmount(), entity.getCurrency()) );

        return order.build();
    }

    @Override
    public OrderItemJpaEntity toJpaEntity(OrderItem item) {
        if ( item == null ) {
            return null;
        }

        OrderItemJpaEntity.OrderItemJpaEntityBuilder orderItemJpaEntity = OrderItemJpaEntity.builder();

        orderItemJpaEntity.id( item.getId() );
        orderItemJpaEntity.productId( itemProductIdValue( item ) );
        orderItemJpaEntity.productNameSnapshot( item.getProductName() );
        orderItemJpaEntity.unitPrice( itemUnitPriceAmount( item ) );
        orderItemJpaEntity.currency( itemUnitPriceCurrency( item ) );
        orderItemJpaEntity.quantity( item.getQuantity() );

        return orderItemJpaEntity.build();
    }

    @Override
    public OrderItem toDomainModel(OrderItemJpaEntity entity) {
        if ( entity == null ) {
            return null;
        }

        OrderItem.OrderItemBuilder orderItem = OrderItem.builder();

        orderItem.id( entity.getId() );
        orderItem.productName( entity.getProductNameSnapshot() );
        orderItem.quantity( entity.getQuantity() );

        orderItem.productId( az.unibank.smartorder.order.domain.model.valueobject.ProductId.of(entity.getProductId()) );
        orderItem.unitPrice( az.unibank.smartorder.order.domain.model.valueobject.Money.of(entity.getUnitPrice(), entity.getCurrency()) );

        return orderItem.build();
    }

    private UUID orderIdValue(Order order) {
        if ( order == null ) {
            return null;
        }
        OrderId id = order.getId();
        if ( id == null ) {
            return null;
        }
        UUID value = id.getValue();
        if ( value == null ) {
            return null;
        }
        return value;
    }

    private UUID orderCustomerIdValue(Order order) {
        if ( order == null ) {
            return null;
        }
        CustomerId customerId = order.getCustomerId();
        if ( customerId == null ) {
            return null;
        }
        UUID value = customerId.getValue();
        if ( value == null ) {
            return null;
        }
        return value;
    }

    private BigDecimal orderTotalAmountAmount(Order order) {
        if ( order == null ) {
            return null;
        }
        Money totalAmount = order.getTotalAmount();
        if ( totalAmount == null ) {
            return null;
        }
        BigDecimal amount = totalAmount.getAmount();
        if ( amount == null ) {
            return null;
        }
        return amount;
    }

    private String orderTotalAmountCurrency(Order order) {
        if ( order == null ) {
            return null;
        }
        Money totalAmount = order.getTotalAmount();
        if ( totalAmount == null ) {
            return null;
        }
        String currency = totalAmount.getCurrency();
        if ( currency == null ) {
            return null;
        }
        return currency;
    }

    protected List<OrderItemJpaEntity> orderItemListToOrderItemJpaEntityList(List<OrderItem> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderItemJpaEntity> list1 = new ArrayList<OrderItemJpaEntity>( list.size() );
        for ( OrderItem orderItem : list ) {
            list1.add( toJpaEntity( orderItem ) );
        }

        return list1;
    }

    protected List<OrderItem> orderItemJpaEntityListToOrderItemList(List<OrderItemJpaEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderItem> list1 = new ArrayList<OrderItem>( list.size() );
        for ( OrderItemJpaEntity orderItemJpaEntity : list ) {
            list1.add( toDomainModel( orderItemJpaEntity ) );
        }

        return list1;
    }

    private UUID itemProductIdValue(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }
        ProductId productId = orderItem.getProductId();
        if ( productId == null ) {
            return null;
        }
        UUID value = productId.getValue();
        if ( value == null ) {
            return null;
        }
        return value;
    }

    private BigDecimal itemUnitPriceAmount(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }
        Money unitPrice = orderItem.getUnitPrice();
        if ( unitPrice == null ) {
            return null;
        }
        BigDecimal amount = unitPrice.getAmount();
        if ( amount == null ) {
            return null;
        }
        return amount;
    }

    private String itemUnitPriceCurrency(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }
        Money unitPrice = orderItem.getUnitPrice();
        if ( unitPrice == null ) {
            return null;
        }
        String currency = unitPrice.getCurrency();
        if ( currency == null ) {
            return null;
        }
        return currency;
    }
}
