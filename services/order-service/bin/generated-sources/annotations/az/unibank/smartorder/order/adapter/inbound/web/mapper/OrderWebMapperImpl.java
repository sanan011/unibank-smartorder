package az.unibank.smartorder.order.adapter.inbound.web.mapper;

import az.unibank.smartorder.order.adapter.inbound.web.dto.response.OrderItemResponse;
import az.unibank.smartorder.order.adapter.inbound.web.dto.response.OrderResponse;
import az.unibank.smartorder.order.domain.model.aggregate.Order;
import az.unibank.smartorder.order.domain.model.entity.OrderItem;
import az.unibank.smartorder.order.domain.model.valueobject.CustomerId;
import az.unibank.smartorder.order.domain.model.valueobject.Money;
import az.unibank.smartorder.order.domain.model.valueobject.OrderId;
import az.unibank.smartorder.order.domain.model.valueobject.ProductId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-27T00:48:39+0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class OrderWebMapperImpl implements OrderWebMapper {

    @Override
    public OrderResponse toResponse(Order order) {
        if ( order == null ) {
            return null;
        }

        UUID id = null;
        UUID customerId = null;
        BigDecimal totalAmount = null;
        String currency = null;
        String status = null;
        List<OrderItemResponse> items = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        id = orderIdValue( order );
        customerId = orderCustomerIdValue( order );
        totalAmount = orderTotalAmountAmount( order );
        currency = orderTotalAmountCurrency( order );
        if ( order.getStatus() != null ) {
            status = order.getStatus().name();
        }
        items = toItemResponses( order.getItems() );
        createdAt = order.getCreatedAt();
        updatedAt = order.getUpdatedAt();

        OrderResponse orderResponse = new OrderResponse( id, customerId, status, items, totalAmount, currency, createdAt, updatedAt );

        return orderResponse;
    }

    @Override
    public List<OrderItemResponse> toItemResponses(List<OrderItem> items) {
        if ( items == null ) {
            return null;
        }

        List<OrderItemResponse> list = new ArrayList<OrderItemResponse>( items.size() );
        for ( OrderItem orderItem : items ) {
            list.add( toItemResponse( orderItem ) );
        }

        return list;
    }

    @Override
    public OrderItemResponse toItemResponse(OrderItem item) {
        if ( item == null ) {
            return null;
        }

        UUID productId = null;
        BigDecimal unitPrice = null;
        String currency = null;
        String productName = null;
        int quantity = 0;

        productId = itemProductIdValue( item );
        unitPrice = itemUnitPriceAmount( item );
        currency = itemUnitPriceCurrency( item );
        productName = item.getProductName();
        quantity = item.getQuantity();

        BigDecimal subTotal = item.getSubTotal().getAmount();

        OrderItemResponse orderItemResponse = new OrderItemResponse( productId, productName, unitPrice, currency, quantity, subTotal );

        return orderItemResponse;
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
